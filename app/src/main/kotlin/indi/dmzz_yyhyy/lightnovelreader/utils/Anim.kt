@file:Suppress("unused")

package indi.dmzz_yyhyy.lightnovelreader.utils

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import kotlin.math.roundToInt

val cubicBezierEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
const val animDuration = 360

fun expandEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(
            durationMillis = animDuration,
            easing = cubicBezierEasing
        ),
        initialOffsetX = { (0.1f * it).roundToInt() }
    ) + fadeIn(
        animationSpec = tween(
            delayMillis = (0.35f * animDuration).roundToInt(),
            durationMillis = (0.45f * animDuration).roundToInt(),
            easing = LinearEasing
        )
    )

fun expandExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(
            durationMillis = animDuration,
            easing = cubicBezierEasing
        ),
        targetOffsetX = { (-0.1f * it).roundToInt() }
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = (0.35f * animDuration).roundToInt(),
            easing = LinearEasing
        )
    )

fun expandPopEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(
            durationMillis = animDuration,
            easing = cubicBezierEasing
        ),
        initialOffsetX = { (-0.1f * it).roundToInt() }
    ) + fadeIn(
        animationSpec = tween(
            delayMillis = (0.35f * animDuration).roundToInt(),
            durationMillis = (0.45f * animDuration).roundToInt(),
            easing = LinearEasing
        )
    )

fun expandPopExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(
            durationMillis = animDuration,
            easing = cubicBezierEasing
        ),
        targetOffsetX = { (0.1f * it).roundToInt() }
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = (0.35f * animDuration).roundToInt(),
            easing = LinearEasing
        )
    )


fun fadeEnter(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 50,
            easing = cubicBezierEasing
        )
    )

fun fadeExit(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = animDuration,
            easing = cubicBezierEasing
        )
    )

fun fadePopEnter(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 50,
            easing = cubicBezierEasing
        )
    )

fun fadePopExit(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = 360,
            easing = cubicBezierEasing
        )
    )