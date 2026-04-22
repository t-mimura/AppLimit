package studio.hazeray.applimit.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UsageStatsProvider {

    private var lastQueryTime: Long = 0L
    private var cachedForeground: String? = null

    @Suppress("DEPRECATION")
    override fun getCurrentForegroundPackage(): String? {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return null

        val endTime = System.currentTimeMillis()
        val beginTime = if (lastQueryTime == 0L) endTime - INITIAL_LOOKBACK_MS else lastQueryTime

        val events = usageStatsManager.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    cachedForeground = event.packageName
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (event.packageName == cachedForeground) {
                        cachedForeground = null
                    }
                }
            }
        }

        lastQueryTime = endTime
        return cachedForeground
    }

    companion object {
        private const val INITIAL_LOOKBACK_MS = 60_000L
    }
}
