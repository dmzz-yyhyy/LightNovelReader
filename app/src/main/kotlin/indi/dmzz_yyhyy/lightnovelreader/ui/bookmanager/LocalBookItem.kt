package indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager

import io.nightfish.lightnovelreader.api.book.BookInformation
import java.time.LocalDateTime

data class LocalBookItem(
    val id: String,
    val bookInformation: BookInformation,
    val size: Long,
    val chapterCount: Int,
    val volumeCount: Int,
    val lastReadTime: LocalDateTime? = null,
    val bookInformationBytes: Long = 0L,
    val volumeBytes: Long = 0L,
    val chapterInformationBytes: Long = 0L,
    val chapterContentBytes: Long = 0L,
    val readingRecordBytes: Long = 0L
) {
    val hasChapterContent: Boolean
        get() = chapterContentBytes > 0L

    fun bytesOf(target: LocalBookClearTarget): Long = when (target) {
        LocalBookClearTarget.VolumeAndChapterIndex -> volumeBytes + chapterInformationBytes
        LocalBookClearTarget.ChapterContent -> chapterContentBytes
        LocalBookClearTarget.ReadingRecord -> readingRecordBytes
    }
}
