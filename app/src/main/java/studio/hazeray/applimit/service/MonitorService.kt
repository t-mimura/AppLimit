package studio.hazeray.applimit.service

import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import studio.hazeray.applimit.BuildConfig
import studio.hazeray.applimit.data.repository.TargetAppRepository
import studio.hazeray.applimit.debug.DebugLogStore
import studio.hazeray.applimit.debug.DebugSettings
import studio.hazeray.applimit.debug.DebugTickRecord
import studio.hazeray.applimit.domain.model.AppSession
import studio.hazeray.applimit.domain.model.SessionState
import studio.hazeray.applimit.domain.model.TargetApp
import studio.hazeray.applimit.overlay.DebugOverlayController
import studio.hazeray.applimit.overlay.DebugOverlayInfo
import studio.hazeray.applimit.overlay.OverlayController
import studio.hazeray.applimit.ui.permission.hasNotificationPermission
import studio.hazeray.applimit.ui.permission.hasUsageStatsPermission

@AndroidEntryPoint
class MonitorService : LifecycleService() {

    @Inject lateinit var monitorLoop: MonitorLoop

    @Inject lateinit var sessionManager: SessionManager

    @Inject lateinit var repository: TargetAppRepository

    @Inject lateinit var notificationHelper: NotificationHelper

    @Inject lateinit var overlayController: OverlayController

    @Inject lateinit var debugOverlayController: DebugOverlayController

    @Inject lateinit var debugLogStore: DebugLogStore

    @Inject lateinit var debugSettings: DebugSettings

