package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf

import androidx.compose.runtime.Stable
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfBookMetadata

@Stable
data class BookshelfBookItem(
    val id: String,
    val bookshelfBookMetadata: BookshelfBookMetadata?,
    val bookInformation: BookInformation,
    val lastUpdatedChapterTitle: String? = null
)