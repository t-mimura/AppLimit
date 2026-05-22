package studio.hazeray.applimit.ui.settings

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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.hazeray.applimit.data.repository.fake.FakeTargetAppRepository
import studio.hazeray.applimit.domain.model.TargetApp

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTargetAppRepository

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTargetAppRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `指定IDのアプリ情報を読み込める`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        val viewModel = SettingsViewModel(repository, id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.targetApp.test {
            val loaded = awaitItem()
            assertEquals("Instagram", loaded?.appName)
            assertEquals(15, loaded?.limitMinutes)
        }
    }

    @Test
    fun `編集はドラフトに反映され保存前は永続化されない`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        val viewModel = SettingsViewModel(repository, id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateLimitMinutes(30)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(30, viewModel.draft.value?.limitMinutes)
        // Repository should still hold the original value.
        assertEquals(15, repository.getTargetAppById(id)?.limitMinutes)
    }

    @Test
    fun `save()でドラフトの変更が永続化される`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        val viewModel = SettingsViewModel(repository, id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateLimitMinutes(30)
        viewModel.updateCooldownMinutes(90)
        viewModel.updateExtensionMinutes(10)
        viewModel.toggleEnabled()
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        val saved = repository.getTargetAppById(id)
        assertEquals(30, saved?.limitMinutes)
        assertEquals(90, saved?.cooldownMinutes)
        assertEquals(10, saved?.extensionMinutes)
        assertFalse(saved?.isEnabled ?: true)
    }

    @Test
    fun `saveを呼ばないとドラフトの編集は永続化されない`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        val viewModel = SettingsViewModel(repository, id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleEnabled()
        viewModel.updateLimitMinutes(45)
        testDispatcher.scheduler.advanceUntilIdle()

        val current = repository.getTargetAppById(id)
        assertTrue(current?.isEnabled ?: false)
        assertEquals(15, current?.limitMinutes)
    }

    @Test
    fun `アプリを削除できる`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        val viewModel = SettingsViewModel(repository, id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteApp()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(repository.getTargetAppById(id))
    }
}
