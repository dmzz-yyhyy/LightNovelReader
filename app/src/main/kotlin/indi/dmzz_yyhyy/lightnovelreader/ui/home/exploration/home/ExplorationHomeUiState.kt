package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage

@State
interface ExplorationHomeUiState {
    val isLoading: Boolean
    val pageTitles: List<String>
    val selectedPage: Int
    val explorationPage: ExplorationPage
}

class MutableExplorationHomeUiState : ExplorationHomeUiState {
    override var isLoading: Boolean by mutableStateOf(true)
    override var pageTitles: MutableList<String> by mutableStateOf(mutableListOf())
    override var selectedPage: Int by mutableStateOf(0)
    override var explorationPage: ExplorationPage by mutableStateOf(ExplorationPage.empty())
}