package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ReaderRepository
import indi.dmzz_yyhyy.lightnovelreader.data.ReadingBookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.ReadingBook
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val readerRepository: ReaderRepository,
    private val readingBookRepository: ReadingBookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState

    init {
        viewModelScope.launch {
            readerRepository.loadChapterContent()
        }
        viewModelScope.launch {
            readerRepository.chapterContent.collect {
                if (readerRepository.chapterContent.value.content != "") {
                    _uiState.update { readerUiState ->
                        readerUiState.copy(
                            isLoading = false,
                            title = readerRepository.chapterContent.value.title,
                            content = readerRepository.chapterContent.value.content
                        )
                    }
                    Log.d("Reader", "ChapterContent collected, title=" + readerRepository.chapterContent.value.title)
                }
            }
        }
        viewModelScope.launch {
            readerRepository.chapterContentId.collect {
                _uiState.update { readerUiState ->
                    readerUiState.copy(
                        isLoading = true,
                        chapterId = readerRepository.chapterContentId.value,
                        title = readerRepository.chapterContent.value.title,
                        content = readerRepository.chapterContent.value.content
                    )
                }
            }
        }
        viewModelScope.launch {
            if (!readingBookRepository.isBookInList(readerRepository.book.value.bookID)) {
                readingBookRepository.addReadingBook(ReadingBook(
                    bookId = readerRepository.book.value.bookID,
                    bookName = readerRepository.book.value.bookName,
                    coverUrl = readerRepository.book.value.coverUrl,
                    introduction = readerRepository.book.value.introduction
                ))
            }
        }
    }

    private fun changeChapter(chapterId: Int) {
        readerRepository.setChapterContentId(chapterId)
        _uiState.update { readerUiState ->
            readerUiState.copy(
                isLoading = true,
                chapterId = chapterId,
                title = "",
                content = ""
            )
        }
        viewModelScope.launch {
            readerRepository.loadChapterContent()
            readerRepository.chapterContent.collect {
                if (readerRepository.chapterContent.value.content != "") {
                    Log.d("Web", "content got")
                    Log.d("Web", "title: ${readerRepository.chapterContent.value.title}")
                    _uiState.update { readerUiState ->
                        readerUiState.copy(
                            isLoading = false,
                            title = readerRepository.chapterContent.value.title,
                            content = readerRepository.chapterContent.value.content
                        )
                    }
                }
            }
        }
    }

    fun onClickMiddle(isBottomBarOpen: Boolean) {
        if (isBottomBarOpen) {
            _uiState.update { readerUiState ->
                readerUiState.copy(
                    isBottomBarOpen = false
                )
            }
        }
    }

    fun onClickBelow() {
        _uiState.update { readerUiState ->
            readerUiState.copy(
                isBottomBarOpen = true
            )
        }
    }

    fun onClickMenuButton() {
        _uiState.update { readerUiState ->
            readerUiState.copy(
                isBottomBarOpen = false,
                isSideSheetsOpen = true,
                volumeList = readerRepository.volumeList.value
            )
        }
    }

    fun onClickNextButton(chapterId: Int) {
        changeChapter(chapterId + 1)
    }

    fun onClickBeforeButton(chapterId: Int) {
        changeChapter(chapterId - 1)
    }

    fun onClickBackButton(navController: NavController) {
        navController.popBackStack()
    }

    fun onCloseChapterSideSheets() {
        _uiState.update { readerUiState ->
            readerUiState.copy(
                isSideSheetsOpen = false
            )
        }
    }
}
