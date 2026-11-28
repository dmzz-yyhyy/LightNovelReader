package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

private val BarMinWidth = 14.dp
private val BarMaxWidth = 24.dp
private val BarMinGap = 10.dp
private val LabelMinSlot = 20.dp
private val ChartHeight = 200.dp

private fun niceMax(value: Int): Int {
    val raw = value * 1.05f / 5f
    val scale = 10f.pow(floor(log10(raw.coerceAtLeast(1f))))
    val step = listOf(1, 2, 3, 5, 10)
        .first { it * scale >= raw }

    return (step * scale).roundToInt() * 5
}

@Immutable
data class BarChartEntry(
    val label: String,
    val value: Int,
    val color: Color,
    val isPlaceholder: Boolean = false
)

@Immutable
data class BarChartTooltip(
    val title: String,
    val value: String
)

@Composable
fun BarChart(
    bars: List<BarChartEntry>,
    selectedIndex: Int,
    average: Float,
    onTap: (Int) -> Unit,
    formatAxisLabel: @Composable (Int) -> String,
    modifier: Modifier = Modifier,
    tooltip: BarChartTooltip? = null,
    zeroColor: Color = colorScheme.onSurface.copy(alpha = 0.08f)
) {
    if (bars.isEmpty()) return

    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val axisStyle = typography.labelSmall.copy(color = colorScheme.outline)
    val xStyle = typography.labelSmall.copy(color = colorScheme.onSurfaceVariant)
    val xStyleSelected = xStyle.copy(color = colorScheme.onSurface, fontWeight = FontWeight.W600)
    val tipStyle = typography.labelSmall.copy(color = colorScheme.inverseOnSurface)
    val grid = colorScheme.outlineVariant
    val inverse = colorScheme.inverseSurface
    val highlight = colorScheme.onSurface.copy(alpha = 0.06f)
    val averageColor = colorScheme.tertiary

    val count = bars.size
    val maxValue = niceMax(max(bars.maxOf { it.value }, 1))
    val axisLabels = (0..5).map { formatAxisLabel(maxValue / 5 * it) }

    val axisWidth = with(density) {
        axisLabels.maxOf { measurer.measure(it, axisStyle).size.width } + 8.dp.toPx()
    }
    val currentOnTap by rememberUpdatedState(onTap)

    BoxWithConstraints(modifier.fillMaxWidth().height(ChartHeight)) {
        val topPadding = with(density) { 28.dp.toPx() }
        val bottomPadding = with(density) { 22.dp.toPx() }
        val plotWidth = constraints.maxWidth - axisWidth
        val plotHeight = constraints.maxHeight - topPadding - bottomPadding
        val baseY = topPadding + plotHeight

        val minBar = with(density) { BarMinWidth.toPx() }
        val maxBar = with(density) { BarMaxWidth.toPx() }
        val minSlot = minBar + with(density) { BarMinGap.toPx() }
        val slot = max(plotWidth / count, minSlot)
        val barWidth = (slot * 0.62f).coerceIn(minBar, maxBar)
        val contentWidth = slot * count
        val scrollable = contentWidth > plotWidth + 0.5f
        val showAllLabels = count <= 12 || slot >= with(density) { LabelMinSlot.toPx() }

        val scrollState = rememberScrollState()
        LaunchedEffect(bars, selectedIndex, scrollable, slot, plotWidth, scrollState.maxValue) {
            if (!scrollable || scrollState.maxValue == Int.MAX_VALUE) return@LaunchedEffect

            val focus = selectedIndex.takeIf { it in bars.indices }
                ?: bars.indexOfLast { !it.isPlaceholder }.coerceAtLeast(0)

            val target = (slot * (focus + 0.5f) - plotWidth / 2)
                .roundToInt()
                .coerceIn(0, scrollState.maxValue)

            scrollState.animateScrollTo(target, tween(200))
        }

        fun indexAt(x: Float) = (x / slot).toInt().coerceIn(0, count - 1)

        Canvas(Modifier.fillMaxSize()) {
            axisLabels.forEachIndexed { index, label ->
                val fraction = index / 5f
                val y = baseY - plotHeight * fraction
                if (index > 0) {
                    drawLine(
                        grid.copy(alpha = 0.45f),
                        Offset(0f, y), Offset(plotWidth, y), strokeWidth = 1.dp.toPx()
                    )
                }
                val text = measurer.measure(label, axisStyle)
                drawText(text, topLeft = Offset(plotWidth + 8.dp.toPx(), y - text.size.height / 2f))
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(end = with(density) { axisWidth.toDp() })
                .horizontalScroll(scrollState, enabled = scrollable)
        ) {
            Canvas(
                Modifier
                    .width(with(density) { contentWidth.toDp() })
                    .fillMaxHeight()
                    .pointerInput(bars, slot) {
                        detectTapGestures { currentOnTap(indexAt(it.x)) }
                    }
            ) {
                if (selectedIndex in 0 until count) {
                    drawRoundRect(
                        highlight,
                        Offset(slot * selectedIndex + 1.dp.toPx(), topPadding),
                        Size(slot - 2.dp.toPx(), plotHeight),
                        CornerRadius(6.dp.toPx())
                    )
                }

                var selectedTop = baseY
                bars.forEachIndexed { i, bar ->
                    val centerX = slot * (i + 0.5f)
                    val x = centerX - barWidth / 2
                    val alpha = if (selectedIndex < 0 || i == selectedIndex) 1f else 0.5f
                    when {
                        bar.isPlaceholder -> {
                            val r = 1.dp.toPx()
                            drawCircle(grid, r, Offset(centerX, baseY - r))
                        }

                        bar.value == 0 -> {
                            val lineHeight = 1.dp.toPx()
                            drawRoundRect(
                                zeroColor.copy(alpha = alpha),
                                Offset(x, baseY - lineHeight),
                                Size(barWidth, lineHeight),
                                CornerRadius(lineHeight / 2)
                            )
                        }

                        else -> {
                            val h = plotHeight * bar.value / maxValue
                            drawRoundRect(
                                bar.color.copy(alpha = alpha),
                                Offset(x, baseY - h),
                                Size(barWidth, h),
                                CornerRadius(min(barWidth, h) / 2)
                            )
                            if (i == selectedIndex) selectedTop = baseY - h
                        }
                    }

                    if (showAllLabels || i == 0 || (i + 1) % 5 == 0) {
                        val text = measurer.measure(bar.label, if (i == selectedIndex) xStyleSelected else xStyle)
                        drawText(text, topLeft = Offset(centerX - text.size.width / 2f, baseY + 5.dp.toPx()))
                    }
                }

                if (tooltip != null && selectedIndex in bars.indices) {
                    val title = measurer.measure(tooltip.title, tipStyle)
                    val value = measurer.measure(tooltip.value, tipStyle.copy(fontWeight = FontWeight.W600))
                    val paddingH = 8.dp.toPx()
                    val paddingV = 4.dp.toPx()
                    val gap = 2.dp.toPx()
                    val w = max(title.size.width, value.size.width) + paddingH * 2
                    val h = title.size.height + value.size.height + gap + paddingV * 2
                    val x = (slot * (selectedIndex + 0.5f) - w / 2)
                        .coerceIn(0f, (contentWidth - w).coerceAtLeast(0f))
                    val y = (selectedTop - h - 5.dp.toPx()).coerceAtLeast(0f)
                    drawRoundRect(inverse, Offset(x, y), Size(w, h), CornerRadius(8.dp.toPx()))
                    drawText(title, topLeft = Offset(x + (w - title.size.width) / 2, y + paddingV))
                    drawText(
                        value,
                        topLeft = Offset(x + (w - value.size.width) / 2, y + paddingV + title.size.height + gap)
                    )
                }
            }
        }

        if (average > 0f) {
            Canvas(Modifier.fillMaxSize()) {
                val averageY = baseY - plotHeight * (average / maxValue)

                drawLine(
                    averageColor.copy(alpha = 0.8f), Offset(0f, averageY), Offset(plotWidth, averageY),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
            }
        }
    }
}
