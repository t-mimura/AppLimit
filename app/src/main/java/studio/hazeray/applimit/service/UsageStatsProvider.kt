package studio.hazeray.applimit.service

interface UsageStatsProvider {
    fun getCurrentForegroundPackage(): String?
}
