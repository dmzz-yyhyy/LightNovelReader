package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import com.valentinilk.shimmer.shimmer
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.rememberLoadingSkeletonShimmer
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow
import kotlin.math.max
import kotlin.math.min

internal val StatsHourLabels = (0..23).map { if (it % 4 == 0) "$it" else "" }

@Composable
fun StatsSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = typography.titleMedium,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun StatsPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colorScheme.surfaceContainerLow)
            .padding(18.dp),
        content = content
    )
}

@Composable
fun StatsEmptyHint(text: String = stringResource(R.string.no_records)) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        textAlign = TextAlign.Center,
        color = colorScheme.onSurface.copy(alpha = 0.5f),
        style = typography.bodyMedium
    )
}

@Composable
fun StatsBookTitle(
    bookInformationFlow: Flow<Result<BookInformation, WebRequestError>>,
    modifier: Modifier = Modifier,
    style: TextStyle = typography.bodyMedium
) {
    val shimmer = rememberLoadingSkeletonShimmer()
    val result by bookInformationFlow.collectAsStateWithLifecycle(null)
    result?.onOk {
        Text(it.title, modifier, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }?.onErr {
        Text(
            text = stringResource(R.string.error_book_title),
            modifier = modifier,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    } ?: Box(modifier = modifier.shimmer(shimmer)
            .width(120.dp)
            .height(20.dp)
            .background(colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp))
    )
}

@Composable
fun StatsBookBarRow(
    bookInformationFlow: Flow<Result<BookInformation, WebRequestError>>,
    timeText: String,
    fraction: Float
) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatsBookTitle(bookInformationFlow, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Text(
                text = timeText,
                style = typography.bodyMedium,
                color = colorScheme.outline,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(colorScheme.surfaceContainerHighest)
        ) {
            Box(modifier = Modifier.fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(colorScheme.primary)
            )
        }
    }
}

@Composable
fun StatsMiniBars(
    modifier: Modifier = Modifier,
    values: List<Float>,
    labels: List<String>,
    colors: List<Color>,
    barHeight: Dp = 48.dp,
    fixedMax: Float? = null
) {
    val measurer = rememberTextMeasurer()
    val maxBarWidth = 24.dp

    val track = colorScheme.surfaceContainerHighest
    val primaryColor = colorScheme.primary
    val labelStyle = typography.labelSmall.copy(color = colorScheme.onSurfaceVariant)
    val maxValue = fixedMax ?: max(values.maxOrNull() ?: 0f, 1f)
    val count = values.size
    if (count == 0) return

    val measuredLabels = remember(labels, labelStyle, measurer) {
        labels.map { label ->
            if (label.isNotEmpty()) {
                measurer.measure(label, labelStyle)
            } else null
        }
    }

    Canvas(
        modifier.fillMaxWidth()
            .height(barHeight + 18.dp)
    ) {
        val height = barHeight.toPx()
        val barWidth = min(size.width / count * 0.62f, maxBarWidth.toPx())
        // Align the first and last bars to the canvas edges
        val step = if (count > 1) (size.width - barWidth) / (count - 1) else 0f
        val radius = CornerRadius(barWidth / 2)

        values.forEachIndexed { i, value ->
            val centerX = if (count > 1) barWidth / 2 + step * i else size.width / 2
            val x = centerX - barWidth / 2

            drawRoundRect(track, Offset(x, 0f), Size(barWidth, height), radius)

            if (value > 0f) {
                // Keep small bars tall enough to preserve rounded ends
                val h = max(
                    height * (value / maxValue).coerceAtMost(1f), barWidth
                ).coerceAtMost(height)
                drawRoundRect(
                    colors.getOrElse(i) { primaryColor },
                    Offset(x, height - h),
                    Size(barWidth, h),
                    radius
                )
            }

            measuredLabels.getOrNull(i)?.let { text ->
                val labelX = (centerX - text.size.width / 2f)
                    .coerceIn(0f, (size.width - text.size.width).coerceAtLeast(0f))

                drawText(
                    text,
                    topLeft = Offset(labelX, height + 4.dp.toPx())
                )
            }
        }
    }
}
