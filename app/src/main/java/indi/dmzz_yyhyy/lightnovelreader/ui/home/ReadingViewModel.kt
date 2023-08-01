package indi.dmzz_yyhyy.lightnovelreader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ReadingBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val readingBookRepository: ReadingBookRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReadingUiState())
    val uiState: StateFlow<ReadingUiState> = _uiState
    init {
        _uiState.update {
                readingUiState -> readingUiState.copy(readingBookDataList = readingBookRepository.readingBookList.value)
        }
        viewModelScope.launch {
            readingBookRepository.readingBookList.collect{
                _uiState.update {
                        readingUiState -> readingUiState.copy(readingBookDataList = readingBookRepository.readingBookList.value)

                }
            }
        }
    }
}