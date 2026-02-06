package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import java.time.LocalDate

data class ReadingStatisticsEntity(
    val date: LocalDate,
    val readingTimeCount: Count
)
