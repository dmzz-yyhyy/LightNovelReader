package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.michaelbull.result.Result
import androidx.compose.runtime.Stable
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadItem
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.RelatedBookKind
import io.nightfish.lightnovelreader.api.book.RelatedBooksRequest
import io.nightfish.lightnovelreader.api.book.UserReadingData
import io.nightfish.lightnovelreader.api.error.WebRequestError

@Stable
interface DetailUiState {
    val bookInformation: Result<BookInformation, WebRequestError>?
    val relatedBooks: DetailRelatedBooks?
    val bookVolumes: Result<BookVolumes, WebRequestError>?
    val userReadingData: UserReadingData?
    val isCached: Boolean
    val downloadItem: DownloadItem?
    val isInBookshelf: Boolean
}

class MutableDetailUiState : DetailUiState {
    override var bookInformation: Result<BookInformation, WebRequestError>? by mutableStateOf(null)
    override var relatedBooks: DetailRelatedBooks? by mutableStateOf(null)
    override var bookVolumes: Result<BookVolumes, WebRequestError>? by mutableStateOf(null)
    override var userReadingData: UserReadingData? by mutableStateOf(null)
    override var isCached: Boolean by mutableStateOf(false)
    override var downloadItem: DownloadItem? by mutableStateOf(null)
    override var isInBookshelf: Boolean by mutableStateOf(false)
}

data class DetailBookTag(val value: String, val request: RelatedBooksRequest?)

data class DetailRelatedBooks(
    val sourceId: String?,
    val authorRequest: RelatedBooksRequest?,
    val tags: List<DetailBookTag>,
)

internal fun relatedBooksForDetail(
    sourceId: String?,
    bookId: String,
    author: String,
    tags: List<String>,
    supportedKinds: Set<RelatedBookKind>,
): DetailRelatedBooks {
    fun request(kind: RelatedBookKind, value: String) =
        if (sourceId != null && kind in supportedKinds && value.isNotBlank())
            RelatedBooksRequest(bookId, kind, value)
        else null

    return DetailRelatedBooks(
        sourceId = sourceId,
        authorRequest = request(RelatedBookKind.AUTHOR, author),
        tags = tags.map { DetailBookTag(it, request(RelatedBookKind.TAG, it)) },
    )
}
