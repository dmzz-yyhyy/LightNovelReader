package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import io.nightfish.lightnovelreader.api.bookshelf.MutableBookshelf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditBookshelfViewModel @Inject constructor(
    private val bookshelfRepository: BookshelfRepository
) : ViewModel() {
    private val _uiState = MutableBookshelf()
    val uiState: Bookshelf = _uiState

    fun init(id: Int?) {
        id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            (bookshelfRepository.getBookshelf(id) ?: MutableBookshelf()).let {
                this@EditBookshelfViewModel._uiState.id = it.id
                this@EditBookshelfViewModel._uiState.name = it.name
                this@EditBookshelfViewModel._uiState.sortType = it.sortType
                this@EditBookshelfViewModel._uiState.sortReversed = it.sortReversed
                this@EditBookshelfViewModel._uiState.autoCache = it.autoCache
                this@EditBookshelfViewModel._uiState.systemUpdateReminder = it.systemUpdateReminder
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.name = name
    }

    fun onAutoCacheChange(autoCache: Boolean) {
        _uiState.autoCache = autoCache
    }

    fun onSystemUpdateReminderChange(systemUpdateReminder: Boolean) {
        _uiState.systemUpdateReminder = systemUpdateReminder
    }

    fun onSortTypeChange(sortType: BookshelfSortType) {
        _uiState.sortType = sortType
    }

    fun save() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.id == -1) {
                bookshelfRepository.createBookShelf(
                    name = _uiState.name,
                    sortType = _uiState.sortType,
                    sortReversed = _uiState.sortReversed,
                    autoCache = _uiState.autoCache,
                    systemUpdateReminder = _uiState.systemUpdateReminder
                )
                return@launch
            }
            bookshelfRepository.updateBookshelf(_uiState.id) {
                _uiState.allBookIds = it.allBookIds
                _uiState.pinnedBookIds = it.pinnedBookIds
                _uiState.updatedBookIds = it.updatedBookIds
                _uiState
            }
        }
    }
}
