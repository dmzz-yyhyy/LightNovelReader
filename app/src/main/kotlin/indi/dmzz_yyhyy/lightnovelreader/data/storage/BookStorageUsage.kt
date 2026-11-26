package indi.dmzz_yyhyy.lightnovelreader.data.storage

import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.Serializable

@Serializable
data class BookStorageUsage(
    val bookId: Identifier.Book,
    val bookInformationBytes: Long,
    val volumeBytes: Long,
    val chapterInformationBytes: Long,
    val chapterContentBytes: Long,
) {
    val totalBytes: Long
        get() = bookInformationBytes + volumeBytes + chapterInformationBytes + chapterContentBytes
}
