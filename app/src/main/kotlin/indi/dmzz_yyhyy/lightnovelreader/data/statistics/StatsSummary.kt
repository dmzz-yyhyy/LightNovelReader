package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import java.time.LocalDate

data class StatsSummary(
    val totalMinutes: Int,
    val totalReads: Int,
    val activeDays: Int,
    val spanDays: Int,
    val firstDate: LocalDate,
    val longestDayDate: LocalDate,
    val longestDayMinutes: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val longestStreakEnd: LocalDate,
    val hourTotals: List<Int>,
    val weekdayAverages: List<Float>,
    val startedCount: Int,
    val topBooks: List<BookReadingTotal>
) {
    val averageMinutesPerActiveDay: Int get() = totalMinutes / activeDays
    val averageMinutesPerDay: Int get() = totalMinutes / spanDays
    val longestStreakStart: LocalDate get() = longestStreakEnd.minusDays((longestStreak - 1).toLong())
}
