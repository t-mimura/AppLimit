package com.example.applimit.data.repository

import com.example.applimit.domain.model.TargetApp
import kotlinx.coroutines.flow.Flow

interface TargetAppRepository {
    fun getAllTargetApps(): Flow<List<TargetApp>>

    fun getEnabledTargetApps(): Flow<List<TargetApp>>

    suspend fun getTargetAppById(id: Long): TargetApp?

    suspend fun getTargetAppByPackageName(packageName: String): TargetApp?

    suspend fun addTargetApp(app: TargetApp): Long

    suspend fun updateTargetApp(app: TargetApp)

    suspend fun removeTargetApp(app: TargetApp)
}
