package com.example.busmanager.presentation.common

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

object AppTransitions {
    const val TRANSITION_DURATION_MS = 500

    fun fadeInSpec(durationMillis: Int = TRANSITION_DURATION_MS): EnterTransition {
        return fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = LinearOutSlowInEasing
            )
        )
    }

    fun fadeOutSpec(durationMillis: Int = TRANSITION_DURATION_MS): ExitTransition {
        return fadeOut(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutLinearInEasing
            )
        )
    }

    fun contentTransform(durationMillis: Int = TRANSITION_DURATION_MS): ContentTransform {
        return fadeInSpec(durationMillis) togetherWith fadeOutSpec(durationMillis)
    }

    fun <T> fadeTweenSpec(durationMillis: Int = TRANSITION_DURATION_MS): TweenSpec<T> {
        return tween(
            durationMillis = durationMillis,
            easing = LinearOutSlowInEasing
        )
    }
}
