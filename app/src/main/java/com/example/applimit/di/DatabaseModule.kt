package com.example.applimit.di

import android.content.Context
import androidx.room.Room
import com.example.applimit.data.db.AppLimitDatabase
import com.example.applimit.data.db.TargetAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
