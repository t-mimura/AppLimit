package studio.hazeray.applimit.ui.appselect

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.hazeray.applimit.data.repository.fake.FakeTargetAppRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AppSelectViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTargetAppRepository
    private lateinit var viewModel: AppSelectViewModel

    private val fakeInstalledApps = listOf(
        InstalledApp("com.instagram.android", "Instagram", null),
        InstalledApp("com.twitter.android", "Twitter", null),
        InstalledApp("com.facebook.katana", "Facebook", null)
    )

    private val fakeLister = object : InstalledAppLister {
        override fun getInstalledApps(): List<InstalledApp> = fakeInstalledApps
    }

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTargetAppRepository()
        viewModel = AppSelectViewModel(repository, fakeLister)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `インストール済みアプリの一覧が取得できる`() = runTest {
        viewModel.installedApps.test {
            val list = awaitItem()
            assertEquals(3, list.size)
        }
    }

    @Test
    fun `検索フィルタでアプリが絞り込める`() = runTest {
        viewModel.updateSearchQuery("Insta")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filteredApps.test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Instagram", list[0].appName)
        }
    }

    @Test
    fun `アプリを選択すると対象アプリに追加される`() = runTest {
        viewModel.selectApp(fakeInstalledApps[0])
        testDispatcher.scheduler.advanceUntilIdle()

        val added = repository.getTargetAppByPackageName("com.instagram.android")
        assertTrue(added != null)
        assertEquals("Instagram", added?.appName)
    }

    @Test
    fun `新規追加時の制限時間は10分`() = runTest {
        viewModel.selectApp(fakeInstalledApps[0])
        testDispatcher.scheduler.advanceUntilIdle()

        val added = repository.getTargetAppByPackageName("com.instagram.android")
        assertEquals(10, added?.limitMinutes)
    }
}
