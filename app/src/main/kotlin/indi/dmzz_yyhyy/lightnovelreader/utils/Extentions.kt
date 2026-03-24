package indi.dmzz_yyhyy.lightnovelreader.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.TimeBarItem
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections
import java.util.zip.ZipInputStream

@Composable
fun withHaptic(action: (() -> Unit)?): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return {
        action?.let {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            it()
        }
    }
}

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

fun <T> Flow<T>.throttleLatest(periodMillis: Long): Flow<T> = flow {
    var lastTime = 0L
    var pendingValue: T? = null
    collect { value ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime >= periodMillis) {
            lastTime = currentTime
            pendingValue = null
            emit(value)
        } else {
            pendingValue = value
        }
    }

    pendingValue?.let { emit(it) }
}

@Composable
fun LazyListState.isScrollingUp(): State<Boolean> {
    val up = remember(this) { mutableStateOf(true) }

    LaunchedEffect(this) {
        var lastIndex = firstVisibleItemIndex
        var lastScroll = firstVisibleItemScrollOffset
        snapshotFlow { firstVisibleItemIndex to firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentScroll) ->
                up.value = currentIndex < lastIndex ||
                        (currentIndex == lastIndex && currentScroll < lastScroll)
                lastIndex = currentIndex
                lastScroll = currentScroll
            }
    }
    return up
}


fun NavController.popBackStackIfResumed() {
    if (isResumed()) {
        popBackStack()
    }
}

fun NavController.isResumed(): Boolean {
    return this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
}

fun quickSelect(list: List<Int>, percentile: Double): Int {
    val targetIndex = (list.size * percentile).toInt().coerceIn(list.indices)
    val arr = list.toMutableList()

    var left = 0
    var right = arr.lastIndex

    while (left < right) {
        val pivotIndex = partition(arr, left, right)
        when {
            pivotIndex == targetIndex -> return arr[pivotIndex]
            pivotIndex < targetIndex -> left = pivotIndex + 1
            else -> right = pivotIndex - 1
        }
    }
    return arr[left]
}

private fun partition(arr: MutableList<Int>, left: Int, right: Int): Int {
    val pivot = arr[right]
    var i = left
    for (j in left until right) {
        if (arr[j] <= pivot) {
            Collections.swap(arr, i, j)
            i++
        }
    }
    Collections.swap(arr, i, right)
    return i
}

suspend fun unzipFile(zipFile: File, outFile: File) {
    withContext(Dispatchers.IO) {
        ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    outFile.outputStream().buffered().use { outStream ->
                        val buffer = ByteArray(8 * 1024)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            outStream.write(buffer, 0, len)
                        }
                    }
                }
                entry = zis.nextEntry
            }
        }
    }
}

fun NavDestination?.currentMainRoute(): Any? {
    if (this == null) return null
    return hierarchy.firstNotNullOfOrNull { dest ->
        when (dest.route) {
            Route.Main.Reading.Home::class.qualifiedName -> Route.Main.Reading
            Route.Main.Bookshelf.Home::class.qualifiedName -> Route.Main.Bookshelf
            Route.Main.Explore.Home::class.qualifiedName -> Route.Main.Explore
            Route.Main.Settings.Home::class.qualifiedName -> Route.Main.Settings
            else -> null
        }
    }
}

fun LazyListScope.navigationBarSpacer() {
    item(key = "nav_spacer") {
        Spacer(
            modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)
        )
    }
}

fun LazyListScope.bottomBarSpacer() {
    item(key = "bottom_bar_spacer") {
        Spacer(
            modifier = Modifier.height(80.dp)
        )
    }
}

fun Modifier.bottomBarPadding() =
    this.padding(bottom = 80.dp)

private val FadeInOnceSpec = tween<Float>(durationMillis = 220, easing = FastOutSlowInEasing)

fun Modifier.fadeInOnce(key: Any): Modifier = composed {
    val appeared = rememberSaveable(key) { mutableStateOf(false) }
    val alphaAnim = remember { Animatable(if (appeared.value) 1f else 0f) }

    LaunchedEffect(key) {
        if (!appeared.value) {
            alphaAnim.snapTo(0f)
            alphaAnim.animateTo(1f, FadeInOnceSpec)
            appeared.value = true
        }
    }
    this.graphicsLayer { alpha = alphaAnim.value }
}

fun List<TimeBarItem>.normalize(): List<Pair<TimeBarItem, Float>> {
    val total = sumOf { it.timeSeconds }.toFloat()
    if (total <= 0f) {
        return map { it to 1f }
    }
    return map {
        it to (it.timeSeconds.toFloat() / total).coerceAtLeast(0.01f)
    }
}