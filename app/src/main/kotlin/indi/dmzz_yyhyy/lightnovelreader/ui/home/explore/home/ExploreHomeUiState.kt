package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import io.nightfish.lightnovelreader.api.web.explore.CustomExploreTapPage

@Stable
interface ExploreHomeUiState {
    val pageTitles: List<String>
    val selectedPage: Int
    val customPage: CustomExploreTapPage?
    val isLoading: Boolean
    val explorePageBooksRawList: List<ExploreBooksRow>
}

class MutableExploreHomeUiState : ExploreHomeUiState {
    override var pageTitles: List<String> by mutableStateOf(emptyList())
    override var selectedPage by mutableIntStateOf(0)
    override var customPage by mutableStateOf<CustomExploreTapPage?>(null)
    override var isLoading by mutableStateOf(false)
    override var explorePageBooksRawList: List<ExploreBooksRow> by mutableStateOf(emptyList())
}
