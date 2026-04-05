package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.DashedShape
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import indi.dmzz_yyhyy.lightnovelreader.utils.formMinutes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt
import java.time.format.TextStyle as JavaTextStyle

private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()
private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
    val labels = context.model.extraStore[BottomAxisLabelKey]
    labels[x.toInt().coerceIn(labels.indices)]
}

private val EndAxisItemPlacer = VerticalAxis.ItemPlacer.count({ 8 })

private fun useHoursUnit(values: List<Float>): Boolean =
    values.maxOrNull()?.let { it > 400f } == true

@Composable
fun rememberAxisLabelComponent(): TextComponent {
    val colorScheme = colorScheme
    return TextComponent(
        textStyle = typography.labelSmall.copy(
            color = colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun rememberReadingTimeAxisFormatter(useHours: Boolean): CartesianValueFormatter {
    return remember(useHours) {
        CartesianValueFormatter { _, value, _ ->
            if (useHours) {
                (value / 60.0).roundToInt().toString()
            } else {
                value.toInt().toString()
            }
        }
    }
}

@Composable
private fun rememberAverageLine(average: Float, showAverage: Boolean): HorizontalLine? {
    if (!showAverage || average <= 0f) return null
    val fill = Fill(colorScheme.tertiary)
    val line = rememberLineComponent(
        fill = fill,
        thickness = 1.dp,
        shape = DashedShape(dashLength = 4.dp, gapLength = 4.dp)
    )
    return remember(average) {
        HorizontalLine(
            y = { average.toDouble() },
            line = line,
        )
    }
}

@Composable
private fun readingTimeMarker() = rememberMarker(
    valueFormatter = { _, targets ->
        val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
        buildAnnotatedString {
            withStyle(SpanStyle(column.color)) {
                append(formMinutes(column.entry.y.toInt()))
            }
        }
    }
)

@Composable
private fun DailyStatsChart(
    date: LocalDate,
    statsMap: Map<LocalDate, Count>
) {
    val formatter = DateTimeFormatter.ofPattern("MM/dd", LocalLocale.current.platformLocale)
    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = stringResource(R.string.detail_of_date, date.format(formatter)),
        style = typography.titleMedium
    )
    val hourlyMap = statsMap[date]?.getHourStatistics() ?: emptyMap()
    val total = hourlyMap.values.sum()
    if (total < 1) {
        Box(
            modifier = Modifier.height(80.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_records))
        }
        return
    }

    val values = List(24) { hourlyMap[it]?.toFloat() ?: 0f }
    val useHoursOnAxis = remember(values) { useHoursUnit(values) }
    val axisValueFormatter = rememberReadingTimeAxisFormatter(useHoursOnAxis)
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    val hourClockLabel = stringResource(R.string.unit_hour_clock)
    val marker = rememberMarker(
        valueFormatter = { _, targets ->
            val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
            buildAnnotatedString {
                withStyle(SpanStyle(column.color)) {
                    append("${column.entry.x.toInt()}$hourClockLabel: ${formMinutes(column.entry.y.toInt())}")
                }
            }
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = Fill(colorScheme.primary),
                        thickness = 36.dp,
                        shape = RoundedCornerShape(topStartPercent = 26, topEndPercent = 26)
                    )
                )
            ),
            endAxis = VerticalAxis.rememberEnd(
                label = rememberAxisLabelComponent(),
                itemPlacer = VerticalAxis.ItemPlacer.count({ 5 }),
                guideline = rememberAxisGuidelineComponent(),
                valueFormatter = axisValueFormatter
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(),
                itemPlacer = HorizontalAxis.ItemPlacer.aligned(spacing = { 6 }),
                valueFormatter = CartesianValueFormatter { _, x, _ ->
                    "${x.toInt()}$hourClockLabel"
                }
            ),
            marker = marker,
            markerController = CartesianMarkerController.rememberToggleOnTap(),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}

