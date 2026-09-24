package indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

// E.g DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY) = 3
fun DayOfWeek.daysUntil(other: DayOfWeek): Int = (7 + (other.ordinal - ordinal)) % 7

fun daysOfWeek(firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale()): List<DayOfWeek> {
    val pivot = 7 - firstDayOfWeek.ordinal
    val daysOfWeek = DayOfWeek.entries
    // Order `daysOfWeek` array so that firstDayOfWeek is at the start position.
    return daysOfWeek.takeLast(pivot) + daysOfWeek.dropLast(pivot)
}

fun firstDayOfWeekFromLocale(locale: Locale = Locale.getDefault()): DayOfWeek =
    WeekFields.of(locale).firstDayOfWeek

fun YearMonth.atStartOfMonth(): LocalDate = this.atDay(1)

val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(year, month)

val YearMonth.nextMonth: YearMonth
    get() = this.plusMonths(1)

val YearMonth.previousMonth: YearMonth
    get() = this.minusMonths(1)