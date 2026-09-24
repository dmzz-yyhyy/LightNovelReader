package indi.dmzz_yyhyy.lightnovelreader.utils.theme

// Copyright 2026, AsteriskMETA contributors
// SPDX-License-Identifier: GPL-3.0

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import kotlinx.coroutines.launch

private const val NavigationDuration = 400
private const val BackDuration = 360
private const val PredictiveBackDuration = 450
private val FadeEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

typealias NavSceneTransition = AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform

object Anim {

    fun fadeEnter(): EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = BackDuration,
            delayMillis = 50,
            easing = FadeEasing,
        ),
    )

    fun fadeExit(): ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = BackDuration,
            easing = FadeEasing,
        ),
    )

    fun navigationForward(): NavSceneTransition {
        val spec = navigationSpec<IntOffset>(NavigationDuration)

        return {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    animationSpec = spec,
                    initialOffsetX = { it },
                ),
                initialContentExit = slideOutHorizontally(
                    animationSpec = spec,
                    targetOffsetX = { -it / 4 },
                ),
            )
        }
    }

    // just use predictiveNavigationBack()
    @Suppress("unused")
    fun navigationBack(): NavSceneTransition {
        val spec = navigationSpec<IntOffset>(BackDuration)

        return {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    animationSpec = spec,
                    initialOffsetX = { -it / 4 },
                ),
                initialContentExit = slideOutHorizontally(
                    animationSpec = spec,
                    targetOffsetX = { it },
                ),
            )
        }
    }

    fun predictiveNavigationBack(): NavSceneTransition {
        val spec = navigationSpec<IntOffset>(PredictiveBackDuration)

        return {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    animationSpec = spec,
                    initialOffsetX = { -it / 4 },
                ),
                initialContentExit = slideOutHorizontally(
                    animationSpec = spec,
                    targetOffsetX = { it },
                ),
            )
        }
    }

    @Composable
    fun Overlay(
        isPresent: Boolean,
        predictiveBackProgress: Float?,
        cornerRadius: Dp,
        onRemoved: suspend () -> Unit,
        content: @Composable () -> Unit,
    ) {
        val offset = remember { Animatable(1f) }
        val backProgress = remember { Animatable(0f) }

        LaunchedEffect(isPresent) {
            if (isPresent) {
                offset.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
            } else {
                launch {
                    backProgress.animateTo(1f, tween(150, easing = FastOutSlowInEasing))
                }
                offset.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
                onRemoved()
            }
        }

        LaunchedEffect(predictiveBackProgress, isPresent) {
            if (!isPresent) return@LaunchedEffect
            if (predictiveBackProgress != null) {
                backProgress.snapTo(FastOutSlowInEasing.transform(predictiveBackProgress))
            } else {
                backProgress.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = size.width * (
                            offset.value + (1f - offset.value) * backProgress.value * 0.25f
                            )
                    scaleX = 1f - backProgress.value * 0.2f
                    scaleY = scaleX
                    shape = RoundedCornerShape(cornerRadius)
                    clip = true
                }
        ) {
            content()
        }
    }

    @Composable
    fun <S> destinationChange(
        indexOf: (S) -> Int,
    ): AnimatedContentTransitionScope<S>.() -> ContentTransform {
        val spatialSpec: FiniteAnimationSpec<IntOffset> =
            MaterialTheme.motionScheme.defaultSpatialSpec()

        val effectsSpec: FiniteAnimationSpec<Float> =
            MaterialTheme.motionScheme.fastEffectsSpec()

        return {
            val direction =
                indexOf(targetState).compareTo(indexOf(initialState))

            (
                    slideInHorizontally(
                        animationSpec = spatialSpec,
                        initialOffsetX = { direction * it / 8 },
                    ) + fadeIn(
                        animationSpec = effectsSpec,
                    )
                    ).togetherWith(
                    slideOutHorizontally(
                        animationSpec = spatialSpec,
                        targetOffsetX = { -direction * it / 8 },
                    ) + fadeOut(
                        animationSpec = effectsSpec,
                    )
                )
        }
    }

    private fun <T> navigationSpec(
        duration: Int,
    ): FiniteAnimationSpec<T> {
        return tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing,
        )
    }
}
