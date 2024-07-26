package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _uiState = MutableDetailUiState()
    val uiState: DetailUiState = _uiState

    fun init(bookId: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            val bookInformation = bookRepository.getBookInformation(bookId)
            _uiState.bookInformation = bookInformation.first()
            _uiState.isLoading = _uiState.bookInformation.id == -1
            viewModelScope.launch {
                bookInformation.collect {
                    if (it.id == -1) return@collect
                    _uiState.bookInformation = it
                    _uiState.isLoading = _uiState.bookInformation.id == -1
                }
            }
            val bookVolumes = bookRepository.getBookVolumes(bookId)
            _uiState.bookVolumes = bookVolumes.first()
            _uiState.isLoading = _uiState.bookVolumes.volumes.isEmpty()
            viewModelScope.launch {
                bookVolumes.collect {
                    if (it.volumes.isEmpty()) return@collect
                    _uiState.bookVolumes = it
                    println(it)
                    _uiState.isLoading = _uiState.bookVolumes.volumes.isEmpty()
                }
            }
            val userReadingData = bookRepository.getUserReadingData(bookId)
            _uiState.userReadingData = userReadingData.first()
            viewModelScope.launch {
                userReadingData.collect {
                    _uiState.userReadingData = it
                }
            }
        }
    }
}