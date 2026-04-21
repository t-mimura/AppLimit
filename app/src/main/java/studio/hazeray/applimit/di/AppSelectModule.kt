package studio.hazeray.applimit.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import studio.hazeray.applimit.ui.appselect.InstalledAppLister
import studio.hazeray.applimit.ui.appselect.InstalledAppProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class AppSelectModule {

    @Binds
    @Singleton
    abstract fun bindInstalledAppLister(impl: InstalledAppProvider): InstalledAppLister
}
