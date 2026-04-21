package com.example.applimit.service.fake

import com.example.applimit.service.UsageStatsProvider

class FakeUsageStatsProvider : UsageStatsProvider {
    var foregroundPackage: String? = null

    override fun getCurrentForegroundPackage(): String? = foregroundPackage
}
