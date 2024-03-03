package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.room.dao.BookMetadataDao
import indi.dmzz_yyhyy.lightnovelreader.data.room.dao.ReadingBookDao
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.BookMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingBookRepository @Inject constructor(
    private val readingBookDao: ReadingBookDao,
    private val bookMetaDataDao: BookMetadataDao,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var _readingBookIdList: List<Int> = listOf()
    private var _readingBookMetadataList: MutableStateFlow<List<BookMetadata>> = MutableStateFlow(mutableListOf())
    val readingBookMetadataList: StateFlow<List<BookMetadata>> get() = _readingBookMetadataList

    init {
        coroutineScope.launch {
            _update()
        }
    }

    private suspend fun _update() {
        _readingBookIdList = readingBookDao.getAll()
        _readingBookMetadataList.value = bookMetaDataDao.getByIdList(_readingBookIdList) ?: _readingBookMetadataList.value
    }

    fun isBookInList(bookId: Int): Boolean = (bookId in _readingBookIdList)

    // 添加书本id至readingBookList并将书本添加至BookMetaData表中
    suspend fun addReadingBook(book: BookMetadata) {
        readingBookDao.add(book.id)
        if (bookMetaDataDao.get(book.id) == null) {
            bookMetaDataDao.add(book)
        }
        coroutineScope.launch {
            _update()
        }
    }

    suspend fun deleteReadingBook(bookId: Int) {
        readingBookDao.delete(bookId)
    }
}