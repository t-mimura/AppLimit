package studio.hazeray.applimit.service.fake

import studio.hazeray.applimit.service.ForegroundActivity
import studio.hazeray.applimit.service.UsageStatsProvider

class FakeUsageStatsProvider : UsageStatsProvider {
    var foreground: ForegroundActivity? = null

    var foregroundPackage: String?
        get() = foreground?.packageName
        set(value) {
            foreground = value?.let { ForegroundActivity(it, "") }
        }

    override fun getCurrentForeground(): ForegroundActivity? = foreground
}
