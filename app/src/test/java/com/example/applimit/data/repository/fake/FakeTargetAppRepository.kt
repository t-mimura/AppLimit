package com.example.applimit.data.repository.fake

import com.example.applimit.data.repository.TargetAppRepository
import com.example.applimit.domain.model.TargetApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTargetAppRepository : TargetAppRepository {
    private val apps = MutableStateFlow<List<TargetApp>>(emptyList())
    private var nextId = 1L

    override fun getAllTargetApps(): Flow<List<TargetApp>> = apps

    override fun getEnabledTargetApps(): Flow<List<TargetApp>> =
        apps.map { list -> list.filter { it.isEnabled } }

    override suspend fun getTargetAppById(id: Long): TargetApp? = apps.value.find { it.id == id }

    override suspend fun getTargetAppByPackageName(packageName: String): TargetApp? =
        apps.value.find { it.packageName == packageName }

    override suspend fun addTargetApp(app: TargetApp): Long {
        val id = nextId++
        val newApp = app.copy(id = id)
        apps.update { it + newApp }
        return id
    }

    override suspend fun updateTargetApp(app: TargetApp) {
        apps.update { list ->
            list.map { if (it.id == app.id) app else it }
        }
    }

    override suspend fun removeTargetApp(app: TargetApp) {
        apps.update { list ->
            list.filter { it.id != app.id }
        }
    }
}
