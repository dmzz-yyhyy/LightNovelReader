package indi.dmzz_yyhyy.lightnovelreader.ui.home

import indi.dmzz_yyhyy.lightnovelreader.data.local.RouteConfig
import indi.dmzz_yyhyy.lightnovelreader.data.local.SearchType
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Information

data class SearchUiState(
    val isLoading: Boolean = true,
    val route: String? = RouteConfig.HOME,
    val searchType: String = SearchType.articleName,
    val page: Int = 1,
    val keyword: String = "",
    val bookList: List<Information> = listOf(),
    val totalPage: Int = 0,
)
