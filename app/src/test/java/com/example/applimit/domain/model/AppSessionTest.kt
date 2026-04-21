package com.example.applimit.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AppSessionTest {
    @Test
    fun `セッションを生成できる`() {
        val session = AppSession(
            targetAppId = 1L,
            state = SessionState.ACTIVE,
            startedAt = 1000L,
            expiresAt = 2000L,
            cooldownUntil = null
        )

        assertEquals(1L, session.targetAppId)
        assertEquals(SessionState.ACTIVE, session.state)
        assertEquals(1000L, session.startedAt)
        assertEquals(2000L, session.expiresAt)
        assertNull(session.cooldownUntil)
    }

    @Test
    fun `クールダウン中のセッションを生成できる`() {
        val session = AppSession(
            targetAppId = 2L,
            state = SessionState.COOLDOWN,
            startedAt = 1000L,
            expiresAt = 2000L,
            cooldownUntil = 5000L
        )

        assertEquals(SessionState.COOLDOWN, session.state)
        assertEquals(5000L, session.cooldownUntil)
    }
}
