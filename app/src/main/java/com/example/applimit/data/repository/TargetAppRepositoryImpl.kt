package com.example.applimit.data.repository

import com.example.applimit.data.db.TargetAppDao
import com.example.applimit.domain.model.TargetApp
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TargetAppRepositoryImpl @Inject constructor(
    private val dao: TargetAppDao
) : TargetAppRepository {
    override fun getAllTargetApps(): Flow<List<TargetApp>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getEnabledTargetApps(): Flow<List<TargetApp>> =
        dao.getEnabledApps().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTargetAppById(id: Long): TargetApp? = dao.getById(id)?.toDomain()

    override suspend fun getTargetAppByPackageName(packageName: String): TargetApp? =
        dao.getByPackageName(packageName)?.toDomain()

    override suspend fun addTargetApp(app: TargetApp): Long = dao.insert(app.toEntity())

    override suspend fun updateTargetApp(app: TargetApp) {
        dao.update(app.toEntity())
    }

    override suspend fun removeTargetApp(app: TargetApp) {
        dao.delete(app.toEntity())
    }
}