@Composable
fun WeeklyStatsChart(
    statsMap: Map<LocalDate, Count>,
    selectedDate: LocalDate,
    showAverage: Boolean = true,
) {
    val dates = remember(selectedDate) {
        (0..6).map { selectedDate.minusDays((6 - it).toLong()) }
    }
    val locale = LocalLocale.current.platformLocale
    val dayLabels = remember(dates, locale) {
        val fmt = DateTimeFormatter.ofPattern("MM/dd", locale)
        dates.map { fmt.format(it) }
    }
    val values = remember(dates, statsMap) {
        dates.map { statsMap[it]?.getTotalMinutes()?.toFloat() ?: 0f }
    }
    val useHoursOnAxis = remember(values) { useHoursUnit(values) }
    val axisValueFormatter = rememberReadingTimeAxisFormatter(useHoursOnAxis)
    val totalMinutes = values.sum()
    val average = remember(values) {
        totalMinutes / values.size
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(selectedDate) { selectedIndex = -1 }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
            extras { it[BottomAxisLabelKey] = dayLabels }
        }
    }

    val marker = readingTimeMarker()
    val averageLine = rememberAverageLine(average, showAverage)

    val markerListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val target = targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget
                target?.columns?.firstOrNull()?.entry?.let { selectedIndex = it.x.toInt() }
            }
            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val target = targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget
                target?.columns?.firstOrNull()?.entry?.let { selectedIndex = it.x.toInt() }
            }
            override fun onHidden(marker: CartesianMarker) {}
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Row {
            Column {
                Text(
                    text = "总计",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = formMinutes(totalMinutes.toInt()),
                    style = typography.titleLarge,
                    color = colorScheme.secondary,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(if (useHoursOnAxis) R.string.unit_hours else R.string.unit_minutes),
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(colorScheme.primary),
                            thickness = 16.dp,
                            shape = RoundedCornerShape(topStartPercent = 26, topEndPercent = 26)
                        )
                    )
                ),
                endAxis = VerticalAxis.rememberEnd(
                    label = rememberAxisLabelComponent(),
                    itemPlacer = EndAxisItemPlacer,
                    guideline = rememberAxisGuidelineComponent(),
                    valueFormatter = axisValueFormatter
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberAxisLabelComponent(),
                    valueFormatter = BottomAxisValueFormatter
                ),
                decorations = listOfNotNull(averageLine),
                marker = marker,
                markerVisibilityListener = markerListener,
                markerController = CartesianMarkerController.rememberToggleOnTap(),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = true),
            zoomState = rememberVicoZoomState(zoomEnabled = false)
        )

        if (selectedIndex in dates.indices) {
            DailyStatsChart(date = dates[selectedIndex], statsMap = statsMap)
        }
    }
}

private data class Week(
    val weekIndex: Int,
    val start: LocalDate,
    val end: LocalDate,
    val days: List<LocalDate>,
)

private fun buildWeek(yearMonth: YearMonth): List<Week> {
    val firstDay = yearMonth.atDay(1)
    val lastDay = yearMonth.atEndOfMonth()

    val buckets = mutableListOf<Week>()
    var weekStart = firstDay
    var weekIndex = 1

    while (!weekStart.isAfter(lastDay)) {
        val naturalWeekEnd = weekStart.with(DayOfWeek.SUNDAY)
        val weekEnd = if (naturalWeekEnd.isAfter(lastDay)) lastDay else naturalWeekEnd
        val days = (0..ChronoUnit.DAYS.between(weekStart, weekEnd))
            .map { weekStart.plusDays(it) }
        buckets.add(
            Week(
                weekIndex,
                weekStart,
                weekEnd,
                days
            )
        )
        weekStart = weekEnd.plusDays(1)
        weekIndex++
    }

    return buckets
}

private fun resolveExpandableWeekIndex(
    targets: List<CartesianMarker.Target>,
    weekBuckets: List<Week>,
): Int {
    val target = targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget
    val index = target?.columns?.firstOrNull()?.entry?.x?.toInt() ?: return -1
    return index.takeIf { (weekBuckets.getOrNull(it)?.days?.size ?: 0) > 1 } ?: -1
}

