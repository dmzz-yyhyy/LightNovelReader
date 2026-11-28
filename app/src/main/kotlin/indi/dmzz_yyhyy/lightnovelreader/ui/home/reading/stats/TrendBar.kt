package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import indi.dmzz_yyhyy.lightnovelreader.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class TrendMode(
    @param:StringRes val labelRes: Int
) {
    Week(R.string.week),
    Month(R.string.month),
    Year(R.string.year)
}

@Immutable
data class TrendBar(
    val label: String,
    val minutes: Int,
    val start: LocalDate,
    val end: LocalDate,
    val isWeekend: Boolean,
    val isFuture: Boolean
)

fun buildTrendBars(
    dayMinutes: Map<LocalDate, Int>,
    monthMinutes: Map<YearMonth, Int>,
    today: LocalDate,
    mode: TrendMode,
    anchor: LocalDate
): List<TrendBar> {
    return when (mode) {
        TrendMode.Week -> {
            val monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            (0..6).map {
                val date = monday.plusDays(it.toLong())
                TrendBar(
                    label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    minutes = dayMinutes[date] ?: 0,
                    start = date,
                    end = date,
                    isWeekend = date.dayOfWeek.value >= 6,
                    isFuture = date.isAfter(today)
                )
            }
        }

        TrendMode.Month -> {
            val yearMonth = YearMonth.from(anchor)
            (1..yearMonth.lengthOfMonth()).map {
                val date = yearMonth.atDay(it)
                TrendBar(
                    label = "$it",
                    minutes = dayMinutes[date] ?: 0,
                    start = date,
                    end = date,
                    isWeekend = date.dayOfWeek.value >= 6,
                    isFuture = date.isAfter(today)
                )
            }
        }

        TrendMode.Year -> (1..12).map {
            val yearMonth = YearMonth.of(anchor.year, it)
            TrendBar(
                label = "$it",
                minutes = monthMinutes[yearMonth] ?: 0,
                start = yearMonth.atDay(1),
                end = yearMonth.atEndOfMonth(),
                isWeekend = false,
                isFuture = yearMonth.atDay(1).isAfter(today)
            )
        }
    }
}
