package indi.dmzz_yyhyy.lightnovelreader.data.book

import java.time.LocalDateTime

data class UserReadingData(
    val id: Int,
    val lastReadTime: LocalDateTime,
    val totalReadTime: Int,
    val readingProgress: Float,
    val lastReadChapterId: Int,
    val lastReadChapterTitle: String,
    val lastReadChapterProgress: Float
) {
    companion object {
        fun empty(): UserReadingData = UserReadingData(
                -1,
                LocalDateTime.MIN,
                -1,
                0.0f,
                -1,
                "",
                0.0f
            )
    }
}
