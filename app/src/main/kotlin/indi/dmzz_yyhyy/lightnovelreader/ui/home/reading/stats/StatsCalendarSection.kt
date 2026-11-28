package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.HeatMapCalendar
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarDay
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarMonth
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarWeek
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.displayText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.yearMonth
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.rememberHeatMapCalendarState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month

@Composable
fun heatColor(level: Level, isWeekend: Boolean): Color {
    val base = if (isWeekend) colorScheme.tertiary else colorScheme.primary
    return when (level) {
        Level.Zero -> colorScheme.onSurface.copy(alpha = 0.08f)
        Level.One -> base.copy(alpha = 0.28f)
        Level.Two -> base.copy(alpha = 0.5f)
        Level.Three -> base.copy(alpha = 0.75f)
        Level.Four -> base
    }
}

@Composable
fun StatsCalendarSection(
    uiState: StatsOverviewUiState,
    onSelectedDate: (LocalDate) -> Unit
) {
    val today = uiState.today
    val startDate = uiState.startDate
    val selectedDate = uiState.selectedDate
    val state = rememberHeatMapCalendarState(
        startMonth = startDate.yearMonth,
        endMonth = today.yearMonth,
        firstVisibleMonth = today.yearMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
        StatsSectionTitle(stringResource(R.string.calendar)) {
            if (selectedDate != today) {
                TextButton(onClick = { onSelectedDate(today) }) {
                    Text(stringResource(R.string.back_to_today))
                }
            }
        }
        HeatMapCalendar(
            state = state,
            contentPadding = PaddingValues(end = 6.dp),
            dayContent = { day, week ->
                CalendarDayBox(
                    selected = selectedDate == day.date,
                    day = day,
                    startDate = startDate,
                    endDate = today,
                    week = week,
                    level = uiState.dateLevelMap[day.date] ?: Level.Zero,
                    onClick = onSelectedDate
                )
            },
            weekHeader = { CalendarWeekHeader(it) },
            monthHeader = { CalendarMonthHeader(it, today) }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            Text(text = stringResource(R.string.heatmap_indicator_less), style = typography.bodySmall)
            Spacer(Modifier.width(6.dp))
            Level.entries.forEach { LevelBox(heatColor(it, false)) }
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.heatmap_indicator_more, uiState.thresholds),
                style = typography.bodySmall
            )
        }
    }
}

@Composable
private fun CalendarDayBox(
    selected: Boolean,
    day: CalendarDay,
    startDate: LocalDate,
    endDate: LocalDate,
    week: CalendarWeek,
    level: Level,
    onClick: (LocalDate) -> Unit
) {
    val isWeekday = day.date.dayOfWeek.value in 1..5

    if (day.date in startDate..endDate) {
        LevelBox(
            color = heatColor(level, !isWeekday),
            selected = selected
        ) { onClick(day.date) }
    } else if (week.days.any { it.date == startDate }) {
        LevelBox(Color.Transparent)
    }
}

@Composable
private fun LevelBox(
    color: Color,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(2.dp))
            .let { if (selected) it.border(1.dp, colorScheme.onSurface) else it }
            .background(color)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    )
}

@Composable
private fun CalendarWeekHeader(dayOfWeek: DayOfWeek) {
    val text = if (dayOfWeek in listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY))
        dayOfWeek.displayText() else ""

    Box(Modifier.height(20.dp)) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center)
                .padding(horizontal = 4.dp),
            style = typography.headlineMedium
        )
    }
}

@Composable
private fun CalendarMonthHeader(
    calendarMonth: CalendarMonth,
    endDate: LocalDate
) {
    if (calendarMonth.weekDays.first().first().date <= endDate) {
        val month = calendarMonth.yearMonth
        val title = if (month.month == Month.JANUARY) {
            month.displayText(short = true)
        } else {
            month.month.displayText()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(text = title, style = typography.headlineMedium)
        }
    }
}
