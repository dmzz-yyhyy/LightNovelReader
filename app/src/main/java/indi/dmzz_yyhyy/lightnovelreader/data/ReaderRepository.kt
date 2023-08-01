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
    private val webBookDataSource: WebBookDataSource,
    private val bookRepository: BookRepository
) {
    private var _book: Book = Book(0, "", "", "")
    private var _bookId: Int = 0
    private var _volumeList: MutableList<Volume> = mutableListOf()
    private var _chapterContentId: MutableStateFlow<Int> = MutableStateFlow(0)
    private var _chapterContent: MutableStateFlow<ChapterContent> = MutableStateFlow(ChapterContent("", ""))
    private var _dataFlow = MutableStateFlow(0)

    val bookName: String get() = _book.bookName
    val bookCoverUrl: String get() = _book.coverURL
    val bookIntroduction: String get() = _book.introduction
    val volumeList: List<Volume> get() = _volumeList
    val chapterContentId: StateFlow<Int> get() = _chapterContentId
    val chapterContent: StateFlow<ChapterContent> get() = _chapterContent
    val dataFlow: StateFlow<Int> get() = _dataFlow

    // 更新数据的方法
    private fun updateData() {
        // 更新数据逻辑...
        // 数据更新后使用emit发射新值
        _dataFlow.value = _dataFlow.value + 1
    }

    suspend fun loadChapterList(bookId: Int) {
        _bookId = bookId
        val book = bookRepository.getBook(bookId)
        if (book != null) { _book = book }
        val chapterList = webBookDataSource.getBookChapterList(bookId)
        if (chapterList != null) { _volumeList = chapterList.toMutableList() }
        while (true) {
            if (_volumeList.size != 0 && _book.bookID != 0) {
                updateData()
                return
            }
        }
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