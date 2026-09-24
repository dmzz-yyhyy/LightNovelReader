package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigationevent.NavigationEventTransitionState.InProgress
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.utils.theme.Anim

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun InterruptibleNavDisplay(
    entries: List<NavEntry<NavKey>>,
    modifier: Modifier = Modifier,
    sceneStrategies: List<SceneStrategy<NavKey>>,
    sharedTransitionScope: SharedTransitionScope,
    pageCornerRadius: Dp,
    onBack: () -> Unit
) {
    val navigator = LocalNavigator.current
    val forwardTransition = Anim.navigationForward()
    val predictiveBackTransition = Anim.predictiveNavigationBack()

    val sceneState = rememberSceneState(
        entries = entries,
        sceneStrategies = sceneStrategies,
        sharedTransitionScope = sharedTransitionScope,
        onBack = onBack,
    )

    val scene = sceneState.currentScene
    val hasOverlay = sceneState.overlayScenes.isNotEmpty()

    val navigationEventState = rememberNavigationEventState(
        currentInfo = SceneInfo(scene),
        backInfo = sceneState.previousScenes.map(::SceneInfo),
    )

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled =
            hasOverlay || scene.previousEntries.isNotEmpty(),
        onBackCompleted = {
            repeat(
                if (hasOverlay) {
                    1
                } else {
                    entries.size - scene.previousEntries.size
                }
            ) {
                onBack()
            }
        },
    )

    val navTransitionState = rememberNavTransitionState(
        scene = scene,
        entries = sceneState.entries,
        previousScenes = sceneState.previousScenes,
        hasOverlay = hasOverlay,
        gesture = navigationEventState.transitionState,
    )

    val currentOverlays = remember {
        mutableStateListOf<OverlayScene<NavKey>>()
    }

    LaunchedEffect(sceneState.overlayScenes) {
        sceneState.overlayScenes.forEach { overlay ->
            if (currentOverlays.none { it.key == overlay.key }) {
                currentOverlays += overlay
            }
        }
    }

    navTransitionState.transition.AnimatedContent(
        modifier = modifier,
        contentAlignment = Alignment.TopStart,
        contentKey = { it.key },
        transitionSpec = {
            val transform = if (navTransitionState.path == NavigationPath.Forward) {
                forwardTransition()
            } else {
                predictiveBackTransition()
            }

            ContentTransform(
                targetContentEnter = transform.targetContentEnter,
                initialContentExit = transform.initialContentExit,
                targetContentZIndex = navTransitionState.targetZIndex,
                sizeTransform = transform.sizeTransform,
            )
        },
    ) { targetScene ->

        val isForeground = targetScene.key == navTransitionState.foregroundKey

        val scrimAlpha by transition.animateFloat(
            transitionSpec = {
                val darkening = initialState == EnterExitState.Visible && targetState == EnterExitState.PostExit

                tween(
                    durationMillis = if (darkening) 300 else 260,
                    easing = if (darkening) {
                        CubicBezierEasing(0.70f, 0.20f, 0.80f, 0.50f)
                    } else {
                        CubicBezierEasing(0.00f, 0.00f, 0.58f, 1.00f)
                    }
                )
            },
            label = "background scrim",
        ) { state ->
            if (state == EnterExitState.Visible) 0f else 0.38f
        }

        val lifecycleOwner = rememberLifecycleOwner(
            maxLifecycle = if (!navTransitionState.isRunning && currentOverlays.isEmpty()) {
                Lifecycle.State.RESUMED
            } else {
                Lifecycle.State.STARTED
            }
        )
        val sceneNavigator = remember(navigator, lifecycleOwner) {
            navigator.withNavigatePermission {
                lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            }
        }

        CompositionLocalProvider(
            LocalLifecycleOwner provides lifecycleOwner,
            LocalNavAnimatedContentScope provides this,
            LocalNavigator provides sceneNavigator,
        ) {
            val clipModifier = if (navTransitionState.isRunning && isForeground) {
                Modifier.graphicsLayer {
                    shape = RoundedCornerShape(pageCornerRadius)
                    clip = true
                }
            } else Modifier

            Box(modifier = Modifier.fillMaxSize()
                .then(clipModifier)
                .background(MaterialTheme.colorScheme.background),
            ) {
                targetScene.content()

                if (navTransitionState.isRunning && !isForeground) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(
                                    alpha = scrimAlpha
                                )
                            )
                    )
                }
            }
        }
    }

    currentOverlays
        .asReversed()
        .forEach { overlay ->
            key(overlay) {
                val isPresent = overlay in sceneState.overlayScenes
                val gesture = navigationEventState.transitionState
                val predictiveBackProgress = if (
                    isPresent &&
                    sceneState.overlayScenes.firstOrNull() == overlay &&
                    gesture is InProgress
                ) {
                    gesture.latestEvent.progress
                } else {
                    null
                }

                Anim.Overlay(
                    isPresent = isPresent,
                    predictiveBackProgress = predictiveBackProgress,
                    cornerRadius = pageCornerRadius,
                    onRemoved = {
                        overlay.onRemove()
                        currentOverlays.remove(overlay)
                    },
                ) {
                    overlay.content()
                }
            }
        }
}
