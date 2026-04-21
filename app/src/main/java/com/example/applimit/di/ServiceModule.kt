package com.example.applimit.di

import com.example.applimit.overlay.OverlayController
import com.example.applimit.overlay.OverlayControllerImpl
import com.example.applimit.service.UsageStatsProvider
import com.example.applimit.service.UsageStatsProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindUsageStatsProvider(impl: UsageStatsProviderImpl): UsageStatsProvider

    @Binds
    @Singleton
    abstract fun bindOverlayController(impl: OverlayControllerImpl): OverlayController
}
