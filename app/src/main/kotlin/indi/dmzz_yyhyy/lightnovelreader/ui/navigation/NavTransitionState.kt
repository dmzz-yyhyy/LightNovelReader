package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.NavigationEventTransitionState.InProgress
import kotlinx.coroutines.launch

internal enum class NavigationPath {
    Forward,
    Back,
}

internal class NavTransitionState(
    val transition: Transition<Scene<NavKey>>,
    val path: NavigationPath,
    val targetZIndex: Float,
    val foregroundKey: Any,
) {
    val isRunning: Boolean
        get() = transition.currentState != transition.targetState

}

private data class InterruptedForward(
    val target: Scene<NavKey>,
    val fraction: Float,
    val gestureProgress: Float,
)

@Composable
internal fun rememberNavTransitionState(
    scene: Scene<NavKey>,
    entries: List<NavEntry<NavKey>>,
    previousScenes: List<Scene<NavKey>>,
    hasOverlay: Boolean,
    gesture: NavigationEventTransitionState,
): NavTransitionState {
    val transitionState = remember {
        SeekableTransitionState(scene)
    }
    val transition = rememberTransition(
        transitionState = transitionState,
        label = "navigation scene",
    )

    val entryKeys = entries.map { it.contentKey }
    val previousEntries = remember {
        mutableStateOf(entryKeys)
    }
    val stackPath = remember(entryKeys) {
        val result = if (isPop(previousEntries.value, entryKeys)) {
            NavigationPath.Back
        } else {
            NavigationPath.Forward
        }
        previousEntries.value = entryKeys
        result
    }

    val settlingPath = remember {
        mutableStateOf<NavigationPath?>(null)
    }

    val gestureInProgress = gesture is InProgress
    val predictiveBack =
        !hasOverlay &&
                gestureInProgress &&
                previousScenes.isNotEmpty()

    val interruptedForward = remember(predictiveBack) {
        if (
            gesture is InProgress &&
            stackPath == NavigationPath.Forward &&
            previousScenes.isNotEmpty() &&
            transition.currentState != transition.targetState &&
            transition.targetState == scene
        ) {
            InterruptedForward(
                target = transition.targetState,
                fraction = transitionState.fraction,
                gestureProgress = gesture.latestEvent.progress,
            )
        } else {
            null
        }
    }

    val gesturePath = if (interruptedForward != null) {
        NavigationPath.Forward
    } else {
        NavigationPath.Back
    }

    if (predictiveBack) {
        SideEffect {
            settlingPath.value = gesturePath
        }
    }

    val path = if (predictiveBack) {
        gesturePath
    } else {
        settlingPath.value ?: stackPath
    }

    if (gesture is InProgress && predictiveBack) {
        val previousScene = previousScenes.last()

        LaunchedEffect(gesture.latestEvent, interruptedForward) {
            if (interruptedForward != null) {
                transitionState.seekTo(
                    fraction = interruptedForwardFraction(
                        startFraction = interruptedForward.fraction,
                        startGestureProgress = interruptedForward.gestureProgress,
                        backProgress = gesture.latestEvent.progress,
                    ),
                    targetState = interruptedForward.target,
                )
            } else {
                transitionState.seekTo(
                    fraction = gesture.latestEvent.progress,
                    targetState = previousScene,
                )
            }
        }
    } else {
        LaunchedEffect(scene) {
            when {
                transitionState.currentState != scene -> {
                    transitionState.animateTo(scene)
                }

                transitionState.targetState != scene -> {
                    val duration = (transitionState.fraction * (transition.totalDurationNanos / 1_000_000))
                        .toInt()

                    animate(
                        initialValue = transitionState.fraction,
                        targetValue = 0f,
                        animationSpec = tween(duration),
                    ) { value, _ ->
                        this@LaunchedEffect.launch {
                            if (value > 0f) {
                                transitionState.seekTo(value)
                            }
                        }
                    }

                    transitionState.snapTo(scene)
                }
            }

            settlingPath.value = null
        }
    }

    val zIndices = remember {
        mutableStateMapOf<Any, Float>()
    }

    val initialKey = transition.currentState.key
    val targetKey = transition.targetState.key

    val initialZ = zIndices.getOrPut(initialKey) { 0f }

    val targetZ = when {
        !predictiveBack && transition.targetState != scene && zIndices.containsKey(targetKey) -> {
            zIndices.getValue(targetKey)
        }
        initialKey == targetKey -> initialZ
        path == NavigationPath.Back -> initialZ - 1f

        else -> initialZ + 1f
    }

    zIndices[targetKey] = targetZ

    val foregroundKey = if (initialZ >= targetZ) {
        initialKey
    } else {
        targetKey
    }

    LaunchedEffect(
        transition.currentState,
        transition.targetState,
    ) {
        if (!transition.isRunning) {
            zIndices.keys.retainAll(
                setOf(transition.targetState.key)
            )
        }
    }

    return NavTransitionState(
        transition = transition,
        path = path,
        targetZIndex = targetZ,
        foregroundKey = foregroundKey
    )
}

private fun interruptedForwardFraction(
    startFraction: Float,
    startGestureProgress: Float,
    backProgress: Float,
): Float {
    val remainingGesture = 1f - startGestureProgress

    val progress = if (remainingGesture > 0f) {
        ((backProgress - startGestureProgress) / remainingGesture).coerceIn(0f, 1f)
    } else {
        1f
    }

    return startFraction * (1f - progress)
}

private fun isPop(
    oldEntries: List<Any>,
    newEntries: List<Any>,
): Boolean {
    if (oldEntries.firstOrNull() != newEntries.firstOrNull()) return false
    if (newEntries.size >= oldEntries.size) return false

    return oldEntries.take(newEntries.size) == newEntries
}
