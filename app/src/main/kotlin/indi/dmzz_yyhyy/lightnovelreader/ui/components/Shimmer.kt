package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.rememberShimmer

@Composable
fun rememberSkeletonShimmer(
    baseColor: Color,
    highlightColor: Color,
    shimmerBounds: ShimmerBounds = ShimmerBounds.View
): Shimmer {
    return rememberShimmer(
        shimmerBounds = shimmerBounds,
        theme = ShimmerTheme(
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    delayMillis = 1000,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            shaderColors = listOf(baseColor, highlightColor, baseColor),
            shaderColorStops = listOf(0.0f, 0.5f, 1.0f),
            shimmerWidth = 300.dp,
            blendMode = BlendMode.SrcIn,
            rotation = 20f
        )
    )
}

@Composable
fun rememberLoadingSkeletonShimmer(
    shimmerBounds: ShimmerBounds = ShimmerBounds.View,
    baseColor: Color? = null,
    highlightColor: Color? = null
): Shimmer = rememberSkeletonShimmer(
    baseColor ?: colorScheme.surfaceContainerLow,
    highlightColor ?: colorScheme.surfaceContainerHigh,
    shimmerBounds
)
