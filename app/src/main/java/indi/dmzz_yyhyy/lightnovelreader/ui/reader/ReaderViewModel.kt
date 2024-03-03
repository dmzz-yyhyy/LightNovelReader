package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ReaderRepository
import indi.dmzz_yyhyy.lightnovelreader.data.ReadingBookRepository
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
        // 获取书本类容并更新UiState
        viewModelScope.launch {
            readerRepository.loadChapterContent()
            _uiState.update { readerUiState ->
                readerUiState.copy(
                    isLoading = false,
                    chapterId = readerRepository.chapterContentId.value,
                    title = readerRepository.chapterContent.value.title,
                    content = readerRepository.chapterContent.value.content
                )
            }
            Log.d("Reader", "ChapterContent loaded, name=" + readerRepository.chapterContent.value.title)
        }
        // 将书本列入正在阅读列表
        viewModelScope.launch {
            if (!readingBookRepository.isBookInList(readerRepository.book.value.id)) {
                readingBookRepository.addReadingBook(readerRepository.book.value.toBookMeatData())
            }
        }
    }

    private fun changeChapter(chapterId: Int) {
        // 设置章节
        readerRepository.setChapterContentId(chapterId)
        // 归零UiState
        _uiState.update { readerUiState ->
            readerUiState.copy(
                isLoading = true,
                chapterId = chapterId,
                title = "",
                content = ""
            )
        }
        // 侦听章节类容更新并更新UiState
        viewModelScope.launch {
            readerRepository.loadChapterContent()
            readerRepository.chapterContent.collect {
                if (readerRepository.chapterContent.value.content != "") {
                    Log.d("Web", "content got")
                    Log.d("Web", "name: ${readerRepository.chapterContent.value.title}")
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

    fun onClickText(isAppBarVisible: Boolean) {

        _uiState.update { readerUiState ->
            if (isAppBarVisible) {
                readerUiState.copy(
                    isAppBarVisible = false,
                    isBottomBarOpen = false
                )
            } else {
                readerUiState.copy(
                    isAppBarVisible = true,
                    isBottomBarOpen = true
            ) }
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

    fun onClickChangeChapter(chapterId: Int) {
        changeChapter(chapterId)
    }
}
