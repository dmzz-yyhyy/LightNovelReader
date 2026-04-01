package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import java.time.LocalTime

data class ReadingStatsUpdate(
    val bookId: String,
    val secondDelta: Int = 0,
    val readEventDelta: Int = 0,
    val localTime: LocalTime = LocalTime.now()
)