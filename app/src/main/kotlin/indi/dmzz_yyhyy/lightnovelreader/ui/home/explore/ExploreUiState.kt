package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable

@Stable
interface ExploreUiState {
    val isOffLine: Boolean
    val isRefreshing: Boolean
}

class MutableExploreUiState : ExploreUiState {
    override var isOffLine: Boolean by mutableStateOf(true)
    override var isRefreshing: Boolean by mutableStateOf(false)
}