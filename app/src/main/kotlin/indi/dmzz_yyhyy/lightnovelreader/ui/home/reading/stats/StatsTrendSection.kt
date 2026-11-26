package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BarChart
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BarChartEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BarChartTooltip
import kotlin.math.abs
import kotlin.math.roundToInt

private fun rangeLabel(mode: TrendMode, bars: List<TrendBar>): String {
    val start = bars.first().start
    val end = bars.last().end
    return when (mode) {
        TrendMode.Week -> "${start.toStatsDay()} – ${end.toStatsDay()}"
        TrendMode.Month -> start.toStatsMonth()
        TrendMode.Year -> start.toStatsYear()
    }
}

@Composable
fun StatsTrendSection(
    uiState: StatsOverviewUiState
) {
    val today = uiState.today
    val selectedDate = uiState.selectedDate
    var mode by rememberSaveable { mutableStateOf(TrendMode.Week) }
    var anchor by remember { mutableStateOf(selectedDate) }
    val bars = remember(uiState.dayMinutes, uiState.monthMinutes, today, mode, anchor) {
        buildTrendBars(uiState.dayMinutes, uiState.monthMinutes, today, mode, anchor)
    }

    LaunchedEffect(selectedDate) {
        val inWindow = !selectedDate.isBefore(bars.first().start) && !selectedDate.isAfter(bars.last().end)
        if (!inWindow) anchor = selectedDate
    }

    val windowStart = bars.first().start
    val windowEnd = bars.last().end
    var focusedIndex by remember(mode, windowStart) { mutableIntStateOf(-1) }
    val elapsed = bars.count { !it.isFuture }
    val total = bars.sumOf { it.minutes }
    val average = if (elapsed == 0) 0f else total.toFloat() / elapsed
    val previousBars = remember(uiState.dayMinutes, uiState.monthMinutes, today, mode, anchor) {
        buildTrendBars(uiState.dayMinutes, uiState.monthMinutes, today, mode, windowStart.minusDays(1))
    }
    val previousTotal = previousBars.sumOf { it.minutes }
    val previousAverage = previousTotal.toFloat() / previousBars.size
    val delta = if (previousTotal > 0 && elapsed > 0) (average - previousAverage) / previousAverage else null

    val selectedIndex = when {
        focusedIndex == -2 -> -1
        focusedIndex >= 0 -> focusedIndex
        else -> when (mode) {
            TrendMode.Year ->
                if (selectedDate.year == windowStart.year) selectedDate.monthValue - 1 else -1
            else -> bars.indexOfFirst { it.start == selectedDate }
        }
    }

    val weekdayColor = heatColor(Level.Four, false)
    val weekendColor = heatColor(Level.Four, true)
    val chartBars = remember(bars, weekdayColor, weekendColor) {
        bars.map { bar ->
            BarChartEntry(
                label = bar.label,
                value = bar.minutes,
                color = if (bar.isWeekend) weekendColor else weekdayColor,
                isPlaceholder = bar.isFuture
            )
        }
    }
    val tooltip = bars.getOrNull(selectedIndex)?.takeIf { it.minutes > 0 }?.let { bar ->
        BarChartTooltip(
            title = if (bar.start == bar.end) bar.start.toStatsDate() else bar.start.toStatsMonth(),
            value = formatMinutes(bar.minutes)
        )
    }

    Column(Modifier.padding(horizontal = 18.dp)) {
        StatsSectionTitle(stringResource(R.string.trend))
        StatsPanel {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                TrendMode.entries.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = mode == item,
                        onClick = { mode = item },
                        shape = SegmentedButtonDefaults.itemShape(index, TrendMode.entries.size),
                        label = { Text(stringResource(item.labelRes)) }
                    )
                }
            }
            Row(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { anchor = windowStart.minusDays(1) },
                    enabled = windowStart.isAfter(uiState.startDate)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_24px),
                        contentDescription = stringResource(R.string.previous_period)
                    )
                }
                Text(
                    text = rangeLabel(mode, bars),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = typography.labelLarge
                )
                IconButton(
                    onClick = { anchor = windowEnd.plusDays(1) },
                    enabled = windowEnd.isBefore(today)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward_24px),
                        contentDescription = stringResource(R.string.next_period)
                    )
                }
            }
            Row(Modifier.padding(horizontal = 4.dp)) {
                TrendStat(
                    label = stringResource(R.string.total),
                    value = if (total == 0) "—" else formatMinutes(total),
                    modifier = Modifier.weight(15f)
                )
                TrendStat(
                    label = stringResource(R.string.average),
                    value = if (average < 1f) "—" else formatMinutes(average.roundToInt()),
                    modifier = Modifier.weight(15f)
                )
                TrendStat(
                    label = stringResource(R.string.trend_change),
                    value = delta?.let { "${if (it >= 0) "↑" else "↓"} ${abs(it * 100).roundToInt()}%" } ?: "—",
                    modifier = Modifier.weight(10f),
                    valueColor = when {
                        delta == null -> colorScheme.onSurface
                        delta >= 0 -> colorScheme.primary
                        else -> colorScheme.outline
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            BarChart(
                bars = chartBars,
                selectedIndex = selectedIndex,
                average = average,
                formatAxisLabel = { axisLabel(it) },
                tooltip = tooltip,
                zeroColor = heatColor(Level.Zero, false),
                onTap = { index ->
                    if (bars[index].isFuture) return@BarChart
                    focusedIndex = if (index == selectedIndex) -2 else index
                }
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TrendStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = colorScheme.onSurface
) {
    Column(modifier) {
        Text(label, style = typography.labelMedium, color = colorScheme.onSurfaceVariant)
        Text(value, style = typography.titleMedium, fontWeight = FontWeight.W600, color = valueColor)
    }
}
