package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberTop
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.StatsDetailedUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.currentDateRange
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()
private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
    val labels = context.model.extraStore[BottomAxisLabelKey]
    val idx = x.toInt().coerceIn(labels.indices)
    labels[idx]
}

private val TopAxisLabelKey = ExtraStore.Key<List<String>>()
private val TopAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
    val labels = context.model.extraStore[TopAxisLabelKey]
    labels[x.toInt().coerceIn(labels.indices)]
}

private const val EndAxisStep = 5.0
private val EndAxisItemPlacer = VerticalAxis.ItemPlacer.step({ EndAxisStep })

@Composable
private fun HourlyReadingTimeChart(
    date: LocalDate,
    statsMap: Map<LocalDate, Count>
) {
    val formatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
    val formattedDate = date.format(formatter)
    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = stringResource(R.string.detail_of_date, formattedDate),
        style = typography.titleMedium
    )
    val hourlyMap = statsMap[date]?.getHourStatistics() ?: emptyMap()
    val total = hourlyMap.values.sum()
    if (total < 1) {
        Box(
            Modifier
                .height(80.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_records))
        }
        return
    }
    val labels = List(24) { "$it" }
    val values = List(24) { hourlyMap[it]?.toFloat() ?: 0f }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
            extras { it[BottomAxisLabelKey] = labels }
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val hourClockLabel = stringResource(R.string.unit_hour_clock)
        val minuteLabel = stringResource(R.string.unit_minutes)

        val marker = rememberMarker(valueFormatter = { _, targets ->
            val columnTarget = (targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget)
            val entry = columnTarget?.columns?.firstOrNull()?.entry
            if (entry != null) {
                val hour = entry.x.toInt().coerceIn(0, 23)
                val minute = entry.y.toInt()
                SpannableStringBuilder().append(
                    "$hour$hourClockLabel: $minute$minuteLabel",
                    ForegroundColorSpan(columnTarget.columns.first().color),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else SpannableStringBuilder()
        })

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = fill(colorScheme.primary),
                            thickness = 36.dp,
                            shape = CorneredShape.rounded(topLeftPercent = 26, topRightPercent = 26)
                        )
                    )
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberAxisLabelComponent(color = colorScheme.onSurface),
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(spacing = { 6 }),
                    valueFormatter = CartesianValueFormatter { _, x, _ ->
                        "${x.toInt()}$hourClockLabel"
                    }
                ),
                decorations = emptyList(),
                marker = marker,
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxSize(),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )
    }
}

@Composable
fun ReadingTimeChart(
    uiState: StatsDetailedUiState,
    labelMapper: (LocalDate) -> String,
) {
    val range = uiState.currentDateRange
    val countMap = uiState.targetDateRangeCountMap
    val dates = remember(range.start, range.endInclusive) {
        generateSequence(range.start) { it.plusDays(1) }
            .takeWhile { it <= range.endInclusive }
            .toList()
    }
    if (dates.isEmpty()) return

    val labels = remember(dates) { dates.map { labelMapper(it) } }
    val topLabels = remember(dates) {
        val formatter = DateTimeFormatter.ofPattern("M/d")
        dates.map { date -> formatter.format(date) }
    }

    val values = remember(dates, countMap) {
        dates.map { countMap[it]?.getTotalMinutes()?.toFloat() ?: 0f }
    }
    var selectedIndex by remember { mutableStateOf(-1f) }
    LaunchedEffect(range.start, range.endInclusive) {
        selectedIndex = -1f
    }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
            extras {
                it[BottomAxisLabelKey] = labels
                it[TopAxisLabelKey] = topLabels
            }
        }
    }

    val minuteLabel = stringResource(R.string.unit_minutes)
    val verticalAxisFormatter = CartesianValueFormatter { _, value, _ ->
        val formatted = DecimalFormat("#,###").format(value.toInt())
        "$formatted$minuteLabel"
    }

    val marker = rememberMarker(
        valueFormatter = { _, targets ->
            val columnTarget = (targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget)
            val entry = columnTarget?.columns?.firstOrNull()?.entry
            if (entry != null) {
                val idx = entry.x.toInt().coerceIn(dates.indices)
                val date = dates[idx]
                val totalMin = countMap[date]?.getTotalMinutes() ?: 0
                SpannableStringBuilder().append(
                    "${dates[selectedIndex.toInt()]}: ${DecimalFormat("#,###").format(totalMin)}$minuteLabel",
                    ForegroundColorSpan(columnTarget.columns.first().color),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else SpannableStringBuilder()
        }
    )

    val markerVisibilityListener = object : CartesianMarkerVisibilityListener {
        override fun onHidden(marker: CartesianMarker) { }
        override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
            val columnTarget = (targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget)
            val entry = columnTarget?.columns?.firstOrNull()?.entry
            if (entry != null) {
                selectedIndex = entry.x.toFloat()
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = fill(colorScheme.primary),
                            thickness = 16.dp,
                            shape = CorneredShape.rounded(topLeftPercent = 26, topRightPercent = 26)
                        )
                    )
                ),
                endAxis = VerticalAxis.rememberEnd(
                    label = rememberAxisLabelComponent(color = colorScheme.onSurface),
                    itemPlacer = EndAxisItemPlacer,
                    guideline = rememberAxisGuidelineComponent(shape = CorneredShape.Pill),
                    valueFormatter = verticalAxisFormatter
                ),
                topAxis = HorizontalAxis.rememberTop(
                    label = rememberAxisLabelComponent(color = colorScheme.onSurface),
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(spacing = { 3 }),
                    valueFormatter = TopAxisValueFormatter
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberAxisLabelComponent(color = colorScheme.onSurface),
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(),
                    valueFormatter = BottomAxisValueFormatter
                ),
                decorations = emptyList(),
                marker = marker,
                markerVisibilityListener = markerVisibilityListener,
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxSize(),
            scrollState = rememberVicoScrollState(scrollEnabled = true),
        )
    }

    if (selectedIndex.toInt() in dates.indices) {
        HourlyReadingTimeChart(date = dates[selectedIndex.toInt()], statsMap = countMap)
    }
}