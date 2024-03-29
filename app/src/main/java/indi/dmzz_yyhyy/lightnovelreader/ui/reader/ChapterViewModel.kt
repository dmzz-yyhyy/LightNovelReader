package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ReaderRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.RouteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChapterViewModel @Inject constructor(
    private val readerRepository: ReaderRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChapterUiState())
    private val _isCloseActivity = MutableStateFlow(false)
    val uiState: StateFlow<ChapterUiState> = _uiState
    val isCloseActivity: StateFlow<Boolean> = _isCloseActivity

    init {
        viewModelScope.launch {
            readerRepository.book.collect {
                Log.d("Debug", "Book data collected: " + readerRepository.bookName)
                _uiState.update { chapterUiState ->
                    chapterUiState.copy(
                        bookName = readerRepository.bookName,
                        bookCoverUrl = readerRepository.bookCoverUrl,
                        bookIntroduction = readerRepository.bookIntroduction
                    )
                }
            }
        }
        viewModelScope.launch {
            readerRepository.volumeList.collect {
                if (readerRepository.volumeList.value.isNotEmpty()) {
                    Log.d("Debug", "Chapters data collected: " + readerRepository.bookName)
                    _uiState.update { chapterUiState ->
                        chapterUiState.copy(
                            isLoading = false,
                            volumeList = readerRepository.volumeList.value
                        )
                    }
                }
            }
        }
    }

    fun onClickChapter(navController: NavController, chapterId: Int) {
        readerRepository.setChapterContentId(chapterId)
        Log.i("Reader", "Loading id=$chapterId")
        navController.navigate(RouteConfig.READER)
    }

    fun onClickReadButton(navController: NavController) {
        readerRepository.loadLastReadChapter()
        navController.navigate(RouteConfig.READER)
    }

    fun onClickBackButton() {
        _isCloseActivity.value = true
    }

    fun onClickChapterSortingButton() {
        _uiState.update { chapterUiState ->
            chapterUiState.copy(
                isChapterReversed = !chapterUiState.isChapterReversed
            )
        }
    }

    fun onClickVolumeSortingButton() {
        _uiState.update { chapterUiState ->
            chapterUiState.copy(
                isVolumeReversed = !chapterUiState.isVolumeReversed
            )
        }
    }
}