package studio.hazeray.applimit.data.repository

import kotlinx.coroutines.flow.Flow
import studio.hazeray.applimit.domain.model.TargetApp

interface TargetAppRepository {
    fun getAllTargetApps(): Flow<List<TargetApp>>

    fun getEnabledTargetApps(): Flow<List<TargetApp>>

    suspend fun getTargetAppById(id: Long): TargetApp?

    suspend fun getTargetAppByPackageName(packageName: String): TargetApp?

    suspend fun addTargetApp(app: TargetApp): Long

    suspend fun updateTargetApp(app: TargetApp)

    suspend fun removeTargetApp(app: TargetApp)
}