@Composable
fun MonthlyStatsChart(
    statsMap: Map<LocalDate, Count>,
    selectedDate: LocalDate,
    showAverage: Boolean = true,
) {
    val yearMonth = remember(selectedDate) { YearMonth.from(selectedDate) }
    val weekBuckets = remember(yearMonth) { buildWeek(yearMonth) }

    val weekLabels = weekBuckets.map { stringResource(R.string.week_label_format, it.weekIndex) }
    val values = remember(weekBuckets, statsMap) {
        weekBuckets.map { bucket ->
            bucket.days.sumOf { day ->
                statsMap[day]?.getTotalMinutes() ?: 0
            }.toFloat()
        }
    }
    val useHoursOnAxis = remember(values) { useHoursUnit(values) }
    val axisValueFormatter = rememberReadingTimeAxisFormatter(useHoursOnAxis)
    val totalMinutes = values.sum()
    val average = remember(values) {
        if (values.isEmpty()) 0f else totalMinutes / values.size
    }

    var selectedWeek by remember { mutableIntStateOf(-1) }
    LaunchedEffect(selectedDate) { selectedWeek = -1 }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
            extras { it[BottomAxisLabelKey] = weekLabels }
        }
    }

    val marker = readingTimeMarker()
    val averageLine = rememberAverageLine(average, showAverage)

    val markerListener = remember(weekBuckets) {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                selectedWeek = resolveExpandableWeekIndex(targets, weekBuckets)
            }
            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                selectedWeek = resolveExpandableWeekIndex(targets, weekBuckets)
            }
            override fun onHidden(marker: CartesianMarker) {}
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Row {
            Column {
                Text(
                    text = "总计",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = formMinutes(totalMinutes.toInt()),
                    style = typography.titleLarge,
                    color = colorScheme.secondary,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(if (useHoursOnAxis) R.string.unit_hours else R.string.unit_minutes),
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(colorScheme.primary),
                            thickness = 24.dp,
                            shape = RoundedCornerShape(topStartPercent = 26, topEndPercent = 26)
                        )
                    )
                ),
                endAxis = VerticalAxis.rememberEnd(
                    label = rememberAxisLabelComponent(),
                    itemPlacer = EndAxisItemPlacer,
                    guideline = rememberAxisGuidelineComponent(),
                    valueFormatter = axisValueFormatter
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberAxisLabelComponent(),
                    valueFormatter = BottomAxisValueFormatter
                ),
                decorations = listOfNotNull(averageLine),
                marker = marker,
                markerVisibilityListener = markerListener,
                markerController = CartesianMarkerController.rememberToggleOnTap(),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )

        if (selectedWeek in weekBuckets.indices) {
            WeekDailyBreakdown(
                bucket = weekBuckets[selectedWeek],
                statsMap = statsMap
            )
        }
    }
}

@Composable
private fun WeekDailyBreakdown(
    bucket: Week,
    statsMap: Map<LocalDate, Count>
) {
    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = stringResource(R.string.detail_of_week, bucket.weekIndex),
        style = typography.titleMedium
    )

    val locale = LocalLocale.current.platformLocale
    val dateLabels = remember(bucket, locale) {
        val fmt = DateTimeFormatter.ofPattern("MM-dd", locale)
        bucket.days.map { fmt.format(it) }
    }
    val values = remember(bucket, statsMap) {
        bucket.days.map { statsMap[it]?.getTotalMinutes()?.toFloat() ?: 0f }
    }
    val useHoursOnAxis = remember(values) { useHoursUnit(values) }
    val axisValueFormatter = rememberReadingTimeAxisFormatter(useHoursOnAxis)

    if (values.all { it == 0f }) {
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

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
            extras { it[BottomAxisLabelKey] = dateLabels }
        }
    }

    val marker = rememberMarker(
        valueFormatter = { _, targets ->
            val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
            val idx = column.entry.x.toInt()
            val dateStr = dateLabels.getOrElse(idx) { "-" }
            buildAnnotatedString {
                withStyle(SpanStyle(column.color)) {
                    append("$dateStr: ${formMinutes(column.entry.y.toInt())}")
                }
            }
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = Fill(colorScheme.primary),
                        thickness = 24.dp,
                        shape = RoundedCornerShape(topStartPercent = 26, topEndPercent = 26)
                    )
                )
            ),
            endAxis = VerticalAxis.rememberEnd(
                label = rememberAxisLabelComponent(),
                itemPlacer = VerticalAxis.ItemPlacer.count({ 5 }),
                guideline = rememberAxisGuidelineComponent(),
                valueFormatter = axisValueFormatter
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(),
                valueFormatter = BottomAxisValueFormatter
            ),
            marker = marker,
            markerController = CartesianMarkerController.rememberToggleOnTap(),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        zoomState = rememberVicoZoomState(zoomEnabled = false)
    )
}