    private var isMonitoring = false
    private var warningNotifiedAppIds: Set<Long> = emptySet()
    private var cooldownNotifiedAppIds: Set<Long> = emptySet()
    private var lastForegroundWasTarget = false
    private val appLabelCache = mutableMapOf<String, String>()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (!hasAllRequiredPermissions()) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "missing required permissions; stopping service")
            }
            stopSelf(startId)
            return START_NOT_STICKY
        }

        startForeground(
            NotificationHelper.MONITOR_NOTIFICATION_ID,
            notificationHelper.buildMonitorNotification()
        )

        if (!isMonitoring) {
            isMonitoring = true
            startMonitoringLoop()
        }

        return START_STICKY
    }

    private fun hasAllRequiredPermissions(): Boolean = hasUsageStatsPermission(this) &&
        Settings.canDrawOverlays(this) &&
        hasNotificationPermission(this)

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        isMonitoring = false
        overlayController.hideOverlay()
        debugOverlayController.hide()
        super.onDestroy()
    }

    private fun startMonitoringLoop() {
        lifecycleScope.launch {
            while (isMonitoring) {
                val enabledApps = repository.getEnabledTargetApps().first()
                val currentTime = System.currentTimeMillis()

                val tickResult = monitorLoop.tick(enabledApps, currentTime)

                pruneStaleNotifiedIds()
                handleSessionState(tickResult.matchedApp, currentTime)
                if (BuildConfig.DEBUG) {
                    recordDebugTick(
                        tickResult.foregroundPackage,
                        tickResult.foregroundClassName,
                        tickResult.matchedApp,
                        currentTime
                    )
                    updateDebugOverlay(
                        tickResult.foregroundPackage,
                        tickResult.matchedApp,
                        currentTime
                    )
                }

                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private fun pruneStaleNotifiedIds() {
        val activeSessions = sessionManager.sessions.value
        val newCooldown = cooldownNotifiedAppIds.filter {
            activeSessions[it]?.state == SessionState.COOLDOWN
        }.toSet()
        if (newCooldown.size != cooldownNotifiedAppIds.size) {
            notificationHelper.cancelLimitReachedNotification()
        }
        cooldownNotifiedAppIds = newCooldown

        val newWarning = warningNotifiedAppIds.filter {
            activeSessions[it]?.state == SessionState.ACTIVE
        }.toSet()
        if (newWarning.size != warningNotifiedAppIds.size) {
            notificationHelper.cancelWarningNotification()
        }
        warningNotifiedAppIds = newWarning
    }

    private fun recordDebugTick(
        foregroundPackage: String?,
        foregroundClassName: String?,
        matchedApp: TargetApp?,
        currentTime: Long
    ) {
        val session = matchedApp?.let { sessionManager.getSession(it.id) }
        val (_, remainingMs) = phaseAndRemaining(session, currentTime)
        debugLogStore.record(
            DebugTickRecord(
                timestamp = currentTime,
                foregroundPackage = foregroundPackage,
                foregroundClassName = foregroundClassName,
                isTarget = matchedApp != null,
                targetAppName = matchedApp?.appName,
                sessionState = session?.state?.name,
                remainingMs = remainingMs,
                isExtended = session?.isExtended == true
            )
        )
    }

    private fun updateDebugOverlay(
        foregroundPackage: String?,
        matchedApp: TargetApp?,
        currentTime: Long
    ) {
        if (!debugSettings.overlayEnabled.value) {
            debugOverlayController.hide()
            return
        }
        if (overlayController.isShowing()) {
            debugOverlayController.hide()
            return
        }

        val label = matchedApp?.appName
            ?: foregroundPackage?.let { resolveAppLabel(it) }
            ?: "(none)"
        val session = matchedApp?.let { sessionManager.getSession(it.id) }
        val (phase, remainingMs) = phaseAndRemaining(session, currentTime)

        debugOverlayController.update(
            DebugOverlayInfo(
                foregroundLabel = label,
                foregroundPackage = foregroundPackage,
                isTarget = matchedApp != null,
                phase = phase,
                remainingMs = remainingMs
            )
        )
    }

    private fun phaseAndRemaining(
        session: AppSession?,
        currentTime: Long
    ): Pair<DebugOverlayInfo.Phase?, Long?> {
        if (session == null) return null to null
        return when (session.state) {
            SessionState.ACTIVE -> {
                val phase = if (session.isExtended) {
                    DebugOverlayInfo.Phase.EXTENDED
                } else {
                    DebugOverlayInfo.Phase.BEFORE_LIMIT
                }
                phase to (session.expiresAt - currentTime)
            }
            SessionState.COOLDOWN -> {
                val until = session.cooldownUntil ?: return null to null
                DebugOverlayInfo.Phase.COOLDOWN to (until - currentTime)
            }
            SessionState.IDLE -> null to null
        }
    }

    private fun resolveAppLabel(packageName: String): String {
        appLabelCache[packageName]?.let { return it }
        val pm = packageManager
        val label = try {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
        appLabelCache[packageName] = label
        return label
    }

    private fun handleSessionState(matchedApp: TargetApp?, currentTime: Long) {
        if (matchedApp == null) {
            if (lastForegroundWasTarget) {
                notificationHelper.cancelWarningNotification()
                notificationHelper.cancelLimitReachedNotification()
            }
            lastForegroundWasTarget = false
            if (overlayController.isShowing()) {
                overlayController.hideOverlay()
            }
            return
        }
        lastForegroundWasTarget = true

        val session = sessionManager.getSession(matchedApp.id)
        if (session == null) {
            if (overlayController.isShowing()) {
                overlayController.hideOverlay()
            }
            return
        }

        val targetApp = matchedApp

        when (session.state) {
            SessionState.ACTIVE -> {
                val remainingMs = session.expiresAt - currentTime
                val remainingMinutes = (remainingMs / 60_000).toInt()
                val warningThreshold = if (targetApp.limitMinutes <= 5) 1 else 5

                if (remainingMinutes in 0..warningThreshold &&
                    targetApp.id !in warningNotifiedAppIds
                ) {
                    notificationHelper.showWarningNotification(
                        targetApp.appName,
                        remainingMinutes
                    )
                    warningNotifiedAppIds = warningNotifiedAppIds + targetApp.id
                }

                if (overlayController.isShowing()) {
                    overlayController.hideOverlay()
                }
            }
            SessionState.COOLDOWN -> {
                if (targetApp.id !in cooldownNotifiedAppIds) {
                    notificationHelper.showLimitReachedNotification(targetApp.appName)
                    notificationHelper.cancelWarningNotification()
                    cooldownNotifiedAppIds = cooldownNotifiedAppIds + targetApp.id
                }

                if (!overlayController.isShowing()) {
                    val cooldownUntil = session.cooldownUntil ?: return
                    val remainingMinutes = ((cooldownUntil - currentTime) / 60_000).toInt()
                        .coerceAtLeast(0)
                    overlayController.showCooldownOverlay(
                        appName = targetApp.appName,
                        remainingMinutes = remainingMinutes,
                        onExtend = {
                            sessionManager.extend(targetApp, System.currentTimeMillis())
                            overlayController.hideOverlay()
                            notificationHelper.cancelLimitReachedNotification()
                        },
                        onDismiss = {
                            overlayController.hideOverlay()
                            notificationHelper.cancelLimitReachedNotification()
                            navigateHome()
                        }
                    )
                }
            }
            SessionState.IDLE -> {
                if (overlayController.isShowing()) {
                    overlayController.hideOverlay()
                }
            }
        }
    }

    private fun navigateHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(homeIntent)
    }

    companion object {
        private const val TAG = "AppLimitMonitor"
        private const val POLLING_INTERVAL_MS = 3_000L
    }
}
