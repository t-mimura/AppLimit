package studio.hazeray.applimit.service

interface UsageStatsProvider {
    fun getCurrentForeground(): ForegroundActivity?
}
