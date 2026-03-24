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
import androidx.compose.ui.text.TextStyle
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
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()
private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
    val labels = context.model.extraStore[BottomAxisLabelKey]
    labels[x.toInt().coerceIn(labels.indices)]
}

private const val END_AXIS_STEP = 5.0
private val EndAxisItemPlacer = VerticalAxis.ItemPlacer.step({ END_AXIS_STEP })

@Composable
fun rememberAxisLabelComponent(): TextComponent {
    return TextComponent(
        textStyle = typography.labelSmall
    )
}

@Composable
private fun rememberAverageLine(average: Float, showAverage: Boolean): HorizontalLine? {
    if (!showAverage || average <= 0f) return null
    val fill = Fill(colorScheme.secondaryContainer)
    val line = rememberLineComponent(
        fill = fill,
        thickness = 1.dp,
        shape = DashedShape(dashLength = 4.dp, gapLength = 4.dp)
    )
    val labelComponent = rememberTextComponent(
        style = TextStyle(color = colorScheme.onSecondaryContainer),
        margins = Insets(start = 6.dp),
        padding = Insets(start = 8.dp, top = 2.dp, end = 8.dp, bottom = 4.dp),
        background = rememberShapeComponent(
            fill,
            RoundedCornerShape(4.dp)
        ),
    )
    return remember(average) {
        HorizontalLine(
            y = { average.toDouble() },
            line = line,
            labelComponent = labelComponent,
            label = { "平均" },
            verticalLabelPosition = Position.Vertical.Bottom,
        )
    }
}

@Composable
private fun readingTimeMarker(minuteLabel: String) = rememberMarker(
    valueFormatter = { _, targets ->
        val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
        buildAnnotatedString {
            withStyle(SpanStyle(column.color)) {
                append("${column.entry.y.toInt()}$minuteLabel")
            }
        }
    }
)

