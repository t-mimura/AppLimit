package studio.hazeray.applimit.service.fake

import studio.hazeray.applimit.service.UsageStatsProvider

class FakeUsageStatsProvider : UsageStatsProvider {
    var foregroundPackage: String? = null

    override fun getCurrentForegroundPackage(): String? = foregroundPackage
}
