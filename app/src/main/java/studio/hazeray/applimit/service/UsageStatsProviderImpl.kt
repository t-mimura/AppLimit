package studio.hazeray.applimit.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import studio.hazeray.applimit.BuildConfig

@Singleton
class UsageStatsProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UsageStatsProvider {

    private var lastQueryTime: Long = 0L
    private val activityOrder: ArrayDeque<Pair<String, String>> = ArrayDeque()

    @Suppress("DEPRECATION")
    override fun getCurrentForeground(): ForegroundActivity? {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return null

        val endTime = System.currentTimeMillis()
        val beginTime = if (lastQueryTime == 0L) endTime - INITIAL_LOOKBACK_MS else lastQueryTime

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "query orderBefore=$activityOrder")
        }

        val events = try {
            usageStatsManager.queryEvents(beginTime, endTime)
        } catch (e: SecurityException) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "queryEvents denied: ${e.message}")
            }
            return null
        }
        val event = UsageEvents.Event()
        val useActivityStopped = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val cls = event.className.orEmpty()
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    onActivityResumed(pkg, cls)
                    logTransition("FG", pkg, cls)
                }
                ACTIVITY_STOPPED -> {
                    if (useActivityStopped) {
                        onActivityStopped(pkg, cls)
                        logTransition("STOPPED", pkg, cls)
                    }
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (!useActivityStopped) {
                        onActivityStopped(pkg, cls)
                        logTransition("BG", pkg, cls)
                    }
                }
            }
        }

        lastQueryTime = endTime
        val top = activityOrder.lastOrNull()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "queryDone top=$top orderAfter=$activityOrder")
        }
        return top?.let { ForegroundActivity(it.first, it.second) }
    }

    private fun onActivityResumed(pkg: String, cls: String) {
        val key = pkg to cls
        activityOrder.remove(key)
        activityOrder.addLast(key)
        while (activityOrder.size > STACK_LIMIT) activityOrder.removeFirst()
    }

    private fun onActivityStopped(pkg: String, cls: String) {
        activityOrder.remove(pkg to cls)
    }

    private fun logTransition(kind: String, pkg: String, cls: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "$kind pkg=$pkg cls=$cls order=$activityOrder")
    }

    companion object {
        private const val TAG = "AppLimitUsage"
        private const val INITIAL_LOOKBACK_MS = 60_000L
        private const val STACK_LIMIT = 16

        // UsageEvents.Event.ACTIVITY_STOPPED was added in API 29. Using the literal
        // keeps the code compilable without version-guarded imports.
        private const val ACTIVITY_STOPPED = 23
    }
}
