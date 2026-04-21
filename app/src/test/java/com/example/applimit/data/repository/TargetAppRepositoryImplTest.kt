package com.example.applimit.data.repository

import app.cash.turbine.test
import com.example.applimit.data.db.TargetAppDao
import com.example.applimit.data.db.TargetAppEntity
import com.example.applimit.domain.model.TargetApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TargetAppRepositoryImplTest {
    private lateinit var fakeDao: FakeTargetAppDao
    private lateinit var repository: TargetAppRepositoryImpl

    @BeforeEach
    fun setup() {
        fakeDao = FakeTargetAppDao()
        repository = TargetAppRepositoryImpl(fakeDao)
    }

    @Test
    fun `getAllTargetAppsで全アプリをドメインモデルとして取得できる`() = runTest {
        fakeDao.insertEntity(
            createEntity(id = 1L, packageName = "com.app1", appName = "App1")
        )

        repository.getAllTargetApps().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("com.app1", items[0].packageName)
            assertEquals("App1", items[0].appName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getEnabledTargetAppsで有効なアプリのみ取得できる`() = runTest {
        fakeDao.insertEntity(
            createEntity(id = 1L, packageName = "com.enabled", isEnabled = true)
        )
        fakeDao.insertEntity(
            createEntity(id = 2L, packageName = "com.disabled", isEnabled = false)
        )

        repository.getEnabledTargetApps().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("com.enabled", items[0].packageName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTargetAppByIdでIDからドメインモデルを取得できる`() = runTest {
        fakeDao.insertEntity(
            createEntity(id = 1L, packageName = "com.test")
        )

        val result = repository.getTargetAppById(1L)

        assertEquals("com.test", result?.packageName)
    }

    @Test
    fun `getTargetAppByIdで存在しないIDはnullを返す`() = runTest {
        val result = repository.getTargetAppById(999L)
        assertNull(result)
    }

    @Test
    fun `getTargetAppByPackageNameでパッケージ名から取得できる`() = runTest {
        fakeDao.insertEntity(
            createEntity(id = 1L, packageName = "com.instagram.android")
        )

        val result = repository.getTargetAppByPackageName("com.instagram.android")

        assertEquals("com.instagram.android", result?.packageName)
    }

    @Test
    fun `addTargetAppでアプリを追加できる`() = runTest {
        val app = TargetApp(
            packageName = "com.new.app",
            appName = "New App"
        )

        val id = repository.addTargetApp(app)

        assertEquals(1L, id)
        val stored = repository.getTargetAppById(id)
        assertEquals("com.new.app", stored?.packageName)
    }

    @Test
    fun `updateTargetAppでアプリを更新できる`() = runTest {
        fakeDao.insertEntity(
            createEntity(id = 1L, packageName = "com.test", limitMinutes = 15)
        )

        val updated = TargetApp(
            id = 1L,
            packageName = "com.test",
            appName = "Test",
            limitMinutes = 30,
            cooldownMinutes = 120,
            extensionMinutes = 5,
            isEnabled = true
        )
        repository.updateTargetApp(updated)

        val result = repository.getTargetAppById(1L)
        assertEquals(30, result?.limitMinutes)
    }

    @Test
    fun `removeTargetAppでアプリを削除できる`() = runTest {
        fakeDao.insertEntity(
            createEntity(id = 1L, packageName = "com.test")
        )

        val app = TargetApp(
            id = 1L,
            packageName = "com.test",
            appName = "Test",
            limitMinutes = 15,
            cooldownMinutes = 120,
            extensionMinutes = 5,
            isEnabled = true
        )
        repository.removeTargetApp(app)

        assertNull(repository.getTargetAppById(1L))
    }

    private fun createEntity(
        id: Long = 0L,
        packageName: String = "com.test.app",
        appName: String = "Test",
        limitMinutes: Int = 15,
        isEnabled: Boolean = true
    ) = TargetAppEntity(
        id = id,
        packageName = packageName,
        appName = appName,
        limitMinutes = limitMinutes,
        cooldownMinutes = 120,
        extensionMinutes = 5,
        isEnabled = isEnabled
    )
}

private class FakeTargetAppDao : TargetAppDao {
    private val entities = mutableListOf<TargetAppEntity>()
    private val flow = MutableStateFlow<List<TargetAppEntity>>(emptyList())
    private var nextId = 1L

    fun insertEntity(entity: TargetAppEntity) {
        val stored = if (entity.id == 0L) {
            entity.copy(id = nextId++)
        } else {
            entity.also {
                nextId = maxOf(nextId, entity.id + 1)
            }
        }
        entities.add(stored)
        flow.value = entities.toList()
    }

    override fun getAll(): Flow<List<TargetAppEntity>> = flow

    override suspend fun getById(id: Long): TargetAppEntity? = entities.find { it.id == id }

    override suspend fun getByPackageName(packageName: String): TargetAppEntity? =
        entities.find { it.packageName == packageName }

    override suspend fun insert(entity: TargetAppEntity): Long {
        val id = nextId++
        entities.add(entity.copy(id = id))
        flow.value = entities.toList()
        return id
    }

    override suspend fun update(entity: TargetAppEntity) {
        val index = entities.indexOfFirst { it.id == entity.id }
        if (index >= 0) {
            entities[index] = entity
            flow.value = entities.toList()
        }
    }

    override suspend fun delete(entity: TargetAppEntity) {
        entities.removeAll { it.id == entity.id }
        flow.value = entities.toList()
    }

    override fun getEnabledApps(): Flow<List<TargetAppEntity>> =
        flow.map { list -> list.filter { it.isEnabled } }
}