@Composable
private fun DailyStatsChart(
    date: LocalDate,
    statsMap: Map<LocalDate, ReadingStatisticsEntity>
) {
    val formatter = DateTimeFormatter.ofPattern("MM/dd", LocalLocale.current.platformLocale)
    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = stringResource(R.string.detail_of_date, date.format(formatter)),
        style = typography.titleMedium
    )

    val hourlyMap = statsMap[date]?.readingTimeCount?.getHourStatistics() ?: emptyMap()
    if (hourlyMap.values.sum() < 1) {
        Box(
            modifier = Modifier.height(80.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_records))
        }
        return
    }

    val values = List(24) { hourlyMap[it]?.toFloat() ?: 0f }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    val hourClockLabel = stringResource(R.string.unit_hour_clock)
    val minuteLabel = stringResource(R.string.unit_minutes)
    val marker = rememberMarker(
        valueFormatter = { _, targets ->
            val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
            buildAnnotatedString {
                withStyle(SpanStyle(column.color)) {
                    append("${column.entry.x.toInt()}$hourClockLabel: ${column.entry.y.toInt()}$minuteLabel")
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
    statsMap: Map<LocalDate, ReadingStatisticsEntity>,
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
        dates.map { statsMap[it]?.readingTimeCount?.getTotalMinutes()?.toFloat() ?: 0f }
    }
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

    val minuteLabel = stringResource(R.string.unit_minutes)
    val marker = readingTimeMarker(minuteLabel)
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
                    color = colorScheme.secondary
                    )
                Text(
                    text = "${totalMinutes.toInt()} 分钟",
                    style = typography.titleLarge,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.unit_minutes),
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 2.dp).align(Alignment.Bottom)
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
                    valueFormatter = CartesianValueFormatter { _, v, _ -> "${v.toInt()}" }
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

@Composable
fun MonthlyStatsChart(
    statsMap: Map<LocalDate, ReadingStatisticsEntity>,
    selectedDate: LocalDate,
    showAverage: Boolean = true,
) {
    val yearMonth = remember(selectedDate) { YearMonth.from(selectedDate) }
    val weekBuckets = remember(yearMonth) { buildWeek(yearMonth) }

    val weekLabels = weekBuckets.map { stringResource(R.string.week_label_format, it.weekIndex) }
    val values = remember(weekBuckets, statsMap) {
        weekBuckets.map { bucket ->
            bucket.days.sumOf { day ->
                statsMap[day]?.readingTimeCount?.getTotalMinutes() ?: 0
            }.toFloat()
        }
    }
    val totalMinutes = values.sum()
    val average = remember(weekBuckets, statsMap, yearMonth) {
        val totalMinutes = weekBuckets.sumOf { bucket ->
            bucket.days.sumOf { day ->
                statsMap[day]?.readingTimeCount?.getTotalMinutes() ?: 0
            }
        }
        val totalDays = yearMonth.lengthOfMonth()
        totalMinutes.toFloat() / totalDays
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

    val minuteLabel = stringResource(R.string.unit_minutes)
    val marker = readingTimeMarker(minuteLabel)
    val averageLine = rememberAverageLine(average, showAverage)

    val markerListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val target = targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget
                target?.columns?.firstOrNull()?.entry?.let { selectedWeek = it.x.toInt() }
            }
            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val target = targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget
                target?.columns?.firstOrNull()?.entry?.let { selectedWeek = it.x.toInt() }
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
                    color = colorScheme.secondary
                )
                Text(
                    text = "${totalMinutes.toInt()} 分钟",
                    style = typography.titleLarge,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.unit_minutes),
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 2.dp).align(Alignment.Bottom)
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
                    valueFormatter = CartesianValueFormatter { _, v, _ -> "${v.toInt()}" }
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
    statsMap: Map<LocalDate, ReadingStatisticsEntity>
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
        bucket.days.map { statsMap[it]?.readingTimeCount?.getTotalMinutes()?.toFloat() ?: 0f }
    }

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

    val minuteLabel = stringResource(R.string.unit_minutes)
    val marker = rememberMarker(
        valueFormatter = { _, targets ->
            val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
            val idx = column.entry.x.toInt()
            val dateStr = dateLabels.getOrElse(idx) { "-" }
            buildAnnotatedString {
                withStyle(SpanStyle(column.color)) {
                    append("$dateStr: ${column.entry.y.toInt()}$minuteLabel")
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
    )
}

@Composable
fun YearlyStatsChart(
    statsMap: Map<LocalDate, ReadingStatisticsEntity>,
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
                statsMap[ym.atDay(day)]?.readingTimeCount?.getTotalMinutes() ?: 0
            }.toFloat()
        }
    }
    val totalMinutes = values.sum()
    val average = remember(values) {
        if (values.isEmpty()) 0f else totalMinutes / values.size
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

    val minuteLabel = stringResource(R.string.unit_minutes)
    val marker = readingTimeMarker(minuteLabel)
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
                    color = colorScheme.secondary
                )
                Text(
                    text = "${totalMinutes.toInt()} 分钟",
                    style = typography.titleLarge,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.unit_minutes),
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 2.dp).align(Alignment.Bottom)
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
                    valueFormatter = CartesianValueFormatter { _, v, _ -> "${v.toInt()}" }
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
    statsMap: Map<LocalDate, ReadingStatisticsEntity>
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
                statsMap[day]?.readingTimeCount?.getTotalMinutes() ?: 0
            }.toFloat()
        }
    }

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

    val minuteLabel = stringResource(R.string.unit_minutes)
    val marker = rememberMarker(
        valueFormatter = { _, targets ->
            val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
            val idx = column.entry.x.toInt()
            val rangeStr = rangeLabels.getOrElse(idx) { "" }
            buildAnnotatedString {
                withStyle(SpanStyle(column.color)) {
                    append("$rangeStr: ${column.entry.y.toInt()}$minuteLabel")
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
    )
}
