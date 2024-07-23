package indi.dmzz_yyhyy.lightnovelreader.data.book

data class ChapterContent(
    val id: Int,
    val title: String,
    val content: String,
    val lastChapter: Int = -1,
    val nextChapter: Int = -1
) {
    fun hasLastChapter(): Boolean = lastChapter > -1
    fun hasNextChapter(): Boolean = nextChapter > -1
    companion object {
        fun empty(): ChapterContent = ChapterContent(-1, "", "")
    }
}
