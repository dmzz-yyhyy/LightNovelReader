package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow

@State
interface ExploreHomeUiState {
    val pageTitles: List<String>
    val selectedPage: Int
    val explorePageTitle: String
    val explorePageBooksRawList: List<ExploreBooksRow>
}

class MutableExploreHomeUiState : ExploreHomeUiState {
    override var pageTitles: List<String> by mutableStateOf(emptyList())
    override var selectedPage by mutableIntStateOf(0)
    override var explorePageTitle by mutableStateOf("")
    override var explorePageBooksRawList: List<ExploreBooksRow> by mutableStateOf(emptyList())
}
