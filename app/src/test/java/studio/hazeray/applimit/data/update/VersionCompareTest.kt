package studio.hazeray.applimit.data.update

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VersionCompareTest {

    @Test
    fun `patch bump is newer`() {
        assertTrue(isNewerVersion("1.0.1", "1.0.0"))
    }

    @Test
    fun `minor bump is newer`() {
        assertTrue(isNewerVersion("1.1.0", "1.0.9"))
    }

    @Test
    fun `major bump is newer`() {
        assertTrue(isNewerVersion("2.0.0", "1.99.99"))
    }

    @Test
    fun `equal versions are not newer`() {
        assertFalse(isNewerVersion("1.0.0", "1.0.0"))
    }

    @Test
    fun `older patch is not newer`() {
        assertFalse(isNewerVersion("1.0.0", "1.0.1"))
    }

    @Test
    fun `v prefix is stripped`() {
        assertTrue(isNewerVersion("v1.0.1", "1.0.0"))
        assertTrue(isNewerVersion("1.0.1", "v1.0.0"))
    }

    @Test
    fun `malformed candidate returns false`() {
        assertFalse(isNewerVersion("not-a-version", "1.0.0"))
    }

    @Test
    fun `malformed current returns false`() {
        assertFalse(isNewerVersion("2.0.0", "x.y.z"))
    }

    @Test
    fun `two-part version is rejected`() {
        assertFalse(isNewerVersion("1.0", "1.0.0"))
    }
}
