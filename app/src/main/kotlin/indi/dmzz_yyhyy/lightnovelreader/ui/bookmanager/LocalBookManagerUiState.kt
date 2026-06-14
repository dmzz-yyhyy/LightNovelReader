package indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface LocalBookManagerUiState {
    val isLoading: Boolean
    val isDeleting: Boolean
    val isSelecting: Boolean
    val bookList: List<LocalBookItem>
    val selectedIds: Set<String>
    val sort: LocalBookSort
    val sortReverse: Boolean
    val load: () -> Unit
    val setSort: (LocalBookSort) -> Unit
    val setReverse: (Boolean) -> Unit
    val enterSelection: (String?) -> Unit
    val exitSelection: () -> Unit
    val toggleSelect: (String) -> Unit
    val selectAll: () -> Unit
    val deleteSelected: () -> Unit
    val clearOrphanedData: () -> Unit
    val clearBookData: (String, List<LocalBookClearTarget>) -> Unit
    val openStorageOverview: () -> Unit
    val openBookDetailScreen: (String) -> Unit
}

class MutableLocalBookManagerUiState(
    override var load: () -> Unit,
    override var setSort: (LocalBookSort) -> Unit,
    override var setReverse: (Boolean) -> Unit,
    override var enterSelection: (String?) -> Unit,
    override var exitSelection: () -> Unit,
    override var toggleSelect: (String) -> Unit,
    override var selectAll: () -> Unit,
    override var deleteSelected: () -> Unit,
    override var clearOrphanedData: () -> Unit,
    override var clearBookData: (String, List<LocalBookClearTarget>) -> Unit,
    override var openStorageOverview: () -> Unit,
    override var openBookDetailScreen: (String) -> Unit
) : LocalBookManagerUiState {
    override var isLoading by mutableStateOf(true)
    override var isDeleting by mutableStateOf(false)
    override var isSelecting by mutableStateOf(false)
    override var bookList by mutableStateOf(emptyList<LocalBookItem>())
    override var selectedIds by mutableStateOf(emptySet<String>())
    override var sort by mutableStateOf(LocalBookSort.Size)
    override var sortReverse by mutableStateOf(true)
}
