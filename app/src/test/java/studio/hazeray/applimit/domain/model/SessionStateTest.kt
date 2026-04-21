package studio.hazeray.applimit.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SessionStateTest {
    @Test
    fun `全ての状態が定義されている`() {
        val states = SessionState.entries
        assertEquals(4, states.size)
    }

    @Test
    fun `IDLE状態が存在する`() {
        assertEquals("IDLE", SessionState.IDLE.name)
    }

    @Test
    fun `ACTIVE状態が存在する`() {
        assertEquals("ACTIVE", SessionState.ACTIVE.name)
    }

    @Test
    fun `WARNING状態が存在する`() {
        assertEquals("WARNING", SessionState.WARNING.name)
    }

    @Test
    fun `COOLDOWN状態が存在する`() {
        assertEquals("COOLDOWN", SessionState.COOLDOWN.name)
    }
}
