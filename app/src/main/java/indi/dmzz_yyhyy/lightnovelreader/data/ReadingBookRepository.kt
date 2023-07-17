package indi.dmzz_yyhyy.lightnovelreader.data

import android.util.Log
import dagger.hilt.android.scopes.ActivityScoped
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingBookRepository  @Inject constructor(
    private val bookRepository: BookRepository
){
    private var _readingList: MutableList<Int> = mutableListOf()
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

    suspend fun getReadingBookList(): MutableList<Book> {
        Log.d("ReadingBookRepository", "readingBookList=$_readingList")
        val readingBookList: MutableList<Book> = mutableListOf()
        for (bookId in _readingList) {
            bookRepository.getBook(bookId)?.let { readingBookList.add(it) }
            Log.i("ReadingViewModel", "Book gets success.${readingBookList[readingBookList.size - 1].bookID}")
        }
        return readingBookList
    }
}