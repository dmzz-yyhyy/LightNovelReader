package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteBookshelfDialogViewModel @Inject constructor(
    private val bookshelfRepository: BookshelfRepository
) : ViewModel() {
    fun deleteBookshelf(id: Identifier.Bookshelf) {
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.deleteBookshelf(id)
        }
    }
}
