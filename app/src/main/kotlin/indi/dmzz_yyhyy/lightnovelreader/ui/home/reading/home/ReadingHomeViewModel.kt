package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.lightnovelreader.api.userdata.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingHomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {

    private val readingBooksUserData =
        userDataRepository.userData(UserDataPath.ReadingBooks)

    var recentReadingBooks: List<Pair<Identifier.Book, Flow<RecentReadingBook>>> by mutableStateOf(
        emptyList()
    )
        private set

    var chapterSheetUiState by mutableStateOf<ChapterSheetUiState?>(null)
        private set

    init {
        viewModelScope.launch {
            readingBooksUserData.getFlowWithDefault(emptyList()).collect { ids ->
                recentReadingBooks = ids
                    .reversed()

                    .map { id ->
                        val bookInformationFlow = bookRepository.getBookInformationFlow(id)
                        val userReadingDataFlow = bookRepository.getUserReadingDataFlow(id)
                        id to bookInformationFlow
                            .combine(userReadingDataFlow) { bookInformationResult, userReadingData ->
                                RecentReadingBook(
                                    id = id,
                                    bookInformationResult = bookInformationResult,
                                    userReadingData = userReadingData
                                )
                            }
                    }
            }
        }
    }

    fun openChapters(bookId: Identifier.Book) {
        viewModelScope.launch(Dispatchers.IO) {
            val userData = bookRepository.getUserReadingData(bookId)
            chapterSheetUiState = ChapterSheetUiState(
                bookId = bookId,
                readingChapterId = userData.lastReadChapterId ?: return@launch,
                bookVolumeFlow = bookRepository.getBookVolumesFlow(bookId)
            )
        }
    }

    fun setVolume(volumeId: Identifier.Volume?) {
        chapterSheetUiState = chapterSheetUiState?.copy(selectedVolumeId = volumeId)
    }

    fun closeContents() {
        chapterSheetUiState = null
    }

    fun removeFromReadingList(bookId: Identifier.Book) {
        viewModelScope.launch(Dispatchers.IO) {
            readingBooksUserData.update {
                it.toMutableList().apply { remove(bookId) }
            }
        }
    }

    fun addToReadingList(bookId: Identifier.Book) {
        viewModelScope.launch(Dispatchers.IO) {
            readingBooksUserData.update {
                it + listOf(bookId)
            }
        }
    }
}
