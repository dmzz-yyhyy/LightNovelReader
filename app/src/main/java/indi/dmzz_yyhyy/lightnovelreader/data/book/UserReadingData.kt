package indi.dmzz_yyhyy.lightnovelreader.data.book

import java.time.LocalDateTime

data class UserReadingData(
    val lastReadTime: LocalDateTime,
    val totalReadTime: Int,
    val readingProgress: Double,
    val lastReadChapterId: Int,
    val lastReadChapterTitle: String,
    val lastReadChapterProgress: Double
)
