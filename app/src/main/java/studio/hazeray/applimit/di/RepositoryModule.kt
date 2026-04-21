package studio.hazeray.applimit.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import studio.hazeray.applimit.data.repository.TargetAppRepository
import studio.hazeray.applimit.data.repository.TargetAppRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTargetAppRepository(impl: TargetAppRepositoryImpl): TargetAppRepository
}
