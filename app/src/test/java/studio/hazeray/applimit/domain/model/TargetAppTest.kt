package studio.hazeray.applimit.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TargetAppTest {
    @Test
    fun `デフォルト値が仕様通りに設定される`() {
        val app = TargetApp(
            packageName = "com.instagram.android",
            appName = "Instagram"
        )

        assertEquals(0L, app.id)
        assertEquals("com.instagram.android", app.packageName)
        assertEquals("Instagram", app.appName)
        assertEquals(15, app.limitMinutes)
        assertEquals(120, app.cooldownMinutes)
        assertEquals(5, app.extensionMinutes)
        assertTrue(app.isEnabled)
    }

    @Test
    fun `カスタム値を指定できる`() {
        val app = TargetApp(
            id = 1L,
            packageName = "com.twitter.android",
            appName = "Twitter",
            limitMinutes = 30,
            cooldownMinutes = 60,
            extensionMinutes = 10,
            isEnabled = false
        )

        assertEquals(1L, app.id)
        assertEquals(30, app.limitMinutes)
        assertEquals(60, app.cooldownMinutes)
        assertEquals(10, app.extensionMinutes)
        assertEquals(false, app.isEnabled)
    }
}
