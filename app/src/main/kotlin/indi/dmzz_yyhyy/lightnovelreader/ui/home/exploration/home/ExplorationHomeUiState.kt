package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationBooksRow

@State
interface ExplorationHomeUiState {
    val pageTitles: List<String>
    val selectedPage: Int
    val explorationPageTitle: String
    val explorationPageBooksRawList: List<ExplorationBooksRow>
}

class MutableExplorationHomeUiState : ExplorationHomeUiState {
    override var pageTitles: MutableList<String> by mutableStateOf(mutableListOf())
    override var selectedPage: Int by mutableStateOf(0)
    override var explorationPageTitle: String by mutableStateOf("")
    override var explorationPageBooksRawList: List<ExplorationBooksRow> by mutableStateOf(mutableListOf())
}