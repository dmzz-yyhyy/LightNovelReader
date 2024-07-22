package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ContentViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _uiState = MutableContentScreenUiState()
    val uiState: ContentScreenUiState = _uiState

    fun init(bookId: Int, chapterId: Int) {
        viewModelScope.launch {
            val chapterContent = bookRepository.getChapterContent(bookId, chapterId)
            _uiState.chapterContent = chapterContent.first()
            _uiState.isLoading = _uiState.chapterContent.id == -1
            chapterContent.collect {
                if (it.id == -1) return@collect
                _uiState.chapterContent = it
                _uiState.isLoading = false
            }
        }
    }
}