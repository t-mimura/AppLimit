package studio.hazeray.applimit.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.hazeray.applimit.domain.model.SessionState
import studio.hazeray.applimit.domain.model.TargetApp
import studio.hazeray.applimit.service.fake.FakeUsageStatsProvider

class MonitorLoopTest {

    private lateinit var usageStatsProvider: FakeUsageStatsProvider
    private lateinit var sessionManager: SessionManager
    private lateinit var monitorLoop: MonitorLoop

    private val instagram = TargetApp(
        id = 1L,
        packageName = "com.instagram.android",
        appName = "Instagram",
        limitMinutes = 10,
        cooldownMinutes = 60,
        extensionMinutes = 5
    )

    private val enabledApps = listOf(instagram)
    private val baseTime = 1_000_000L

    @BeforeEach
    fun setup() {
        usageStatsProvider = FakeUsageStatsProvider()
        sessionManager = SessionManager()
        monitorLoop = MonitorLoop(usageStatsProvider, sessionManager)
    }

    @Test
    fun `対象アプリがフォアグラウンドの場合セッションがACTIVEになる`() {
        usageStatsProvider.foregroundPackage = "com.instagram.android"

        monitorLoop.tick(enabledApps, baseTime)

        assertEquals(SessionState.ACTIVE, sessionManager.currentSession.value?.state)
    }

    @Test
    fun `対象外アプリがフォアグラウンドの場合セッションは変わらない`() {
        usageStatsProvider.foregroundPackage = "com.twitter.android"

        monitorLoop.tick(enabledApps, baseTime)

        assertNull(sessionManager.currentSession.value)
    }

    @Test
    fun `フォアグラウンドアプリがnullの場合セッションは変わらない`() {
        usageStatsProvider.foregroundPackage = null

        monitorLoop.tick(enabledApps, baseTime)

        assertNull(sessionManager.currentSession.value)
    }

    @Test
    fun `制限時間超過後のtickでWARNINGになる`() {
        usageStatsProvider.foregroundPackage = "com.instagram.android"
        monitorLoop.tick(enabledApps, baseTime)

        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        monitorLoop.tick(enabledApps, afterLimit)

        assertEquals(SessionState.WARNING, sessionManager.currentSession.value?.state)
    }

    @Test
    fun `対象アプリがフォアグラウンドから離れた場合onTargetAppLeftが呼ばれる`() {
        usageStatsProvider.foregroundPackage = "com.instagram.android"
        monitorLoop.tick(enabledApps, baseTime)

        usageStatsProvider.foregroundPackage = "com.example.other"
        monitorLoop.tick(enabledApps, baseTime + 3000)

        // Timer doesn't stop, session stays ACTIVE
        assertEquals(SessionState.ACTIVE, sessionManager.currentSession.value?.state)
    }
}
