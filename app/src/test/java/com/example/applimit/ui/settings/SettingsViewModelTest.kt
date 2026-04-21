package com.example.applimit.ui.settings

import app.cash.turbine.test
import com.example.applimit.data.repository.fake.FakeTargetAppRepository
import com.example.applimit.domain.model.TargetApp
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    fun `制限時間を更新できる`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        val viewModel = SettingsViewModel(repository, id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateLimitMinutes(30)
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = repository.getTargetAppById(id)
        assertEquals(30, updated?.limitMinutes)
    }

    @Test
    fun `クールダウン時間を更新できる`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        val viewModel = SettingsViewModel(repository, id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateCooldownMinutes(90)
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = repository.getTargetAppById(id)
        assertEquals(90, updated?.cooldownMinutes)
    }

    @Test
    fun `延長時間を更新できる`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        val viewModel = SettingsViewModel(repository, id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateExtensionMinutes(10)
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = repository.getTargetAppById(id)
        assertEquals(10, updated?.extensionMinutes)
    }

    @Test
    fun `有効無効を切り替えられる`() = runTest {
        val app = TargetApp(packageName = "com.instagram.android", appName = "Instagram")
        val id = repository.addTargetApp(app)

        val viewModel = SettingsViewModel(repository, id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleEnabled()
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = repository.getTargetAppById(id)
        assertFalse(updated?.isEnabled ?: true)
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
