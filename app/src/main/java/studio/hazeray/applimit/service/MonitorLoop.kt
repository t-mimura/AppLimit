package studio.hazeray.applimit.service

import javax.inject.Inject
import javax.inject.Singleton
import studio.hazeray.applimit.domain.model.TargetApp

@Singleton
class MonitorLoop @Inject constructor(
    private val usageStatsProvider: UsageStatsProvider,
    private val sessionManager: SessionManager
) {
    private var lastDetectedPackage: String? = null

    fun tick(enabledApps: List<TargetApp>, currentTimeMillis: Long): MonitorTickResult {
        val foreground = usageStatsProvider.getCurrentForeground()
        val foregroundPackage = foreground?.packageName
        val matchedApp = enabledApps.find { it.packageName == foregroundPackage }

        if (matchedApp != null) {
            sessionManager.onTargetAppDetected(matchedApp, currentTimeMillis)
            lastDetectedPackage = foregroundPackage
        } else if (lastDetectedPackage != null) {
            sessionManager.onTargetAppLeft(currentTimeMillis)
            lastDetectedPackage = null
        }

        sessionManager.checkState(enabledApps, currentTimeMillis)
        return MonitorTickResult(foregroundPackage, foreground?.className, matchedApp)
    }
}

data class MonitorTickResult(
    val foregroundPackage: String?,
    val foregroundClassName: String?,
    val matchedApp: TargetApp?
)
