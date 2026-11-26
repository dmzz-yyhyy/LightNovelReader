package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.michaelbull.result.Result
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.BookshelfBookItem
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.BookshelfUiState
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.flow.Flow

@Stable
interface BookshelfHomeUiState {
    val bookshelfList: List<BookshelfUiState>
    val selectedBookshelfId: Identifier.Bookshelf?
    val selectedTabIndex get() = bookshelfList.indexOfFirst { it.id == selectedBookshelfId }
    val selectedBookshelf: BookshelfUiState? get() = if (selectedTabIndex != -1) bookshelfList[selectedTabIndex] else null
    val selectMode: Boolean
    val reorderMode: Boolean
    val reorderBookshelfMode: Boolean
    var updatedExpanded: Boolean
    var pinnedExpanded: Boolean
    var allExpanded: Boolean
    val selectedBookIds: List<Identifier.Book>
    val reorderBookIds: List<Pair<Identifier.Book, Flow<Result<BookshelfBookItem, WebRequestError>>>>
    val reorderBookshelfIds: List<Identifier.Bookshelf>
    val toast: String
    val changePage: (Identifier.Bookshelf) -> Unit
    val changeSortType: (BookshelfSortType) -> Unit
    val changeSortReversed: (Boolean) -> Unit
    val changeBookSelectState: (Identifier.Book) -> Unit
    val enableReorderMode: () -> Unit
    val disableReorderMode: () -> Unit
    val moveBook: (Int, Int) -> Unit
    val enableBookshelfReorderMode: () -> Unit
    val disableBookshelfReorderMode: () -> Unit
    val moveBookshelf: (Int, Int) -> Unit
    val onCreate: () -> Unit
    val onEdit: (Identifier.Bookshelf) -> Unit
    val onBookClick: (Identifier.Book) -> Unit
    val onEnableSelectMode: () -> Unit
    val onDisableSelectMode: () -> Unit
    val onSelectAll: () -> Unit
    val onPin: () -> Unit
    val onRemove: () -> Unit
    val onMarkSelectedBooks: () -> Unit
    val saveAllBookshelfJsonData: (Uri) -> Unit
    val saveBookshelfJsonData: (Uri) -> Unit
    val importBookshelf: (Uri) -> Unit
    val clearToast: () -> Unit
}

class MutableBookshelfHomeUiState(
    override val changePage: (Identifier.Bookshelf) -> Unit = {},
    override val changeSortType: (BookshelfSortType) -> Unit = {},
    override val changeSortReversed: (Boolean) -> Unit = {},
    override val changeBookSelectState: (Identifier.Book) -> Unit = {},
    override val enableReorderMode: () -> Unit = {},
    override val disableReorderMode: () -> Unit = {},
    override val moveBook: (Int, Int) -> Unit = { _, _ -> },
    override val enableBookshelfReorderMode: () -> Unit = {},
    override val disableBookshelfReorderMode: () -> Unit = {},
    override val moveBookshelf: (Int, Int) -> Unit = { _, _ -> },
    override val onCreate: () -> Unit = {},
    override val onEdit: (Identifier.Bookshelf) -> Unit = {},
    override val onBookClick: (Identifier.Book) -> Unit = {},
    override val onEnableSelectMode: () -> Unit = {},
    override val onDisableSelectMode: () -> Unit = {},
    override val onSelectAll: () -> Unit = {},
    override val onPin: () -> Unit = {},
    override val onRemove: () -> Unit = {},
    override val onMarkSelectedBooks: () -> Unit = {},
    override val saveAllBookshelfJsonData: (Uri) -> Unit = {},
    override val saveBookshelfJsonData: (Uri) -> Unit = {},
    override val importBookshelf: (Uri) -> Unit = {},
    override val clearToast: () -> Unit = {},
) : BookshelfHomeUiState {
    override var bookshelfList by mutableStateOf(emptyList<BookshelfUiState>())
    override var selectedBookshelfId: Identifier.Bookshelf? by mutableStateOf(null)
    override var selectMode by mutableStateOf(false)
    override var reorderMode by mutableStateOf(false)
    override var reorderBookshelfMode by mutableStateOf(false)
    override var updatedExpanded by mutableStateOf(true)
    override var pinnedExpanded by mutableStateOf(true)
    override var allExpanded by mutableStateOf(true)
    override val selectedBookIds: MutableList<Identifier.Book> = mutableStateListOf()
    override val reorderBookIds: MutableList<Pair<Identifier.Book, Flow<Result<BookshelfBookItem, WebRequestError>>>> =
        mutableStateListOf()
    override val reorderBookshelfIds: MutableList<Identifier.Bookshelf> = mutableStateListOf()
    override var toast by mutableStateOf("")
}
