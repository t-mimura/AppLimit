package studio.hazeray.applimit.service

import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UsageStatsProvider {

    override fun getCurrentForegroundPackage(): String? {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return null

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - QUERY_INTERVAL_MS

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginTime,
            endTime
        )

        return usageStats
            ?.filter { it.lastTimeUsed > 0 }
            ?.maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }

    companion object {
        private const val QUERY_INTERVAL_MS = 10_000L
    }
}
