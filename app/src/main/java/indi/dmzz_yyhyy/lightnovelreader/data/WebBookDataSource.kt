package indi.dmzz_yyhyy.lightnovelreader.data

import android.util.Log
import indi.dmzz_yyhyy.lightnovelreader.api.LightNovelReaderAPI
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.Information
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
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

    suspend fun getBookChapterList(bookId: Int): List<Volume>? =
        withContext(ioDispatcher) {
            lightNovelReaderAPI.getBookChapterList(bookId)

        }

    suspend fun getBookContent(bookId: Int, chapterId: Int): ChapterContent? =
        withContext(ioDispatcher) {
            lightNovelReaderAPI.getBookContent(bookId, chapterId)

        }

    private fun creatBook(information: Information?): Book? {
        if (information == null) {return null}
        Log.i("Web", "Books get success, bookId=${information.bookID}")
        return Book(information.bookID, information.bookName, information.bookCoverURL, information.bookIntroduction)
    }
}


