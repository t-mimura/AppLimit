package com.example.applimit.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "target_apps")
data class TargetAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val limitMinutes: Int,
    val cooldownMinutes: Int,
    val extensionMinutes: Int,
    val isEnabled: Boolean
)
