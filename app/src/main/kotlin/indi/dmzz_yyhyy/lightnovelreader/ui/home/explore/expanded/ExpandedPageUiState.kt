package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import kotlinx.coroutines.flow.Flow

@Stable
interface ExpandedPageUiState {
    val pageTitle: String
    val filters: List<Filter<*>>
    val bookList: List<Pair<Identifier.Book, Flow<Result<BookInformation, WebRequestError>>>>
    val allBookshelfBookIds: List<Identifier.Book>
}

class MutableExpandedPageUiState : ExpandedPageUiState {
    override var pageTitle: String by mutableStateOf("")
    override var filters: SnapshotStateList<Filter<*>> = mutableStateListOf()
    override var bookList =
        mutableStateListOf<Pair<Identifier.Book, Flow<Result<BookInformation, WebRequestError>>>>()
    override var allBookshelfBookIds: List<Identifier.Book> by mutableStateOf(emptyList())
}
