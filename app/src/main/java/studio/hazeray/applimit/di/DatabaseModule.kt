package studio.hazeray.applimit.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import studio.hazeray.applimit.data.db.AppLimitDatabase
import studio.hazeray.applimit.data.db.TargetAppDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppLimitDatabase =
        Room.databaseBuilder(
            context,
            AppLimitDatabase::class.java,
            "app_limit.db"
        ).build()

    @Provides
    fun provideTargetAppDao(database: AppLimitDatabase): TargetAppDao = database.targetAppDao()
}
