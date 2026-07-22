package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

interface ContentViewModel {
    val uiState: ContentUiState
    fun changeBookId(id: String)
    fun loadNextChapter()
    fun loadLastChapter()
    fun changeChapter(id: String)
}