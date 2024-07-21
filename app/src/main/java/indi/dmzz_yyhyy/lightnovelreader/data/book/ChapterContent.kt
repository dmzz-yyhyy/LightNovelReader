package indi.dmzz_yyhyy.lightnovelreader.data.book

data class ChapterContent(
    val id: Int,
    val title: String,
    val content: String,
) {
    companion object {
        fun empty(): ChapterContent = ChapterContent(-1, "", "")
    }
}
