package com.example.busmanager.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppTransitionsTest {

    @Test
    fun defaultTransitionDuration_isFiveHundredMilliseconds() {
        assertEquals(500, AppTransitions.TRANSITION_DURATION_MS)
    }

    @Test
    fun fadeInSpec_createsNonNullTransition() {
        val enterTransition = AppTransitions.fadeInSpec()
        assertNotNull(enterTransition)
    }

    @Test
    fun fadeOutSpec_createsNonNullTransition() {
        val exitTransition = AppTransitions.fadeOutSpec()
        assertNotNull(exitTransition)
    }

    @Test
    fun contentTransform_combinesFadeInAndFadeOut() {
        val transform = AppTransitions.contentTransform()
        assertNotNull(transform)
    }

    @Test
    fun fadeTweenSpec_createsNonNullSpec() {
        val spec = AppTransitions.fadeTweenSpec<Float>()
        assertNotNull(spec)
    }
}
