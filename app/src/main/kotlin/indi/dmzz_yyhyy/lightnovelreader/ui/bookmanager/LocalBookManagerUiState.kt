package indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.nightfish.lightnovelreader.api.identifier.Identifier

interface LocalBookManagerUiState {
    val isLoading: Boolean
    val isDeleting: Boolean
    val isSelecting: Boolean
    val bookList: List<LocalBookItem>
    val selectedIds: Set<Identifier.Book>
    val sort: LocalBookSort
    val sortReverse: Boolean
    val load: () -> Unit
    val setSort: (LocalBookSort) -> Unit
    val setReverse: (Boolean) -> Unit
    val enterSelection: (Identifier.Book?) -> Unit
    val exitSelection: () -> Unit
    val toggleSelect: (Identifier.Book) -> Unit
    val selectAll: () -> Unit
    val deleteSelected: () -> Unit
    val clearOrphanedData: () -> Unit
    val clearBookData: (Identifier.Book, List<LocalBookClearTarget>) -> Unit
    val openStorageOverview: () -> Unit
    val openBookDetailScreen: (Identifier.Book) -> Unit
}

class MutableLocalBookManagerUiState(
    override var load: () -> Unit,
    override var setSort: (LocalBookSort) -> Unit,
    override var setReverse: (Boolean) -> Unit,
    override var enterSelection: (Identifier.Book?) -> Unit,
    override var exitSelection: () -> Unit,
    override var toggleSelect: (Identifier.Book) -> Unit,
    override var selectAll: () -> Unit,
    override var deleteSelected: () -> Unit,
    override var clearOrphanedData: () -> Unit,
    override var clearBookData: (Identifier.Book, List<LocalBookClearTarget>) -> Unit,
    override var openStorageOverview: () -> Unit,
    override var openBookDetailScreen: (Identifier.Book) -> Unit
) : LocalBookManagerUiState {
    override var isLoading by mutableStateOf(true)
    override var isDeleting by mutableStateOf(false)
    override var isSelecting by mutableStateOf(false)
    override var bookList by mutableStateOf(emptyList<LocalBookItem>())
    override var selectedIds by mutableStateOf(emptySet<Identifier.Book>())
    override var sort by mutableStateOf(LocalBookSort.Size)
    override var sortReverse by mutableStateOf(true)
}
