package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditBookshelfViewModel @Inject constructor(
    private val bookshelfRepository: BookshelfRepository
) : ViewModel() {
    var bookshelf: Bookshelf by mutableStateOf(Bookshelf.create())
        private set

    fun init(id: Int?) {
        id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            bookshelf = bookshelfRepository.getBookshelf(id) ?: Bookshelf.create()
        }
    }

    fun onNameChange(name: String) {
        bookshelf = bookshelf.copy(
            name = name
        )
    }

    fun onAutoCacheChange(autoCache: Boolean) {
        bookshelf = bookshelf.copy(
            autoCache = autoCache
        )
    }

    fun onSystemUpdateReminderChange(systemUpdateReminder: Boolean) {
        bookshelf = bookshelf.copy(
            systemUpdateReminder = systemUpdateReminder
        )
    }

    fun onSortTypeChange(sortType: BookshelfSortType) {
        bookshelf = bookshelf.copy(
            sortType = sortType
        )
    }

    fun save() {
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.updateBookshelf(bookshelf.id) { _ ->
                bookshelf
            }
        }
    }
}
