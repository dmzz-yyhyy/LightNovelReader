package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation

@State
interface ExplorationSearchUiState {
    val isFocused: Boolean
    val isLoading: Boolean
    val isLoadingComplete: Boolean
    val historyList: List<String>
    val searchTypeNameList: List<String>
    val searchType: String
    val searchTip: String
    val searchResult: List<BookInformation>
}

class MutableExplorationSearchUiState : ExplorationSearchUiState {
    override var isFocused: Boolean by mutableStateOf(true)
    override var isLoading: Boolean by mutableStateOf(true)
    override var isLoadingComplete: Boolean by mutableStateOf(false)
    override var historyList: MutableList<String> by mutableStateOf(mutableListOf())
    override var searchTypeNameList: MutableList<String> by mutableStateOf(mutableListOf())
    override var searchType: String by mutableStateOf("")
    override var searchTip: String by mutableStateOf("")
    override var searchResult: MutableList<BookInformation> by mutableStateOf(mutableListOf())
}