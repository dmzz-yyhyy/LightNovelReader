package indi.dmzz_yyhyy.lightnovelreader.ui.home

import indi.dmzz_yyhyy.lightnovelreader.data.book.SearchBook
import indi.dmzz_yyhyy.lightnovelreader.data.local.RouteConfig
import indi.dmzz_yyhyy.lightnovelreader.data.local.SearchType

data class SearchUiState(
    val isLoading: Boolean = true,
    val route: String? = RouteConfig.HOME,
    val searchType: String = SearchType.articleName,
    val page: Int = 1,
    val keyword: String = "",
    val bookList: List<SearchBook> = listOf(),
    val totalPage: Int = 0
)
