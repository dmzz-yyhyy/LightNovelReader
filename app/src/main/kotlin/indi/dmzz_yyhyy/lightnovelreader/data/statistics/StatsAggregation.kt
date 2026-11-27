package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun buildDailyDateDetails(
    date: LocalDate,
    records: List<BookRecord>,
    count: Count?
): DailyDateDetails? {
    val readRecords = records.filter { it.reads > 0 || it.seconds > 0 }
    if (readRecords.isEmpty()) return null
    val timedRecords = readRecords.filter { it.seconds > 0 }

    return DailyDateDetails(
        date = date,
        totalSeconds = timedRecords.sumOf { it.seconds },
        readCount = readRecords.sumOf { it.reads },
        hourMinutes = (0..23).map { count?.getMinute(it) ?: 0 },
        books = timedRecords
            .map { DailyBookDetail(it.book, it.bookInformationFlow, it.seconds) }
            .sortedByDescending { it.seconds }
    )
}

/** 没有任何阅读记录时返回 null。 */
fun buildStatsSummary(
    dayMinutes: Map<LocalDate, Int>,
    dailyCounts: Map<LocalDate, Count>,
    records: List<BookRecord>,
    today: LocalDate,
    topBookCount: Int
): StatsSummary? {
    val activeDates = dayMinutes.keys.sorted()
    val firstDate = activeDates.firstOrNull() ?: return null
    val longestDay = dayMinutes.maxBy { it.value }

    val hourTotals = (0..23).map { hour -> dailyCounts.values.sumOf { it.getMinute(hour) } }
    val weekdayDays = IntArray(7)
    val weekdayMinutes = IntArray(7)
    var cursor = firstDate
    while (!cursor.isAfter(today)) {
        val index = cursor.dayOfWeek.ordinal
        weekdayDays[index]++
        weekdayMinutes[index] += dayMinutes[cursor] ?: 0
        cursor = cursor.plusDays(1)
    }
    val weekdayAverages = DayOfWeek.entries.map {
        val days = weekdayDays[it.ordinal]
        if (days == 0) 0f else weekdayMinutes[it.ordinal].toFloat() / days
    }

    var longestStreak = 0
    var longestStreakEnd = firstDate
    var run = 0
    var previous: LocalDate? = null
    for (date in activeDates) {
        run = if (previous != null && previous.plusDays(1) == date) run + 1 else 1
        if (run > longestStreak) {
            longestStreak = run
            longestStreakEnd = date
        }
        previous = date
    }
    var streakCursor = if (today in dayMinutes) today else today.minusDays(1)
    var currentStreak = 0
    while (streakCursor in dayMinutes) {
        currentStreak++
        streakCursor = streakCursor.minusDays(1)
    }

    val recordsByBook = records.groupBy { it.book }
    val topBooks = recordsByBook
        .map { (book, bookRecords) ->
            BookReadingTotal(
                book = book,
                bookInformationFlow = bookRecords.first().bookInformationFlow,
                seconds = bookRecords.sumOf { it.seconds.toLong() }
            )
        }
        .filter { it.seconds > 0 }
        .sortedByDescending { it.seconds }
        .take(topBookCount)

    return StatsSummary(
        totalMinutes = dayMinutes.values.sum(),
        totalReads = records.sumOf { it.reads },
        activeDays = activeDates.size,
        spanDays = (ChronoUnit.DAYS.between(firstDate, today) + 1).toInt(),
        firstDate = firstDate,
        longestDayDate = longestDay.key,
        longestDayMinutes = longestDay.value,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        longestStreakEnd = longestStreakEnd,
        hourTotals = hourTotals,
        weekdayAverages = weekdayAverages,
        startedCount = records.filter { it.reads > 0 }.map { it.book }.distinct().size,
        topBooks = topBooks
    )
}
