package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ReaderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ReaderViewModel  @Inject constructor(
    private val readerRepository: ReaderRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState
    init {
        viewModelScope.launch {
            readerRepository.loadChapterContent()
            readerRepository.chapterContent.collect {
                Log.d("Web", "content got")
                _uiState.update {
                        readerUiState -> readerUiState.copy(
                            isLoading = false,
                            title = readerRepository.chapterContent.value.title,
                            content = readerRepository.chapterContent.value.content
                        )
                }
            }
        }
        viewModelScope.launch {
            readerRepository.chapterContentId.collect {
                _uiState.update {
                        readerUiState -> readerUiState.copy(
                    isLoading = true,
                    chapterId = readerRepository.chapterContentId.value,
                    title = readerRepository.chapterContent.value.title,
                    content = readerRepository.chapterContent.value.content
                )
                }
            }
        }
    }
    fun onClickMiddle(isBottomBarOpen: Boolean){
        if(isBottomBarOpen){
            _uiState.update {
                readerUiState -> readerUiState.copy(
                    isBottomBarOpen = false
                )
            }
        }
    }
    fun onClickBelow(){
        _uiState.update {
            readerUiState ->  readerUiState.copy(
                isBottomBarOpen = true
            )
        }
    }

    fun onClickNextButton(chapterId: Int) {
        readerRepository.setChapterContentId(chapterId+1)
        _uiState.update {
                readerUiState -> readerUiState.copy(
            isLoading = true,
            chapterId = chapterId+1,
            title = "",
            content = ""
        )
        }
        viewModelScope.launch {
            readerRepository.loadChapterContent()
            readerRepository.chapterContent.collect {
                Log.d("Web", "content got")
                _uiState.update {
                        readerUiState -> readerUiState.copy(
                    isLoading = false,
                    title = readerRepository.chapterContent.value.title,
                    content = readerRepository.chapterContent.value.content
                )
                }
            }
        }
    }
}