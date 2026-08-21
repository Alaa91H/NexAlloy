package io.github.nexalloy.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateCheckerTest {
    @Test
    fun `alpha semantic tags map to the Android version code convention`() {
        val version = VersionInfo.fromTagName("v0.1.0-alpha.4")

        assertEquals(10_004, version.versionCode)
        assertEquals("v0.1.0-alpha.4", version.versionName)
    }

    @Test
    fun `later alpha semantic tag sorts above the installed alpha`() {
        val installed = VersionInfo.fromTagName("v0.1.0-alpha.4")
        val available = VersionInfo.fromTagName("v0.1.0-alpha.5")

        assertTrue(available.versionCode > installed.versionCode)
    }

    @Test
    fun `legacy numeric tags remain supported`() {
        val version = VersionInfo.fromTagName("10005-0.1.0-alpha.5")

        assertEquals(10_005, version.versionCode)
        assertEquals("0.1.0-alpha.5", version.versionName)
    }
}
