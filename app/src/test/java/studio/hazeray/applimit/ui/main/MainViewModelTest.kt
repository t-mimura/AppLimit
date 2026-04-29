package studio.hazeray.applimit.ui.main

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.hazeray.applimit.data.repository.fake.FakeTargetAppRepository
import studio.hazeray.applimit.domain.model.TargetApp
import studio.hazeray.applimit.service.SessionManager

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTargetAppRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTargetAppRepository()
        sessionManager = SessionManager()
        viewModel = MainViewModel(repository, sessionManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態で対象アプリリストが空`() = runTest {
        viewModel.rows.test {
            assertEquals(emptyList<TargetAppRowState>(), awaitItem())
        }
    }

    @Test
    fun `対象アプリ追加後にリストに反映される`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        repository.addTargetApp(app)

        viewModel.rows.test {
            // initial empty
            awaitItem()
            // after repository emits and combine fires
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Instagram", list[0].app.appName)
            assertEquals(SessionStatus.NotStarted, list[0].status)
        }
    }

    @Test
    fun `有効無効の切り替えが反映される`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        viewModel.toggleEnabled(id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.rows.test {
            // initial empty from stateIn, then combine fires with real data
            skipItems(1)
            val list = awaitItem()
            assertFalse(list[0].app.isEnabled)
        }
    }

    @Test
    fun `対象アプリ削除が反映される`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        viewModel.removeApp(id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.rows.test {
            // initial empty; after deletion combine also produces empty
            assertTrue(awaitItem().isEmpty())
        }
    }
}
