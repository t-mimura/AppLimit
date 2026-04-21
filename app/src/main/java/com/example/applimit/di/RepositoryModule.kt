package com.example.applimit.di

import com.example.applimit.data.repository.TargetAppRepository
import com.example.applimit.data.repository.TargetAppRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTargetAppRepository(impl: TargetAppRepositoryImpl): TargetAppRepository
}
