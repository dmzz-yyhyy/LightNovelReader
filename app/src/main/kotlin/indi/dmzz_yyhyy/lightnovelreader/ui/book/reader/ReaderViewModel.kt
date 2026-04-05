package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.ReadingStatsUpdate
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip.FlipPageContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll.ScrollContentViewModel
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository,
    val contentComponentRepository: ContentComponentRepository
) : ViewModel() {
    val settingState = SettingState(userDataRepository, viewModelScope)
    private var contentViewModel: ContentViewModel by mutableStateOf(ContentViewModel.empty)
    private val _uiState = MutableReaderScreenUiState(contentViewModel.uiState)
    val uiState: ReaderScreenUiState = _uiState
    private val readingBookListUserData =
        userDataRepository.stringListUserData(UserDataPath.ReadingBooks.path)
    var bookId = ""
        set(value) {
            field = value
            _uiState.bookId = value
            contentViewModel.changeBookId(value)
            addToReadingBook(value)
            viewModelScope.launch(Dispatchers.IO) {
                statsRepository.updateReadingStatistics(
                    ReadingStatsUpdate(
                        bookId = value,
                        readEventDelta = 1
                    )
                )
            }

            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookVolumesFlow(value).collect { _uiState.bookVolumes = it }
            }
        }
    private var chapterId = ""
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        viewModelScope.launch {
            settingState.isUsingFlipPageUserData.getFlowWithDefault(false).collect {
                if (it && contentViewModel !is FlipPageContentViewModel) {
                    contentViewModel = FlipPageContentViewModel(
                        bookRepository = bookRepository,
                        coroutineScope = viewModelScope,
                        updateReadingProgress = ::saveReadingProgress,
                        contentComponentRepository = contentComponentRepository
                    )
                    contentViewModel.changeBookId(bookId)
                    contentViewModel.changeChapter(chapterId)
                    _uiState.contentUiState = contentViewModel.uiState
                }
                else if (!it && contentViewModel !is ScrollContentViewModel) {
                    contentViewModel = ScrollContentViewModel(
                        bookRepository = bookRepository,
                        coroutineScope = viewModelScope,
                        settingState = settingState,
                        updateReadingProgress = ::saveReadingProgress,
                        contentComponentRepository = contentComponentRepository
                    )
                    contentViewModel.changeBookId(bookId)
                    contentViewModel.changeChapter(chapterId)
                    _uiState.contentUiState = contentViewModel.uiState
                }
            }
        }
    }

    fun prevChapter() = contentViewModel.loadLastChapter()

    fun nextChapter() = contentViewModel.loadNextChapter()

    fun changeChapter(chapterId: String) {
        this.chapterId = chapterId
        contentViewModel.changeChapter(chapterId)
    }

    private fun saveReadingProgress(chapterId: String, progress: Float) {
        if (progress.isNaN() || progress <= 0f || bookId.isBlank()) return
        val title = _uiState.contentUiState.readingChapterContent.title
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = LocalDateTime.now()

            bookRepository.updateUserReadingData(bookId) { userReadingData ->
                Log.v("ReaderViewModel", "$bookId/$chapterId Saving progress $progress. (${_uiState.contentUiState.readingChapterContent.title})")
                userReadingData.apply {
                    lastReadTime = currentTime
                    lastReadChapterId = chapterId
                    lastReadChapterTitle = title
                    userReadingData.updateChapterReadingProgress(chapterId, progress)
                    val total = _uiState.bookVolumes.volumes.sumOf { it.chapters.size }
                    if (total > 0) {
                        readingProgress = (userReadingData.maxChapterReadingProgressMap.values.sum() / total).coerceIn(0f, 1f)
                    }
                }
            }
            val readingData = bookRepository.getUserReadingData(bookId)
            if (readingData.readingProgress >= 1f) {
                statsRepository.markBookFinished(bookId)
            }
        }
    }


    fun updateTotalReadingTime(bookId: String, totalReadingTime: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) {
                it.apply {
                    lastReadTime = LocalDateTime.now()
                    totalReadTime = it.totalReadTime + totalReadingTime
                }
            }
        }
    }

    private fun addToReadingBook(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            readingBookListUserData.update {
                val newList = it.toMutableList()
                if (it.contains(bookId))
                    newList.remove(bookId)
                newList.add(bookId)
                return@update newList
            }
        }
    }

    fun accumulateReadingTime(bookId: String, seconds: Int) {
        if (bookId.isBlank()) return
        coroutineScope.launch(Dispatchers.IO) {
            statsRepository.accumulateBookReadTime(bookId, seconds)
        }
    }
}