package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import io.nightfish.lightnovelreader.api.bookshelf.MutableBookshelf

@State
interface BookshelfHomeUiState {
    val bookshelfList: List<Bookshelf>
    val selectedBookshelfId: Int
    val bookInformationMap: Map<String, BookInformation>
    val bookLastChapterTitleMap: Map<String, String>
    val selectedTabIndex get() = bookshelfList.indexOfFirst { it.id == selectedBookshelfId }
    val selectedBookshelf: Bookshelf get() = if (selectedTabIndex != -1) bookshelfList[selectedTabIndex] else MutableBookshelf()
    val selectMode: Boolean
    val reorderMode: Boolean
    val reorderBookshelfMode: Boolean
    var updatedExpanded: Boolean
    var pinnedExpanded: Boolean
    var allExpanded: Boolean
    val selectedBookIds: List<String>
    val reorderBookIds: List<String>
    val reorderBookshelfIds: List<Int>
    val toast: String
    val changePage: (Int) -> Unit
    val changeSortType: (BookshelfSortType) -> Unit
    val changeSortReversed: (Boolean) -> Unit
    val changeBookSelectState: (String) -> Unit
    val enableReorderMode: () -> Unit
    val disableReorderMode: () -> Unit
    val moveBook: (Int, Int) -> Unit
    val enableBookshelfReorderMode: () -> Unit
    val disableBookshelfReorderMode: () -> Unit
    val moveBookshelf: (Int, Int) -> Unit
    val onCreate: () -> Unit
    val onEdit: (Int) -> Unit
    val onBookClick: (String) -> Unit
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
    override val changePage: (Int) -> Unit = {},
    override val changeSortType: (BookshelfSortType) -> Unit = {},
    override val changeSortReversed: (Boolean) -> Unit = {},
    override val changeBookSelectState: (String) -> Unit = {},
    override val enableReorderMode: () -> Unit = {},
    override val disableReorderMode: () -> Unit = {},
    override val moveBook: (Int, Int) -> Unit = { _, _ -> },
    override val enableBookshelfReorderMode: () -> Unit = {},
    override val disableBookshelfReorderMode: () -> Unit = {},
    override val moveBookshelf: (Int, Int) -> Unit = { _, _ -> },
    override val onCreate: () -> Unit = {},
    override val onEdit: (Int) -> Unit = {},
    override val onBookClick: (String) -> Unit = {},
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
    override var bookshelfList by mutableStateOf(emptyList<MutableBookshelf>())
    override var selectedBookshelfId by mutableIntStateOf(-1)
    override var bookInformationMap = mutableStateMapOf<String, BookInformation>()
    override var bookLastChapterTitleMap = mutableStateMapOf<String, String>()
    override var selectMode by mutableStateOf(false)
    override var reorderMode by mutableStateOf(false)
    override var reorderBookshelfMode by mutableStateOf(false)
    override var updatedExpanded by mutableStateOf(true)
    override var pinnedExpanded by mutableStateOf(true)
    override var allExpanded by mutableStateOf(true)
    override val selectedBookIds: MutableList<String> = mutableStateListOf()
    override val reorderBookIds: MutableList<String> = mutableStateListOf()
    override val reorderBookshelfIds: MutableList<Int> = mutableStateListOf()
    override var toast by mutableStateOf("")
}
