package com.example.applimit.service

interface UsageStatsProvider {
    fun getCurrentForegroundPackage(): String?
}
