package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class ContentViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val _uiState = MutableContentScreenUiState()
    private var _bookId: Int = -1
    private val fontSizeUserData = userDataRepository.floatUserData(UserDataPath.Reader.FontSize.path)
    private val fontLineHeightUserData = userDataRepository.floatUserData(UserDataPath.Reader.FontLineHeight.path)
    private val keepScreenOnUserData = userDataRepository.booleanUserData(UserDataPath.Reader.KeepScreenOn.path)
    private val readingBookListUserData = userDataRepository.intListUserData(UserDataPath.ReadingBooks.path)
    val uiState: ContentScreenUiState = _uiState

    fun init(bookId: Int, chapterId: Int) {
        if (bookId != _bookId) {
            viewModelScope.launch {
                val bookVolumes = bookRepository.getBookVolumes(bookId)
                _uiState.bookVolumes = bookVolumes.first()
                viewModelScope.launch {
                    bookVolumes.collect {
                        if (it.volumes.isEmpty()) return@collect
                        _uiState.bookVolumes = it
                    }
                }
            }
        }
        _bookId = bookId
        viewModelScope.launch {
            val chapterContent = bookRepository.getChapterContent(
                chapterId = chapterId,
                bookId = bookId)
            _uiState.chapterContent = chapterContent.first()
            _uiState.isLoading = _uiState.chapterContent.id == -1
            bookRepository.updateUserReadingData(bookId) {
                it.copy(
                    lastReadTime = LocalDateTime.now(),
                    lastReadChapterId = chapterId,
                    lastReadChapterTitle = _uiState.chapterContent.title,
                    lastReadChapterProgress = if (it.lastReadChapterId == chapterId) it.lastReadChapterProgress else 0f,
                )
            }
            chapterContent.collect { content ->
                if (content.id == -1) return@collect
                _uiState.chapterContent = content
                _uiState.isLoading = _uiState.chapterContent.id == -1
                bookRepository.updateUserReadingData(bookId) {
                    it.copy(
                        lastReadTime = LocalDateTime.now(),
                        lastReadChapterId = chapterId,
                        lastReadChapterTitle = _uiState.chapterContent.title,
                        lastReadChapterProgress = if (it.lastReadChapterId == chapterId) it.lastReadChapterProgress else 0f,
                    )
                }
            }
        }
        viewModelScope.launch {
            bookRepository.getUserReadingData(bookId).collect {
                _uiState.userReadingData = it
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.fontSize = fontSizeUserData.getOrDefault(_uiState.fontSize)
            _uiState.fontLineHeight = fontLineHeightUserData.getOrDefault(_uiState.fontLineHeight)
            _uiState.keepScreenOn = keepScreenOnUserData.getOrDefault(_uiState.keepScreenOn)
        }
    }

    fun lastChapter() {
        if (!_uiState.chapterContent.hasLastChapter()) return
        _uiState.isLoading = true
        viewModelScope.launch {
            init(
                bookId = _bookId,
                chapterId = _uiState.chapterContent.lastChapter
            )
        }
    }

    fun nextChapter() {
        if (!_uiState.chapterContent.hasNextChapter()) return
        _uiState.isLoading = true
        viewModelScope.launch {
            init(
                bookId = _bookId,
                chapterId = _uiState.chapterContent.nextChapter
            )
        }
    }

    fun changeChapter(chapterId: Int) {
        _uiState.isLoading = true
        viewModelScope.launch {
            init(
                bookId = _bookId,
                chapterId = chapterId
            )
        }
    }

    fun changeChapterReadingProgress(bookId: Int, progress: Float) {
        if (progress.isNaN() || progress == 0.0f) return
        viewModelScope.launch {
            bookRepository.updateUserReadingData(bookId) {
                it.copy(
                    lastReadTime = LocalDateTime.now(),
                    lastReadChapterProgress = progress
                )
            }
        }
    }

    fun updateTotalReadingTime(bookId: Int, totalReadingTime: Int) {
        viewModelScope.launch {
            bookRepository.updateUserReadingData(bookId) {
                it.copy(
                    lastReadTime = LocalDateTime.now(),
                    totalReadTime = it.totalReadTime + totalReadingTime
                )
            }
        }
    }

    fun changeFontSize(size: Float) {
        _uiState.fontSize = size
    }

    fun changeFontLineHeight(height: Float) {
        _uiState.fontLineHeight = height
    }

    fun saveFontSize() {
        viewModelScope.launch(Dispatchers.IO) {
            fontSizeUserData.set(_uiState.fontSize)
        }
    }

    fun saveFontLineHeight() {
        viewModelScope.launch(Dispatchers.IO) {
            fontLineHeightUserData.set(_uiState.fontLineHeight)
        }
    }

    fun changeKeepScreenOn(keepScreenOn: Boolean) {
        _uiState.keepScreenOn = keepScreenOn
        viewModelScope.launch(Dispatchers.IO) {
            keepScreenOnUserData.set(keepScreenOn)
        }
    }

    fun addToReadingBook(bookId: Int) {
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
}