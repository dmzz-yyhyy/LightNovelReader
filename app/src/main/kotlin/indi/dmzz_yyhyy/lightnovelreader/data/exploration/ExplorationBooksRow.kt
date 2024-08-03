package indi.dmzz_yyhyy.lightnovelreader.data.exploration

data class ExplorationBooksRow(
    val title: String,
    val bookList: List<ExplorationDisplayBook>,
    val expandable: Boolean,
    val expandedPageDataSourceId: String? = null
)
