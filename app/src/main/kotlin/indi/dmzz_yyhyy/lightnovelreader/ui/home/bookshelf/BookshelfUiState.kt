package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf

import androidx.compose.runtime.Stable
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.zip
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Stable
data class BookshelfUiState(
    val id: Int,
    val name: String,
    val sortType: BookshelfSortType,
    val sortReversed: Boolean,
    val autoCache: Boolean,
    val systemUpdateReminder: Boolean,
    val allBookFlows: List<Pair<String, Flow<Result<BookshelfBookItem, WebRequestError>>>>,
    val pinnedBookFlows: List<Pair<String, Flow<Result<BookshelfBookItem, WebRequestError>>>>,
    val updatedBookFlows: List<Pair<String, Flow<Result<BookshelfBookItem, WebRequestError>>>>
)

fun Bookshelf.toBookshelfUiState(bookRepository: BookRepository, bookshelfRepository: BookshelfRepository) = BookshelfUiState(
    id = this.id,
    name = this.name,
    sortType = this.sortType,
    sortReversed = this.sortReversed,
    autoCache = this.autoCache,
    systemUpdateReminder = this.systemUpdateReminder,
    allBookFlows = this.allBookIds.map { id ->
        val bookInformationFlow = bookRepository.getBookInformationFlow(id)
        val bookshelfBookMetadataFlow = bookshelfRepository.getBookshelfBookMetadataFlow(id)
        id to bookInformationFlow.combine(bookshelfBookMetadataFlow) { result, bookshelfBookMetadata ->
            result.map {
                BookshelfBookItem(
                    id = id,
                    bookshelfBookMetadata = bookshelfBookMetadata,
                    bookInformation = it,
                )
            }
        }
    },
    pinnedBookFlows = this.pinnedBookIds.map { id ->
        val bookInformationFlow = bookRepository.getBookInformationFlow(id)
        val bookshelfBookMetadataFlow = bookshelfRepository.getBookshelfBookMetadataFlow(id)
        id to bookInformationFlow.combine(bookshelfBookMetadataFlow) { result, bookshelfBookMetadata ->
            result.map {
                BookshelfBookItem(
                    id = id,
                    bookshelfBookMetadata = bookshelfBookMetadata,
                    bookInformation = it,
                )
            }
        }
    },
    updatedBookFlows = this.updatedBookIds.map { id ->
        val bookInformationFlow = bookRepository.getBookInformationFlow(id)
        val bookVolumesFlow = bookRepository.getBookVolumesFlow(id)
        val bookshelfBookMetadataFlow = bookshelfRepository.getBookshelfBookMetadataFlow(id)
        id to combine(bookshelfBookMetadataFlow, bookInformationFlow, bookVolumesFlow) { bookshelfBookMetadata, bookInformationResult, bookVolumesResult ->
            zip({ bookInformationResult }, { bookVolumesResult }) { bookInformation, bookVolumes ->
                BookshelfBookItem(
                    id = id,
                    bookshelfBookMetadata = bookshelfBookMetadata,
                    bookInformation = bookInformation,
                    lastUpdatedChapterTitle = bookVolumes.volumes.lastOrNull()?.let { volume ->
                        val title = volume.chapters.lastOrNull()?.title ?: return@let null
                        "${volume.volumeTitle} $title"
                    }
                )
            }
        }
    }
)