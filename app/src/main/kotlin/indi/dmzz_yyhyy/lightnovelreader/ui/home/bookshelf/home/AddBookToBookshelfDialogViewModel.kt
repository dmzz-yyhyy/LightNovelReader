package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookToBookshelfDialogViewModel @Inject constructor(
    private val bookshelfRepository: BookshelfRepository,
    private val localBookDataSource: LocalBookDataSource
) : ViewModel() {
    var allBookshelfFlow = bookshelfRepository.getAllBookshelvesFlow()

    fun markSelectedBooks(selectedBookIds: List<String>, bookshelfIds: List<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            selectedBookIds.forEach { bookId ->
                localBookDataSource.getBookInformation(bookId)?.let { bookInformation ->
                    bookshelfIds.forEach {
                        bookshelfRepository.addBookIntoBookShelf(
                            it,
                            bookInformation
                        )
                    }
                }
            }
        }
    }
}