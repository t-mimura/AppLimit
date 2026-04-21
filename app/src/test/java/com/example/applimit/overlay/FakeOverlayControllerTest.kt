package com.example.applimit.overlay

import com.example.applimit.service.fake.FakeOverlayController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    fun `警告オーバーレイを表示すると表示状態になる`() {
        controller.showWarningOverlay("Instagram", 10, {}, {})
        assertTrue(controller.isShowing())
        assertEquals("Instagram", controller.lastAppName)
    }

    @Test
    fun `クールダウンオーバーレイを表示すると表示状態になる`() {
        controller.showCooldownOverlay("Instagram", 30, {}, {})
        assertTrue(controller.isShowing())
    }

    @Test
    fun `非表示にすると表示状態が解除される`() {
        controller.showWarningOverlay("Instagram", 10, {}, {})
        controller.hideOverlay()
        assertFalse(controller.isShowing())
    }

    @Test
    fun `コールバックが保持され呼び出し可能`() {
        var extended = false
        var dismissed = false

        controller.showWarningOverlay("Instagram", 10, { extended = true }, { dismissed = true })
        controller.lastOnExtend?.invoke()
        controller.lastOnDismiss?.invoke()

        assertTrue(extended)
        assertTrue(dismissed)
    }
}
