package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.serialier.LocalDateSerializer
import indi.dmzz_yyhyy.lightnovelreader.data.serialier.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class BookRecordData(
    val id: Int? = null,
    @Serializable(LocalDateSerializer::class)
    val date: LocalDate,
    val bookId: String,
    val sessions: Int,
    val totalTime: Int,
    @Serializable(LocalTimeSerializer::class)
    val firstSeen: LocalTime,
    @Serializable(LocalTimeSerializer::class)
    val lastSeen: LocalTime
)

