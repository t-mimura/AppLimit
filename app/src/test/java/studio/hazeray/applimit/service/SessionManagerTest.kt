package studio.hazeray.applimit.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
    private val enabledApps = listOf(appA, appB)
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
    fun `制限時間超過で自動的にACTIVEからCOOLDOWNに遷移する`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1

        sessionManager.checkState(enabledApps, afterLimit)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.COOLDOWN, session?.state)
        // cooldownUntil is calculated from expiresAt (limit reached time), not from check time
        assertEquals(baseTime + 10 * 60 * 1000 + 60 * 60 * 1000, session?.cooldownUntil)
    }

    @Test
    fun `COOLDOWNから延長でACTIVEに戻る`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterLimit)

        sessionManager.extend(appA, afterLimit)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(afterLimit + 5 * 60 * 1000, session?.expiresAt)
        assertNull(session?.cooldownUntil)
        assertTrue(session?.isExtended == true)
    }

    @Test
    fun `初回検出時のセッションはisExtendedがfalse`() {
        sessionManager.onTargetAppDetected(appA, baseTime)

        assertFalse(sessionManager.getSession(appA.id)?.isExtended == true)
    }

    @Test
    fun `延長後さらに制限時間超過でCOOLDOWNに戻り新しいcooldownUntilが設定される`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterLimit)
        sessionManager.extend(appA, afterLimit)

        val afterExtension = afterLimit + 5 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterExtension)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.COOLDOWN, session?.state)
        // Fresh cooldown period based on the extended expiresAt
        assertEquals(afterLimit + 5 * 60 * 1000 + 60 * 60 * 1000, session?.cooldownUntil)
    }

    @Test
    fun `クールダウン時間経過でセッションが削除される`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterLimit)

        val afterCooldown = baseTime + 10 * 60 * 1000 + 60 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterCooldown)

        assertNull(sessionManager.getSession(appA.id))
    }

    @Test
    fun `クールダウン中に対象アプリ検出してもCOOLDOWNのまま変わらない`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterLimit)
        val cooldownUntilBefore = sessionManager.getSession(appA.id)?.cooldownUntil

        val duringCooldown = afterLimit + 1000
        sessionManager.onTargetAppDetected(appA, duringCooldown)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.COOLDOWN, session?.state)
        assertEquals(cooldownUntilBefore, session?.cooldownUntil)
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
        sessionManager.checkState(enabledApps, afterLimitA)

        val detectB = afterLimitA + 1000
        sessionManager.onTargetAppDetected(appB, detectB)

        assertEquals(SessionState.COOLDOWN, sessionManager.getSession(appA.id)?.state)
        val sessionB = sessionManager.getSession(appB.id)
        assertEquals(SessionState.ACTIVE, sessionB?.state)
        assertEquals(detectB + 15 * 60 * 1000, sessionB?.expiresAt)
    }

    @Test
    fun `Bがフォアグラウンド中でもAの制限時間経過でAはCOOLDOWNになる`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val switchToB = baseTime + 30 * 1000
        sessionManager.onTargetAppDetected(appB, switchToB)

        val afterA = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterA)

        assertEquals(SessionState.COOLDOWN, sessionManager.getSession(appA.id)?.state)
        assertEquals(SessionState.ACTIVE, sessionManager.getSession(appB.id)?.state)
    }

    @Test
    fun `checkStateは個別アプリのクールダウン完了を個別にクリアする`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        sessionManager.onTargetAppDetected(appB, baseTime)

        val afterA = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterA)

        val afterCooldownA = baseTime + 10 * 60 * 1000 + 60 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterCooldownA)

        assertNull(sessionManager.getSession(appA.id))
        assertNotNull(sessionManager.getSession(appB.id))
    }

    @Test
    fun `COOLDOWN状態ではextendでACTIVEに戻る`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1
        sessionManager.checkState(enabledApps, afterLimit)

        val duringCooldown = afterLimit + 1000
        sessionManager.extend(appA, duringCooldown)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(duringCooldown + 5 * 60 * 1000, session?.expiresAt)
        assertNull(session?.cooldownUntil)
    }

    @Test
    fun `ACTIVE状態ではextendは何もしない`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val originalExpiresAt = sessionManager.getSession(appA.id)?.expiresAt

        sessionManager.extend(appA, baseTime + 1000)

        val session = sessionManager.getSession(appA.id)
        assertEquals(SessionState.ACTIVE, session?.state)
        assertEquals(originalExpiresAt, session?.expiresAt)
        assertFalse(session?.isExtended == true)
    }

    @Test
    fun `enabledAppsに含まれないセッションはACTIVEからCOOLDOWNに遷移しない`() {
        sessionManager.onTargetAppDetected(appA, baseTime)
        val afterLimit = baseTime + 10 * 60 * 1000 + 1

        // appA is not in enabledApps (e.g., user disabled it)
        sessionManager.checkState(listOf(appB), afterLimit)

        assertEquals(SessionState.ACTIVE, sessionManager.getSession(appA.id)?.state)
    }
}
