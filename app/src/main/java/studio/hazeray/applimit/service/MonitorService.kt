package studio.hazeray.applimit.service

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import studio.hazeray.applimit.data.repository.TargetAppRepository
import studio.hazeray.applimit.domain.model.SessionState
import studio.hazeray.applimit.domain.model.TargetApp
import studio.hazeray.applimit.overlay.OverlayController

@AndroidEntryPoint
class MonitorService : LifecycleService() {

    @Inject lateinit var monitorLoop: MonitorLoop

    @Inject lateinit var sessionManager: SessionManager

    @Inject lateinit var repository: TargetAppRepository

    @Inject lateinit var notificationHelper: NotificationHelper

    @Inject lateinit var overlayController: OverlayController

    private var isMonitoring = false
    private var warningNotificationSent = false

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

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

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        isMonitoring = false
        overlayController.hideOverlay()
        super.onDestroy()
    }

    private fun startMonitoringLoop() {
        lifecycleScope.launch {
            while (isMonitoring) {
                val enabledApps = repository.getEnabledTargetApps().first()
                val currentTime = System.currentTimeMillis()

                monitorLoop.tick(enabledApps, currentTime)

                handleSessionState(enabledApps, currentTime)

                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private fun handleSessionState(enabledApps: List<TargetApp>, currentTime: Long) {
        val session = sessionManager.currentSession.value ?: run {
            overlayController.hideOverlay()
            warningNotificationSent = false
            notificationHelper.cancelWarningNotifications()
            return
        }

        val targetApp = enabledApps.find { it.id == session.targetAppId } ?: return

        when (session.state) {
            SessionState.ACTIVE -> {
                val remainingMs = session.expiresAt - currentTime
                val remainingMinutes = (remainingMs / 60_000).toInt()
                val warningThreshold = if (targetApp.limitMinutes <= 5) 1 else 5

                if (remainingMinutes <= warningThreshold && !warningNotificationSent) {
                    notificationHelper.showWarningNotification(
                        targetApp.appName,
                        remainingMinutes
                    )
                    warningNotificationSent = true
                }

                if (overlayController.isShowing()) {
                    overlayController.hideOverlay()
                }
            }
            SessionState.WARNING -> {
                notificationHelper.showLimitReachedNotification(targetApp.appName)

                if (!overlayController.isShowing()) {
                    val usedMinutes = ((currentTime - session.startedAt) / 60_000).toInt()
                    overlayController.showWarningOverlay(
                        appName = targetApp.appName,
                        usedMinutes = usedMinutes,
                        onExtend = {
                            sessionManager.extend(targetApp, System.currentTimeMillis())
                            overlayController.hideOverlay()
                            warningNotificationSent = false
                            notificationHelper.cancelWarningNotifications()
                        },
                        onDismiss = {
                            sessionManager.dismiss(targetApp, System.currentTimeMillis())
                            overlayController.hideOverlay()
                            notificationHelper.cancelWarningNotifications()
                            navigateHome()
                        }
                    )
                }
            }
            SessionState.COOLDOWN -> {
                if (!overlayController.isShowing()) {
                    val cooldownUntil = session.cooldownUntil ?: return
                    val remainingMinutes = ((cooldownUntil - currentTime) / 60_000).toInt()
                    overlayController.showCooldownOverlay(
                        appName = targetApp.appName,
                        remainingMinutes = remainingMinutes,
                        onExtend = {
                            sessionManager.extend(targetApp, System.currentTimeMillis())
                            overlayController.hideOverlay()
                            warningNotificationSent = false
                        },
                        onDismiss = {
                            overlayController.hideOverlay()
                            navigateHome()
                        }
                    )
                }
            }
            SessionState.IDLE -> {
                overlayController.hideOverlay()
                warningNotificationSent = false
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
        private const val POLLING_INTERVAL_MS = 3_000L
    }
}
