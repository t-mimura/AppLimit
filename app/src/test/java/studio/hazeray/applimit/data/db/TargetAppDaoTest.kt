package studio.hazeray.applimit.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TargetAppDaoTest {
    private lateinit var database: AppLimitDatabase
    private lateinit var dao: TargetAppDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppLimitDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.targetAppDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createEntity(
        packageName: String = "com.test.app",
        appName: String = "Test App",
        isEnabled: Boolean = true
    ) = TargetAppEntity(
        packageName = packageName,
        appName = appName,
        limitMinutes = 15,
        cooldownMinutes = 120,
        extensionMinutes = 5,
        isEnabled = isEnabled
    )

    @Test
    fun `insertしたエンティティをgetByIdで取得できる`() = runTest {
        val entity = createEntity()
        val id = dao.insert(entity)

        val result = dao.getById(id)

        assertEquals(id, result?.id)
        assertEquals("com.test.app", result?.packageName)
        assertEquals("Test App", result?.appName)
    }

    @Test
    fun `存在しないIDを取得するとnullが返る`() = runTest {
        val result = dao.getById(999L)
        assertNull(result)
    }

    @Test
    fun `getByPackageNameでパッケージ名から取得できる`() = runTest {
        val entity = createEntity(packageName = "com.instagram.android")
        dao.insert(entity)

        val result = dao.getByPackageName("com.instagram.android")

        assertEquals("com.instagram.android", result?.packageName)
    }

    @Test
    fun `getAllでFlow経由で全件取得できる`() = runTest {
        dao.insert(createEntity(packageName = "com.app1", appName = "App1"))
        dao.insert(createEntity(packageName = "com.app2", appName = "App2"))

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateでエンティティを更新できる`() = runTest {
        val id = dao.insert(createEntity())
        val updated = createEntity().copy(id = id, limitMinutes = 30)

        dao.update(updated)

        val result = dao.getById(id)
        assertEquals(30, result?.limitMinutes)
    }

    @Test
    fun `deleteでエンティティを削除できる`() = runTest {
        val id = dao.insert(createEntity())
        val entity = dao.getById(id) ?: return@runTest

        dao.delete(entity)

        assertNull(dao.getById(id))
    }

    @Test
    fun `getEnabledAppsで有効なアプリのみ取得できる`() = runTest {
        dao.insert(createEntity(packageName = "com.enabled", isEnabled = true))
        dao.insert(createEntity(packageName = "com.disabled", isEnabled = false))

        dao.getEnabledApps().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("com.enabled", items[0].packageName)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