@Composable
fun YearlyStatsChart(
    statsMap: Map<LocalDate, Count>,
    selectedDate: LocalDate,
    showAverage: Boolean = true,
) {
    val year = selectedDate.year
    val locale = LocalLocale.current.platformLocale
    val monthLabels = remember(locale) {
        (1..12).map { Month.of(it).getDisplayName(JavaTextStyle.SHORT, locale) }
    }
    val values = remember(year, statsMap) {
        (1..12).map { month ->
            val ym = YearMonth.of(year, month)
            (1..ym.lengthOfMonth()).sumOf { day ->
                statsMap[ym.atDay(day)]?.getTotalMinutes() ?: 0
            }.toFloat()
        }
    }
    val useHoursOnAxis = remember(values) { useHoursUnit(values) }
    val axisValueFormatter = rememberReadingTimeAxisFormatter(useHoursOnAxis)
    val totalMinutes = values.sum()
    val average = remember(values) {
        val nonZeroValues = values.filter { it > 0f }
        if (nonZeroValues.isEmpty()) 0f else nonZeroValues.sum() / nonZeroValues.size
    }

    var selectedMonth by remember { mutableIntStateOf(-1) }
    LaunchedEffect(year) { selectedMonth = -1 }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
            extras { it[BottomAxisLabelKey] = monthLabels }
        }
    }

    val marker = readingTimeMarker()
    val averageLine = rememberAverageLine(average, showAverage)

    val markerListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val target = targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget
                target?.columns?.firstOrNull()?.entry?.let { selectedMonth = it.x.toInt() }
            }
            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val target = targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget
                target?.columns?.firstOrNull()?.entry?.let { selectedMonth = it.x.toInt() }
            }
            override fun onHidden(marker: CartesianMarker) {}
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Row {
            Column {
                Text(
                    text = "总计",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = formMinutes(totalMinutes.toInt()),
                    style = typography.titleLarge,
                    color = colorScheme.secondary,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(if (useHoursOnAxis) R.string.unit_hours else R.string.unit_minutes),
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(colorScheme.primary),
                            thickness = 16.dp,
                            shape = RoundedCornerShape(topStartPercent = 26, topEndPercent = 26)
                        )
                    )
                ),
                endAxis = VerticalAxis.rememberEnd(
                    label = rememberAxisLabelComponent(),
                    itemPlacer = EndAxisItemPlacer,
                    guideline = rememberAxisGuidelineComponent(),
                    valueFormatter = axisValueFormatter
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberAxisLabelComponent(),
                    valueFormatter = BottomAxisValueFormatter
                ),
                decorations = listOfNotNull(averageLine),
                marker = marker,
                markerVisibilityListener = markerListener,
                markerController = CartesianMarkerController.rememberToggleOnTap(),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxSize()
                .height(230.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = true),
            zoomState = rememberVicoZoomState(zoomEnabled = false)
        )

        if (selectedMonth in 0..11) {
            MonthWeeklyBreakdown(
                yearMonth = YearMonth.of(year, selectedMonth + 1),
                statsMap = statsMap
            )
        }
    }
}

@Composable
private fun MonthWeeklyBreakdown(
    yearMonth: YearMonth,
    statsMap: Map<LocalDate, Count>
) {
    val monthName = remember(yearMonth) {
        yearMonth.month.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())
    }
    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = monthName,
        style = typography.titleMedium
    )

    val weekBuckets = remember(yearMonth) { buildWeek(yearMonth) }
    val weekLabels = weekBuckets.map { stringResource(R.string.week_label_format, it.weekIndex) }
    val rangeFmt = remember { DateTimeFormatter.ofPattern("MM/dd") }
    val rangeLabels = remember(weekBuckets) {
        weekBuckets.map { b -> "${b.start.format(rangeFmt)} ~ ${b.end.format(rangeFmt)}" }
    }
    val values = remember(weekBuckets, statsMap) {
        weekBuckets.map { bucket ->
            bucket.days.sumOf { day ->
                statsMap[day]?.getTotalMinutes() ?: 0
            }.toFloat()
        }
    }
    val useHoursOnAxis = remember(values) { useHoursUnit(values) }
    val axisValueFormatter = rememberReadingTimeAxisFormatter(useHoursOnAxis)

    if (values.all { it == 0f }) {
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

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
            extras { it[BottomAxisLabelKey] = weekLabels }
        }
    }

    val marker = rememberMarker(
        valueFormatter = { _, targets ->
            val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
            val idx = column.entry.x.toInt()
            val rangeStr = rangeLabels.getOrElse(idx) { "" }
            buildAnnotatedString {
                withStyle(SpanStyle(column.color)) {
                    append("$rangeStr: ${formMinutes(column.entry.y.toInt())}")
                }
            }
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = Fill(colorScheme.primary),
                        thickness = 24.dp,
                        shape = RoundedCornerShape(topStartPercent = 26, topEndPercent = 26)
                    )
                )
            ),
            endAxis = VerticalAxis.rememberEnd(
                label = rememberAxisLabelComponent(),
                itemPlacer = VerticalAxis.ItemPlacer.count({ 5 }),
                guideline = rememberAxisGuidelineComponent(),
                valueFormatter = axisValueFormatter
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(),
                valueFormatter = BottomAxisValueFormatter
            ),
            marker = marker,
            markerController = CartesianMarkerController.rememberToggleOnTap(),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        zoomState = rememberVicoZoomState(zoomEnabled = false)
    )
}
