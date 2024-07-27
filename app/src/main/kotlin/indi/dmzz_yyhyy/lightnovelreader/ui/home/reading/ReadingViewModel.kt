package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataReadingViewModel: UserDataRepository
) : ViewModel() {
    private val _uiState = MutableReadingUiState()
    private val readingBooksUserData = userDataReadingViewModel.intListUserData(UserDataPath.ReadingBooks.path)
    val uiState: ReadingUiState = _uiState

    fun update() {
        viewModelScope.launch(Dispatchers.IO) {
            readingBooksUserData.getFlow().collect {
                if (it == null) return@collect
                updateReadingBooksFromIds(it)
            }
        }
    }

    private fun updateReadingBooksFromIds(ids: List<Int>) {
        _uiState.recentReadingBooks = ids.map {
            ReadingBook(BookInformation.empty(), UserReadingData.empty())
        }.toMutableList()
        for ((index, id) in ids.withIndex()) {
            viewModelScope.launch {
                val bookInformation = bookRepository.getBookInformation(id)
                _uiState.recentReadingBooks[index] =
                    _uiState.recentReadingBooks[index].copy(
                        bookInformation = bookInformation.first()
                    )
                bookInformation.collect { bookInformation1 ->
                    if (bookInformation1.id == -1) return@collect
                    _uiState.recentReadingBooks[index] =
                        _uiState.recentReadingBooks[index].copy(
                            bookInformation = bookInformation1
                        )
                    _uiState.isLoading =
                        _uiState.recentReadingBooks.isEmpty()
                                || _uiState.recentReadingBooks.any { it.id == -1 }
                }
            }
            viewModelScope.launch {
                val userReadingData = bookRepository.getUserReadingData(id)
                userReadingData.collect { userReadingData1 ->
                    _uiState.recentReadingBooks[index] =
                        _uiState.recentReadingBooks[index].copy(
                            userReadingData = userReadingData1
                        )
                }
            }
        }
    }
}
