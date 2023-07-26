package indi.dmzz_yyhyy.lightnovelreader.data

import android.util.Log
import indi.dmzz_yyhyy.lightnovelreader.api.LightNovelReaderAPI
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterList
import indi.dmzz_yyhyy.lightnovelreader.data.book.Content
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

    suspend fun getBookChapterList(bookId: Int): ChapterList? =
        withContext(ioDispatcher) {
            lightNovelReaderAPI.getBookChapterList(bookId)

        }

    suspend fun getBookContent(bookId: Int, chapterId: Int): Content? =
        withContext(ioDispatcher) {
            lightNovelReaderAPI.getBookContent(bookId, chapterId)

        }

    private fun creatBook(information: Information?): Book? {
        if (information == null) {return null}
        val data = information.data
        Log.i("Web", "Books get success, bookId=${data.bookID}")
        return Book(data.bookID, data.bookName, data.bookCoverURL, data.bookIntroduction)
    }
}


