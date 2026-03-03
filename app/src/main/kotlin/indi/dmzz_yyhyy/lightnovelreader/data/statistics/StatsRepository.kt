package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

data class ReadingStatsUpdate(
    val bookId: String,
    val secondDelta: Int = 0,
    val readEventDelta: Int = 0,
    val localTime: LocalTime = LocalTime.now()
)

data class TotalReadingSummary(
    val totalMinutes: Int,
    val totalReadCount: Int
)

@Singleton
class StatsRepository @Inject constructor(
    private val bookRecordDao: BookRecordDao
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

    fun getAllReadingStats(): List<DailyReadingStats> {
        val recordsMap = bookRecordDao.getAllBookRecords().groupBy { it.date }
        val allDates = recordsMap.keys.sorted()

        return allDates.map { date ->
            val records = recordsMap[date].orEmpty()
            val totalCount = mergeCounts(records.map { it.readingTimeCount })
            ReadingStatisticsEntity(date = date, readingTimeCount = totalCount)
                .toDailyStatsData(records)
        }
    }

    fun importReadingStats(data: AppUserDataContent) {
        data.readingStatsData?.forEach { dailyStats ->
            dailyStats.bookRecords.forEach {
                bookRecordDao.insertBookRecord(it.toEntity())
            }
        }
    }

    suspend fun getReadingStatistics(start: LocalDate, end: LocalDate? = null): Map<LocalDate, ReadingStatisticsEntity> {
        return if (end == null) {
            val records = bookRecordDao.getBookRecordsForDate(start)
            val mergedCount = mergeCounts(records.map { it.readingTimeCount })
            mapOf(start to ReadingStatisticsEntity(start, mergedCount))
        }
        else {
            val allDates = generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .toList()

            val fetched = bookRecordDao.getBookRecordsBetweenDates(allDates.first(), allDates.last())
                .groupBy { it.date }

            allDates.associateWith { date ->
                val records = fetched[date].orEmpty()
                val mergedCount = mergeCounts(records.map { it.readingTimeCount })
                ReadingStatisticsEntity(date, mergedCount)
            }
        }
    }

    suspend fun getBookRecords(
        start: LocalDate,
        end: LocalDate? = null
    ): Map<LocalDate, List<BookRecordEntity>> {
        val raw = if (end == null) {
            val records = bookRecordDao.getBookRecordsForDate(start)
            mapOf(start to records)
        }
        else {
            bookRecordDao.getBookRecordsBetweenDates(start, end).groupBy { it.date }
        }

        return raw.filterValues { it.isNotEmpty() }
    }

    fun createStatsEntity(date: LocalDate) = ReadingStatisticsEntity(
        date = date,
        readingTimeCount = Count()
    )

    fun getTotalReadingSummary(): TotalReadingSummary {
        val records = bookRecordDao.getAllBookRecords()
        val totalMinutes = records.sumOf { it.readingTimeCount.getTotalMinutes() }
        val totalReadCount = records.sumOf { it.readCount }
        return TotalReadingSummary(
            totalMinutes = totalMinutes,
            totalReadCount = totalReadCount
        )
    }

    suspend fun updateReadingStatistics(update: ReadingStatsUpdate) {
        val today = LocalDate.now()
        val existingRecord = bookRecordDao.getBookRecordByIdAndDate(update.bookId, today)
            ?: createRecordEntity(update.bookId, today)

        val updatedRecord = existingRecord.copy(
            readingTimeCount = updateCount(existingRecord.readingTimeCount, update),
            readCount = existingRecord.readCount + update.readEventDelta
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

    suspend fun getBookFirstReadDate(bookId: String): LocalDate? =
        bookRecordDao.getFirstReadDate(bookId)

    suspend fun getBookFinishedDate(bookId: String): LocalDate? =
        bookRecordDao.getFirstFinishedDate(bookId)

    suspend fun getBookFirstReadDateMap(): Map<String, LocalDate> =
        bookRecordDao.getFirstReadDates().associate { it.bookId to it.date }

    suspend fun getBookFirstFinishedDateMap(): Map<String, LocalDate> =
        bookRecordDao.getFirstFinishedDates().associate { it.bookId to it.date }

    private fun createRecordEntity(bookId: String, date: LocalDate): BookRecordEntity = BookRecordEntity(
        bookId = bookId,
        date = date,
        readingTimeCount = Count(),
        readCount = 0,
        isFinished = false
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

    private fun mergeCounts(counts: Iterable<Count>): Count {
        return counts.fold(Count()) { acc, count -> acc + count }
    }

    fun clear() {
        bookRecordDao.clear()
    }
}
