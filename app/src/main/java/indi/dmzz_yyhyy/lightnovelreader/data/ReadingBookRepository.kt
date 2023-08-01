package indi.dmzz_yyhyy.lightnovelreader.data

import android.util.Log
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingBookRepository  @Inject constructor(
    private val bookRepository: BookRepository
){
    private var _readingList: MutableList<Int> = mutableListOf()
    private var _readingBookList: MutableStateFlow<MutableList<Book>> = MutableStateFlow(mutableListOf())
    val readingBookList: StateFlow<MutableList<Book>> = _readingBookList
    fun setReadingBookList(readingBookList: List<Int>) {
        _readingList = readingBookList.toMutableList()
        Log.d("ReadingBookRepository", "set readingBookList success readingBookList=$_readingList")
    }

    fun addReadingBook(bookId: Int) {
        _readingList.add(bookId)
    }

    fun removeReadingBook(bookId: Int) {
        _readingList.remove(bookId)
    }

    suspend fun loadReadingBookList() {
        Log.d("ReadingBookRepository", "readingBookList=$_readingList")
        val readingBookList: MutableList<Book> = mutableListOf()
        for (bookId in _readingList) {
            bookRepository.getBook(bookId)?.let { readingBookList.add(it) }}
        _readingBookList.value = readingBookList
    }
}