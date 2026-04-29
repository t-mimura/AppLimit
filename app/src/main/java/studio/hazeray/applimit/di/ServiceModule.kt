package studio.hazeray.applimit.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import studio.hazeray.applimit.overlay.DebugOverlayController
import studio.hazeray.applimit.overlay.DebugOverlayControllerImpl
import studio.hazeray.applimit.overlay.OverlayController
import studio.hazeray.applimit.overlay.OverlayControllerImpl
import studio.hazeray.applimit.service.UsageStatsProvider
import studio.hazeray.applimit.service.UsageStatsProviderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindUsageStatsProvider(impl: UsageStatsProviderImpl): UsageStatsProvider

    @Binds
    @Singleton
    abstract fun bindOverlayController(impl: OverlayControllerImpl): OverlayController

    @Binds
    @Singleton
    abstract fun bindDebugOverlayController(
        impl: DebugOverlayControllerImpl
    ): DebugOverlayController
}
