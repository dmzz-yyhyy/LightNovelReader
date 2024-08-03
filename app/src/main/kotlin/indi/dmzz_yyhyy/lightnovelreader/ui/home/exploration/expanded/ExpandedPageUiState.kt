package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.expanded

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.Filter

@State
interface ExpandedPageUiState {
    val pageTitle: String
    val filters: List<Filter>
    val bookList: List<BookInformation>
}

class MutableExpandedPageUiState : ExpandedPageUiState {
    override var pageTitle: String by mutableStateOf("")
    override var filters: MutableList<Filter> by mutableStateOf(mutableListOf())
    override var bookList: MutableList<BookInformation> by mutableStateOf(mutableListOf())
}