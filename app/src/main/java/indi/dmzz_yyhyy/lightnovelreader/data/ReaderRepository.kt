package indi.dmzz_yyhyy.lightnovelreader.data

import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.ViewModelScoped
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import indi.dmzz_yyhyy.lightnovelreader.data.book.Chapter
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class ReaderRepository @Inject constructor(
    private val webBookDataSource: WebBookDataSource,
    private val bookRepository: BookRepository
) {
    private val _dataFlow = MutableStateFlow(0)
    private var _book: Book = Book(0, "", "", "")
    private var _bookId: Int = 0
    private var _volumeList: MutableList<Volume> = mutableListOf()
    val bookName: String get() = _book.bookName
    val bookCoverUrl: String get() = _book.coverURL
    val bookIntroduction: String get() = _book.introduction
    val volumeList: List<Volume> get() = _volumeList
    val dataFlow: MutableStateFlow<Int> get() = _dataFlow

    // 更新数据的方法
    private fun updateData() {
        // 更新数据逻辑...
        // 数据更新后使用emit发射新值
        _dataFlow.value = _dataFlow.value + 1
    }

    suspend fun load(bookId: Int) {
        _bookId = bookId
        if (bookRepository.getBook(bookId) != null) { _book = bookRepository.getBook(bookId)!! }
        if (webBookDataSource.getBookChapterList(bookId) != null) { _volumeList = webBookDataSource.getBookChapterList(bookId)!!.data.toMutableList() }
        while (true) {
            if (_volumeList.size != 0 && _book.bookID != 0) {
                updateData()
                return
            }
        }
    }
}