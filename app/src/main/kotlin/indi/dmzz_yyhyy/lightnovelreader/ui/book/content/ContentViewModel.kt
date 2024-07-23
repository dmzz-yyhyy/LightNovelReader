package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class ContentViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _uiState = MutableContentScreenUiState()
    private var _bookId: Int = -1
    val uiState: ContentScreenUiState = _uiState

    fun init(bookId: Int, chapterId: Int) {
        _bookId = bookId
        viewModelScope.launch {
            val chapterContent = bookRepository.getChapterContent(
                chapterId = chapterId,
                bookId = bookId)
            _uiState.chapterContent = chapterContent.first()
            _uiState.isLoading = _uiState.chapterContent.id == -1
            chapterContent.collect {
                if (it.id == -1) return@collect
                _uiState.chapterContent = it
                _uiState.isLoading = _uiState.chapterContent.id == -1
            }
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
}