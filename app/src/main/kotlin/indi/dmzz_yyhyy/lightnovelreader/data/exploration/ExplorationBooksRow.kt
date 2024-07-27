package indi.dmzz_yyhyy.lightnovelreader.data.exploration

import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource

data class ExplorationBooksRow(
    val title: String,
    val bookList: List<ExplorationDisplayBook>,
    val expandable: Boolean,
    val expandedPageDataSource: ExplorationExpandedPageDataSource? = null
)
