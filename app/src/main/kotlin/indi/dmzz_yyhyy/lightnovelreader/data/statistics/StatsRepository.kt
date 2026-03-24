package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.DailyCountDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.DailyCountEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

data class TotalReadingSummary(
    val totalMinutes: Int,
    val totalReadCount: Int
)

@Singleton
class StatsRepository @Inject constructor(
    private val bookRecordDao: BookRecordDao,
    private val dailyCountDao: DailyCountDao
) {
    private val bookReadTimeBuffer = mutableMapOf<String, Pair<LocalTime, Int>>()

    suspend fun accumulateBookReadTime(bookId: String, seconds: Int) {
        if (seconds < 0) {
            bookReadTimeBuffer.keys.toList().forEach { _ ->
                clearBookReadTimeBuffer(bookId)
                bookReadTimeBuffer.remove(bookId)
            }
            return
        }
        val current = bookReadTimeBuffer[bookId] ?: Pair(LocalTime.now(), 0)
        val newTotal = current.second + seconds
        bookReadTimeBuffer[bookId] = current.copy(second = newTotal)

        if (newTotal >= 60 || Duration.between(current.first, LocalTime.now()).seconds >= 60) {
            clearBookReadTimeBuffer(bookId)
        }
    }

    private suspend fun clearBookReadTimeBuffer(bookId: String) {
        val (startTime, totalSeconds) = bookReadTimeBuffer[bookId] ?: return

        updateReadingStatistics(
            ReadingStatsUpdate(
                bookId = bookId,
                secondDelta = totalSeconds,
                localTime = startTime,
                readEventDelta = 0
            )
        )

        bookReadTimeBuffer.clear()
    }

    suspend fun getBookRecords(
        start: LocalDate,
        end: LocalDate? = null
    ): Map<LocalDate, List<BookRecord>> {
        return if (end == null) {
            bookRecordDao.getBookRecordsForDate(start)
                .map { it.toData() }
                .takeIf { it.isNotEmpty() }
                ?.let { mapOf(start to it) }
                ?: emptyMap()
        } else {
            bookRecordDao
                .getBookRecordsBetweenDates(start, end)
                .map { it.toData() }
                .groupBy { it.date }
                .filterValues { it.isNotEmpty() }
        }
    }

    suspend fun getDailyCounts(start: LocalDate, end: LocalDate): Map<LocalDate, Count> {
        return dailyCountDao.getBetween(start, end)
            .associate { it.date to it.timeCount }
    }

    fun getTotalReadingSummary(): TotalReadingSummary {
        val dailyCounts = dailyCountDao.getAll()
        val records = bookRecordDao.getAllBookRecords()
        val totalMinutes = dailyCounts.sumOf { it.timeCount.getTotalMinutes() }
        val totalReadCount = records.sumOf { it.reads }
        return TotalReadingSummary(
            totalMinutes = totalMinutes,
            totalReadCount = totalReadCount
        )
    }

    suspend fun updateReadingStatistics(update: ReadingStatsUpdate) {
        val today = LocalDate.now()

        val existingDailyCount = dailyCountDao.getByDate(today)
            ?: DailyCountEntity(today, Count())
        val updatedDailyCount = existingDailyCount.copy(
            timeCount = updateCount(existingDailyCount.timeCount, update)
        )
        dailyCountDao.insert(updatedDailyCount)

        val existingRecord = bookRecordDao.getBookRecordByIdAndDate(update.bookId, today)
            ?: createRecordEntity(update.bookId, today)

        val updatedRecord = existingRecord.copy(
            reads = existingRecord.reads + update.readEventDelta,
            seconds = existingRecord.seconds + update.secondDelta,
            lastSeen = update.localTime,
        )
        bookRecordDao.insertBookRecord(updatedRecord)
        bookReadTimeBuffer.clear()
    }

    suspend fun markBookFinished(bookId: String) {
        val today = LocalDate.now()
        val existingRecord = bookRecordDao.getBookRecordByIdAndDate(bookId, today)
            ?: createRecordEntity(bookId, today)

        if (!existingRecord.isFinished) {
            bookRecordDao.insertBookRecord(existingRecord.copy(isFinished = true))
        }
    }

    suspend fun markBookFavorited(bookId: String) {
        val today = LocalDate.now()
        val existingRecord = bookRecordDao.getBookRecordByIdAndDate(bookId, today)
            ?: createRecordEntity(bookId, today)

        if (!existingRecord.isFavorited) {
            bookRecordDao.insertBookRecord(existingRecord.copy(isFavorited = true))
        }
    }

    suspend fun getBookFirstReadDate(bookId: String): LocalDate? =
        bookRecordDao.getFirstReadDate(bookId)

    suspend fun getBookFinishedDate(bookId: String): LocalDate? =
        bookRecordDao.getFirstFinishedDate(bookId)

    suspend fun getBookFirstReadDateMap(): Map<String, LocalDate> =
        bookRecordDao.getFirstReadDates().associate { it.bookId to it.date }

    suspend fun getBookFirstFinishedDateMap(): Map<String, LocalDate> =
        bookRecordDao.getFirstFinishedDates().associate { it.bookId to it.date }

    suspend fun getBookFavoriteDateMap(): Map<String, LocalDate> =
        bookRecordDao.getFirstFavoritedDates().associate { it.bookId to it.date }

    private fun createRecordEntity(bookId: String, date: LocalDate): BookRecordEntity =
        BookRecordEntity(
            bookId = bookId,
            date = date,
            reads = 0,
            seconds = 0,
            isFinished = false,
            isFavorited = false,
            firstSeen = LocalTime.now(),
            lastSeen = LocalTime.now(),
        )

    private fun updateCount(count: Count, update: ReadingStatsUpdate): Count {
        val minutesDelta = update.secondDelta / 60
        if (minutesDelta > 0) {
            val hour = update.localTime.hour
            val totalMinutes = count.getMinute(hour) + minutesDelta
            count.setMinute(hour, totalMinutes.coerceAtMost(60))
        }
        return count
    }

    fun clear() {
        bookRecordDao.clear()
        dailyCountDao.clear()
    }
}
