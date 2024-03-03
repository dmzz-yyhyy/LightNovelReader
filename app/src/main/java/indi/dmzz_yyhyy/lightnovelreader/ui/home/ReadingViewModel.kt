package indi.dmzz_yyhyy.lightnovelreader.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ReadingBookRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.ReaderActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val readingBookRepository: ReadingBookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReadingUiState())
    val uiState: StateFlow<ReadingUiState> = _uiState

    init {
        _uiState.update { readingUiState ->
            readingUiState.copy(readingBookDataList = readingBookRepository.readingBookMetadataList.value)
        }
        viewModelScope.launch {
            while (true) {
                readingBookRepository.readingBookMetadataList.collect {
                    _uiState.update { readingUiState ->
                        readingUiState.copy(readingBookDataList = readingBookRepository.readingBookMetadataList.value)

                    }
                }
            }
        }
    }

    fun onCardClick(bookId: Int, context: Context) {
        val intent = Intent(context, ReaderActivity::class.java)
        intent.putExtra("id", bookId)
        startActivity(context, intent, Bundle())
    }
}