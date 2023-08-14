package indi.dmzz_yyhyy.lightnovelreader.data.book

data class SearchBook(
    val bookId: Int,
    val title: String,
    val coverUrl: String,
    val writer: String,
    val type: String,
    val tags: List<String>,
    val introduction: String
)