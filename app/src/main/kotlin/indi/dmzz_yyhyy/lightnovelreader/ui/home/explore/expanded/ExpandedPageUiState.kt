package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.github.michaelbull.result.Result
import androidx.compose.runtime.Stable
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.flow.Flow

@Stable
interface ExpandedPageUiState {
    val pageTitle: String
    val filters: List<Filter<*>>
    val bookList: List<Pair<String, Flow<Result<BookInformation, WebRequestError>>>>
    val allBookshelfBookIds: List<String>
    val isLoading: Boolean
    val isComplete: Boolean
    val errorMessage: String?
}

class MutableExpandedPageUiState : ExpandedPageUiState {
    override var pageTitle: String by mutableStateOf("")
    override var filters: SnapshotStateList<Filter<*>> = mutableStateListOf()
    override var bookList =
        mutableStateListOf<Pair<String, Flow<Result<BookInformation, WebRequestError>>>>()
    override var allBookshelfBookIds: List<String> by mutableStateOf(emptyList())
    override var isLoading by mutableStateOf(true)
    override var isComplete by mutableStateOf(false)
    override var errorMessage: String? by mutableStateOf(null)

    internal fun acceptResult(
        result: SearchResult,
        bookInformation: (String) -> Flow<Result<BookInformation, WebRequestError>>
    ): Boolean {
        when (result) {
            is SearchResult.SingleBook, is SearchResult.MultipleBook -> {
                val id = when (result) {
                    is SearchResult.SingleBook -> result.bookId
                    is SearchResult.MultipleBook -> result.bookId
                }
                if (bookList.none { it.first == id }) bookList.add(id to bookInformation(id))
                isLoading = false
                if (result is SearchResult.SingleBook) isComplete = true
            }
            is SearchResult.Error -> {
                errorMessage = result.error.message.orEmpty()
                isComplete = true
            }
            is SearchResult.Empty, is SearchResult.End -> isComplete = true
        }
        if (isComplete) isLoading = false
        return !isComplete
    }
}
