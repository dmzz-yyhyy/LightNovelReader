package indi.dmzz_yyhyy.lightnovelreader.data

import android.util.Log
import indi.dmzz_yyhyy.lightnovelreader.api.LightNovelReaderAPI
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import indi.dmzz_yyhyy.lightnovelreader.data.book.Chapter
import indi.dmzz_yyhyy.lightnovelreader.data.book.Information
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class WebBookDataSource @Inject constructor(
    private val lightNovelReaderAPI: LightNovelReaderAPI,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getBook(bookId: Int): Book? =
        withContext(ioDispatcher) {
            creatBook(lightNovelReaderAPI.getBookInformation(bookId))
        }
    private fun creatBook(information: Information?): Book? {
        if (information == null) {return null}
        val data = information.data
        Log.i("Web", "Books get success, bookId=${data.bookID}")
        return Book(data.bookID, data.bookName, data.bookCoverURL, data.bookIntroduction)
    }
    suspend fun getBookChapterList(bookId: Int): List<Chapter> {
        withContext(ioDispatcher) {
            lightNovelReaderAPI.getBookInformation(bookId)
        }
    }
}


