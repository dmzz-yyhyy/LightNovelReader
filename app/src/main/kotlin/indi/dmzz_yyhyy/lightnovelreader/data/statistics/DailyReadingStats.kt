package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.serialier.CountSerializer
import indi.dmzz_yyhyy.lightnovelreader.data.serialier.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class DailyReadingStats(
    @Serializable(LocalDateSerializer::class)
    val date: LocalDate,
    @Serializable(CountSerializer::class)
    val readingTimeCount: Count,
    val foregroundTime: Int,
    val favoriteBooks: List<String>,
    val startedBooks: List<String>,
    val finishedBooks: List<String>,
    val bookRecords: List<BookRecordData>
)

