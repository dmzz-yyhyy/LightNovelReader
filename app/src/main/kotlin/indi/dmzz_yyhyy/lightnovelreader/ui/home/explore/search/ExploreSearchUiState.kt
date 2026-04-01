package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.util.LocalString

@State
interface ExploreSearchUiState {
    val isFocused: Boolean
    val isLoading: Boolean
    val isLoadingComplete: Boolean
    val errorMessage: String
    val historyList: List<String>
    val suggestions: List<String>
    val searchTypeIdList: List<String>
    val searchTypeNameMap: Map<String, LocalString>
    val searchType: String
    val searchTip: LocalString
    val searchResult: List<BookInformation>
    val allBookshelfBookIds: List<String>
    val dropdownMenuExpanded: Boolean
    val searchBarExpanded: Boolean
    fun setDropdownMenuExpandedState(state: Boolean)
    fun setSearchBarExpandedState(state: Boolean)
}

class MutableExploreSearchUiState : ExploreSearchUiState {
    override var isFocused: Boolean by mutableStateOf(true)
    override var isLoading: Boolean by mutableStateOf(true)
    override var isLoadingComplete: Boolean by mutableStateOf(false)
    override var errorMessage: String by mutableStateOf("")
    override var historyList: List<String> by mutableStateOf(emptyList())
    override var suggestions: List<String> by mutableStateOf(emptyList())
    override var searchTypeIdList = mutableStateListOf<String>()
    override var searchTypeNameMap = mutableStateMapOf<String, LocalString>()
    override var searchType: String by mutableStateOf("")
    override var searchTip: LocalString by mutableStateOf(LocalString(""))
    override var searchResult: SnapshotStateList<BookInformation> = mutableStateListOf()
    override var allBookshelfBookIds: List<String> by mutableStateOf(emptyList())
    override var dropdownMenuExpanded by mutableStateOf(false)
    override var searchBarExpanded by mutableStateOf(true)
    override fun setDropdownMenuExpandedState(state: Boolean) {
        dropdownMenuExpanded = state
    }
    override fun setSearchBarExpandedState(state: Boolean) {
        searchBarExpanded = state
    }
}
