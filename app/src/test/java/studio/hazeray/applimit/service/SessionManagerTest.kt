package studio.hazeray.applimit.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.hazeray.applimit.domain.model.SessionState
import studio.hazeray.applimit.domain.model.TargetApp

class SessionManagerTest {

    private lateinit var sessionManager: SessionManager
    private val targetApp = TargetApp(
        id = 1L,
        packageName = "com.instagram.android",
        appName = "Instagram",
        limitMinutes = 10,
        cooldownMinutes = 60,
        extensionMinutes = 5
    )
    private val baseTime = 1_000_000L

    @BeforeEach
    fun setup() {
        sessionManager = SessionManager()
    }

    @Test
    fun `初期状態はnull`() {
        assertNull(sessionManager.currentSession.value)
    }

    @Test
    fun `対象アプリ検出でIDLEからACTIVEに遷移する`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)

        val session = sessionManager.currentSession.value
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(targetApp.id, session?.targetAppId)
        assertEquals(baseTime, session?.startedAt)
        assertEquals(baseTime + 10 * 60 * 1000, session?.expiresAt)
    }

    @Test
    fun `制限時間超過でACTIVEからWARNINGに遷移する`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1

        val session = sessionManager.checkState(afterLimit)

        assertEquals(SessionState.WARNING, session?.state)
    }

    @Test
    fun `延長でWARNINGからACTIVEに戻る`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)

        sessionManager.extend(targetApp, afterLimit)

        val session = sessionManager.currentSession.value
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(afterLimit + 5 * 60 * 1000, session?.expiresAt)
    }

    @Test
    fun `閉じるでWARNINGからCOOLDOWNに遷移する`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)

        sessionManager.dismiss(targetApp, afterLimit)

        val session = sessionManager.currentSession.value
        assertEquals(SessionState.COOLDOWN, session?.state)
        assertEquals(afterLimit + 60 * 60 * 1000, session?.cooldownUntil)
    }

    @Test
    fun `クールダウン時間経過でCOOLDOWNからIDLEに遷移する`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)
        sessionManager.dismiss(targetApp, afterLimit)

        val afterCooldown = afterLimit + 60 * 60 * 1000 + 1
        val session = sessionManager.checkState(afterCooldown)

        assertNull(session)
    }

    @Test
    fun `クールダウン中に対象アプリ検出してもCOOLDOWNのまま変わらない`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)
        sessionManager.dismiss(targetApp, afterLimit)
        val cooldownUntilBefore = sessionManager.currentSession.value?.cooldownUntil

        val duringCooldown = afterLimit + 1000
        sessionManager.onTargetAppDetected(targetApp, duringCooldown)

        val session = sessionManager.currentSession.value
        assertEquals(SessionState.COOLDOWN, session?.state)
        assertEquals(cooldownUntilBefore, session?.cooldownUntil)
    }

    @Test
    fun `ACTIVE中にonTargetAppLeftを呼んでもタイマーは止まらない`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)
        val leaveTime = baseTime + 5 * 60 * 1000
        sessionManager.onTargetAppLeft(leaveTime)

        val session = sessionManager.currentSession.value
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(baseTime + 10 * 60 * 1000, session?.expiresAt)
    }

    @Test
    fun `resetでセッションがnullに戻る`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)
        sessionManager.reset()

        assertNull(sessionManager.currentSession.value)
    }

    @Test
    fun `ACTIVE中に同じアプリを再検出しても状態は変わらない`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)
        val originalExpiresAt = sessionManager.currentSession.value?.expiresAt

        sessionManager.onTargetAppDetected(targetApp, baseTime + 3000)

        val session = sessionManager.currentSession.value
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(originalExpiresAt, session?.expiresAt)
    }

    @Test
    fun `COOLDOWNから延長でACTIVEに戻る`() {
        sessionManager.onTargetAppDetected(targetApp, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)
        sessionManager.dismiss(targetApp, afterLimit)

        val duringCooldown = afterLimit + 1000
        sessionManager.extend(targetApp, duringCooldown)

        val session = sessionManager.currentSession.value
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(duringCooldown + 5 * 60 * 1000, session?.expiresAt)
        assertNull(session?.cooldownUntil)
    }
}
