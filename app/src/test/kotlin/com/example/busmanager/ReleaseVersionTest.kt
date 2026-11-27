package com.example.busmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseVersionTest {

    @Test
    fun releaseVersion_matchesTargetReleaseSpecifications() {
        assertEquals(28, BuildConfig.VERSION_CODE)
        assertEquals("2.20.0", BuildConfig.VERSION_NAME)
        assertTrue(BuildConfig.VERSION_NAME.startsWith("2."))
    }
}
