package com.example.applimit.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TargetAppEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppLimitDatabase : RoomDatabase() {
    abstract fun targetAppDao(): TargetAppDao
}
