package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.room.dao.ReadingBookDao
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.ReadingBook
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingBookRepository  @Inject constructor(
    private val readingBookDao: ReadingBookDao
){
    private var _readingBookList: MutableStateFlow<List<ReadingBook>> = MutableStateFlow(mutableListOf())
    val readingBookList: StateFlow<List<ReadingBook>> get() = _readingBookList

    init {
        runBlocking {
            launch {
                readingBookDao.getAll().collect {
                    _readingBookList.value = it
                }
            }
        }
    }

    fun isBookInList(bookId: Int): Boolean {
        for (book in _readingBookList.value) {
            if (book.bookId == bookId) {
                return true
            }
        }
        return false
    }

    suspend fun addReadingBook(book: ReadingBook) {
        readingBookDao.add(book)
    }

    suspend fun deleteReadingBook(bookId: Int) {
        readingBookDao.delete(bookId)
    }
}