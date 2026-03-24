package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddToBookshelfDialogViewModel @Inject constructor(
    private val bookshelfRepository: BookshelfRepository,
    private val bookRepository: BookRepository

) : ViewModel() {
    private val _addToBookshelfDialogUiState = MutableAddToBookshelfDialogUiState()

    var navController: NavController? = null
    var bookId = ""
        set(value) {
            viewModelScope.launch(Dispatchers.IO) {
                _addToBookshelfDialogUiState.allBookShelf.addAll(
                    bookshelfRepository.getAllBookshelfIds()
                        .mapNotNull { bookshelfRepository.getBookshelf(it) }
                )
            }
            viewModelScope.launch(Dispatchers.IO) {
                _addToBookshelfDialogUiState.selectedBookshelfIds.addAll(bookshelfRepository.getBookshelfBookMetadata(bookId)?.bookShelfIds ?: emptyList())
            }
            field = value
        }
    val addToBookshelfDialogUiState = _addToBookshelfDialogUiState

    fun onSelectBookshelf(bookshelfId: Int) {
        if (bookId.isBlank()) return
        _addToBookshelfDialogUiState.selectedBookshelfIds += listOf(bookshelfId)
    }

    fun onDeselectBookshelf(bookshelfId: Int) {
        if (bookId.isBlank()) return
        _addToBookshelfDialogUiState.selectedBookshelfIds =
            _addToBookshelfDialogUiState.selectedBookshelfIds.apply { removeAll { it == bookshelfId } }
    }

    fun onDismissAddToBookshelfRequest() {
        navController?.popBackStack()
        if (bookId.isBlank()) return
        _addToBookshelfDialogUiState.selectedBookshelfIds.clear()
    }

    fun processAddToBookshelfRequest() {
        navController?.popBackStack()
        if (bookId.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val oldBookShelfIds = bookshelfRepository.getBookshelfBookMetadata(bookId)?.bookShelfIds ?: emptyList()
            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookInformationFlow(bookId).collect { bookInformation ->
                    if (bookInformation.isEmpty()) return@collect
                    _addToBookshelfDialogUiState.selectedBookshelfIds.forEach {
                        bookshelfRepository.addBookIntoBookShelf(it, bookInformation)
                    }
                }
            }
            oldBookShelfIds.filter { !_addToBookshelfDialogUiState.selectedBookshelfIds.contains(it) }.forEach {
                bookshelfRepository.deleteBookFromBookshelf(it, bookId)
            }
        }
    }
}
