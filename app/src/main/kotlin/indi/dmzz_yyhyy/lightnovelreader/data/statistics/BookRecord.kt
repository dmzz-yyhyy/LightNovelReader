package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import androidx.compose.runtime.Stable
import java.time.LocalDate
import java.time.LocalTime

@Stable
data class BookRecord(
    val bookId: String,
    val date: LocalDate,
    val reads: Int,
    val seconds: Int,
    val isFinished: Boolean = false,
    val isFavorited: Boolean = false,
    val firstSeen: LocalTime,
    val lastSeen: LocalTime,
)
