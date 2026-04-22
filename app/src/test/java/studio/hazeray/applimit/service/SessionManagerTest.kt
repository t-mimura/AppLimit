package studio.hazeray.applimit.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.hazeray.applimit.domain.model.SessionState
import studio.hazeray.applimit.domain.model.TargetApp

class SessionManagerTest {

    private lateinit var sessionManager: SessionManager
    private val appA = TargetApp(
        id = 1L,
        packageName = "com.instagram.android",
        appName = "Instagram",
        limitMinutes = 10,
        cooldownMinutes = 60,
        extensionMinutes = 5
    )
    private val appB = TargetApp(
        id = 2L,
        packageName = "com.twitter.android",
        appName = "Twitter",
        limitMinutes = 15,
        cooldownMinutes = 120,
        extensionMinutes = 5
    )
    private val baseTime = 1_000_000L

    @BeforeEach
    fun setup() {
        sessionManager = SessionManager()
    }

    @Test
    fun `初期状態は空のマップ`() {
        assertTrue(sessionManager.sessions.value.isEmpty())
        assertNull(sessionManager.getSession(appA.id))
    }

    @Test
    fun `対象アプリ検出でIDLEからACTIVEに遷移する`() {
        sessionManager.onTargetAppDetected(appA, baseTime)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(appA.id, session?.targetAppId)
        assertEquals(baseTime, session?.startedAt)
        assertEquals(baseTime + 10 * 60 * 1000, session?.expiresAt)
    }

    @Test
    fun `制限時間超過でACTIVEからWARNINGに遷移する`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1

        sessionManager.checkState(afterLimit)

        assertEquals(SessionState.WARNING, sessionManager.getSession(appA.id)?.state)
    }

    @Test
    fun `延長でWARNINGからACTIVEに戻る`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)

        sessionManager.extend(appA, afterLimit)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(afterLimit + 5 * 60 * 1000, session?.expiresAt)
    }

    @Test
    fun `閉じるでWARNINGからCOOLDOWNに遷移する`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)

        sessionManager.dismiss(appA, afterLimit)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.COOLDOWN, session?.state)
        assertEquals(afterLimit + 60 * 60 * 1000, session?.cooldownUntil)
    }

    @Test
    fun `クールダウン時間経過でCOOLDOWNからIDLEに遷移する`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)
        sessionManager.dismiss(appA, afterLimit)

        val afterCooldown = afterLimit + 60 * 60 * 1000 + 1
        sessionManager.checkState(afterCooldown)

        assertNull(sessionManager.getSession(appA.id))
    }

    @Test
    fun `クールダウン中に対象アプリ検出してもCOOLDOWNのまま変わらない`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)
        sessionManager.dismiss(appA, afterLimit)
        val cooldownUntilBefore = sessionManager.getSession(appA.id)?.cooldownUntil

        val duringCooldown = afterLimit + 1000
        sessionManager.onTargetAppDetected(appA, duringCooldown)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.COOLDOWN, session?.state)
        assertEquals(cooldownUntilBefore, session?.cooldownUntil)
    }

    @Test
    fun `COOLDOWNから延長でACTIVEに戻る`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimit)
        sessionManager.dismiss(appA, afterLimit)

        val duringCooldown = afterLimit + 1000
        sessionManager.extend(appA, duringCooldown)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(duringCooldown + 5 * 60 * 1000, session?.expiresAt)
        assertNull(session?.cooldownUntil)
    }

    @Test
    fun `ACTIVE中にonTargetAppLeftを呼んでもタイマーは止まらない`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val leaveTime = baseTime + 5 * 60 * 1000
        sessionManager.onTargetAppLeft(leaveTime)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(baseTime + 10 * 60 * 1000, session?.expiresAt)
    }

    @Test
    fun `resetですべてのセッションが空になる`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        sessionManager.onTargetAppDetected(appB, baseTime)
        sessionManager.reset()

        assertTrue(sessionManager.sessions.value.isEmpty())
    }

    @Test
    fun `ACTIVE中に同じアプリを再検出しても状態は変わらない`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val originalExpiresAt = sessionManager.getSession(appA.id)?.expiresAt

        sessionManager.onTargetAppDetected(appA, baseTime + 3000)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(originalExpiresAt, session?.expiresAt)
    }

    @Test
    fun `AがACTIVE中にBを検出するとBが独立してACTIVEになる`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val switchTime = baseTime + 30 * 1000

        sessionManager.onTargetAppDetected(appB, switchTime)

        val sessionA = sessionManager.getSession(appA.id)
        val sessionB = sessionManager.getSession(appB.id)
        assertEquals(SessionState.ACTIVE, sessionA?.state)
        assertEquals(baseTime + 10 * 60 * 1000, sessionA?.expiresAt)
        assertEquals(SessionState.ACTIVE, sessionB?.state)
        assertEquals(switchTime, sessionB?.startedAt)
        assertEquals(switchTime + 15 * 60 * 1000, sessionB?.expiresAt)
    }

    @Test
    fun `AがCOOLDOWN中でもBは独立してACTIVEになる`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimitA = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterLimitA)
        sessionManager.dismiss(appA, afterLimitA)

        val detectB = afterLimitA + 1000
        sessionManager.onTargetAppDetected(appB, detectB)

        assertEquals(SessionState.COOLDOWN, sessionManager.getSession(appA.id)?.state)
        val sessionB = sessionManager.getSession(appB.id)
        assertEquals(SessionState.ACTIVE, sessionB?.state)
        assertEquals(detectB + 15 * 60 * 1000, sessionB?.expiresAt)
    }

    @Test
    fun `Bがフォアグラウンド中でもAの制限時間経過でAはWARNINGになる`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val switchToB = baseTime + 30 * 1000
        sessionManager.onTargetAppDetected(appB, switchToB)

        val afterA = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterA)

        assertEquals(SessionState.WARNING, sessionManager.getSession(appA.id)?.state)
        assertEquals(SessionState.ACTIVE, sessionManager.getSession(appB.id)?.state)
    }

    @Test
    fun `checkStateは個別アプリのクールダウン完了を個別にクリアする`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        sessionManager.onTargetAppDetected(appB, baseTime)

        val afterA = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterA)
        sessionManager.dismiss(appA, afterA)

        val afterCooldownA = afterA + 60 * 60 * 1000 + 1
        sessionManager.checkState(afterCooldownA)

        assertNull(sessionManager.getSession(appA.id))
        assertNotNull(sessionManager.getSession(appB.id))
    }

    @Test
    fun `片方のextendは他方のセッションに影響しない`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        sessionManager.onTargetAppDetected(appB, baseTime)
        val afterA = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(afterA)

        sessionManager.extend(appA, afterA)

        val sessionB = sessionManager.getSession(appB.id)
        assertEquals(SessionState.ACTIVE, sessionB?.state)
        assertEquals(baseTime + 15 * 60 * 1000, sessionB?.expiresAt)
    }
}
