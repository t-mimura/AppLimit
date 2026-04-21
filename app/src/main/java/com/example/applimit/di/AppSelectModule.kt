package com.example.applimit.di

import com.example.applimit.ui.appselect.InstalledAppLister
import com.example.applimit.ui.appselect.InstalledAppProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppSelectModule {

    @Binds
    @Singleton
    abstract fun bindInstalledAppLister(impl: InstalledAppProvider): InstalledAppLister
}
