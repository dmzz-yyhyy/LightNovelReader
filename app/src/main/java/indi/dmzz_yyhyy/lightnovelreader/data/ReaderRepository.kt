package indi.dmzz_yyhyy.lightnovelreader.data

import dagger.hilt.android.scopes.ActivityRetainedScoped
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class ReaderRepository @Inject constructor(
    private val webBookDataSource: WebBookDataSource
) {
    private var _book: MutableStateFlow<Book> = MutableStateFlow(Book(0, "", "", ""))
    private var _bookId: Int = 0
    private var _volumeList: MutableStateFlow<MutableList<Volume>> = MutableStateFlow(mutableListOf())
    private var _chapterContentId: MutableStateFlow<Int> = MutableStateFlow(0)
    private var _chapterContent: MutableStateFlow<ChapterContent> = MutableStateFlow(ChapterContent("", ""))

    val book: StateFlow<Book> get() = _book
    val bookName: String get() = _book.value.bookName
    val bookCoverUrl: String get() = _book.value.coverUrl
    val bookIntroduction: String get() = _book.value.introduction
    val volumeList: StateFlow<List<Volume>> get() = _volumeList
    val chapterContentId: StateFlow<Int> get() = _chapterContentId
    val chapterContent: StateFlow<ChapterContent> get() = _chapterContent

    // 更新数据的方法
    suspend fun loadChapterList(bookId: Int) {
        _bookId = bookId
        val book = webBookDataSource.getBook(bookId)
        if (book != null) { _book.value = book }
        val chapterList = webBookDataSource.getBookChapterList(bookId)
        if (chapterList != null) { _volumeList.value = chapterList.toMutableList() }
    }
    suspend fun loadChapterContent(){
        _chapterContent.value = ChapterContent("", "")
        val bookContent = webBookDataSource.getBookContent(_bookId, _chapterContentId.value)
        if (bookContent != null) { _chapterContent.value = bookContent }
    }
    fun setChapterContentId(id: Int){
        _chapterContentId.value = id;
    }
}