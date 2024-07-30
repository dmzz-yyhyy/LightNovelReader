package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State

@State
interface ExplorationUiState {
    val isOffLine: Boolean
}

class MutableExplorationUiState : ExplorationUiState {
    override var isOffLine: Boolean by mutableStateOf(true)
}