package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import io.nightfish.lightnovelreader.api.identifier.Identifier

@Stable
interface ExploreHomeUiState {
    val pageTitles: List<String>
    val selectedPage: Int
    val explorePageTitle: String
    val explorePageBooksRawList: List<ExploreBooksRow>
    val sourceItems: List<Pair<Identifier, String>>
    val selectedSourceId: Identifier?
}

class MutableExploreHomeUiState : ExploreHomeUiState {
    override var pageTitles: List<String> by mutableStateOf(emptyList())
    override var selectedPage by mutableIntStateOf(0)
    override var explorePageTitle by mutableStateOf("")
    override var explorePageBooksRawList: List<ExploreBooksRow> by mutableStateOf(emptyList())
    override var sourceItems: List<Pair<Identifier, String>> by mutableStateOf(emptyList())
    override var selectedSourceId: Identifier? by mutableStateOf(null)
}
