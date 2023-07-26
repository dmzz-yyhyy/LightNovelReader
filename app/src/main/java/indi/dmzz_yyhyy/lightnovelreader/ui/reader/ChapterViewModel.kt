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
class ChapterViewModel @Inject constructor(
    private val readerRepository: ReaderRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChapterUiState())
    val uiState: StateFlow<ChapterUiState> = _uiState
    init {
        viewModelScope.launch {
            readerRepository.dataFlow.collect {
                Log.d("Web", "data got")
                Log.d("Debug", "${readerRepository.bookName}")
                _uiState.update {
                        chapterUiState -> chapterUiState.copy(
                            bookName = readerRepository.bookName,
                            bookCoverUrl = readerRepository.bookCoverUrl,
                            bookIntroduction = readerRepository.bookIntroduction,
                            volumeList = readerRepository.volumeList
                        )
                }
            }
        }
    }
}