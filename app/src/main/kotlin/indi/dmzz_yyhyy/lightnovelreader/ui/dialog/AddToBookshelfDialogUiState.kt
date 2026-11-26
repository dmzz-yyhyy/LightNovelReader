package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.mutableStateListOf
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.identifier.Identifier

interface AddToBookshelfDialogUiState {
    val allBookShelf: List<Bookshelf>
    val selectedBookshelfIds: List<Identifier.Bookshelf>
}

class MutableAddToBookshelfDialogUiState : AddToBookshelfDialogUiState {
    override var allBookShelf = mutableStateListOf<Bookshelf>()
    override var selectedBookshelfIds = mutableStateListOf<Identifier.Bookshelf>()
}
