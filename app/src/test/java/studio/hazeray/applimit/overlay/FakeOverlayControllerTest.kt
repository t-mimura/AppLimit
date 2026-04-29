package studio.hazeray.applimit.overlay

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.hazeray.applimit.service.fake.FakeOverlayController

class FakeOverlayControllerTest {

    private lateinit var controller: FakeOverlayController

    @BeforeEach
    fun setup() {
        controller = FakeOverlayController()
    }

    @Test
    fun `初期状態では表示されていない`() {
        assertFalse(controller.isShowing())
    }

    @Test
    fun `クールダウンオーバーレイを表示すると表示状態になる`() {
        controller.showCooldownOverlay("Instagram", 30, {}, {})
        assertTrue(controller.isShowing())
        assertEquals("Instagram", controller.lastAppName)
    }

    @Test
    fun `非表示にすると表示状態が解除される`() {
        controller.showCooldownOverlay("Instagram", 30, {}, {})
        controller.hideOverlay()
        assertFalse(controller.isShowing())
    }

    @Test
    fun `コールバックが保持され呼び出し可能`() {
        var extended = false
        var dismissed = false

        controller.showCooldownOverlay("Instagram", 30, { extended = true }, { dismissed = true })
        controller.lastOnExtend?.invoke()
        controller.lastOnDismiss?.invoke()

        assertTrue(extended)
        assertTrue(dismissed)
    }
}
