package indi.dmzz_yyhyy.lightnovelreader.data

import android.content.ContentValues
import android.util.Log
import indi.dmzz_yyhyy.lightnovelreader.api.LightNovelReaderAPI
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository  @Inject constructor (
    private val webBookDataSource: WebBookDataSource,
    private val localBookDataSource: LocalBookDataSource
) {
    suspend fun getBook(boolId: Int): Book? {
        var book = localBookDataSource.getBook(boolId)
        if (book == null) {
            book = webBookDataSource.getBook(boolId)
            if (book == null) {
                return null
            }
            localBookDataSource.add(book)
        }
        return book
    }

    suspend fun getBookChapterList(bookId: Int): Book? {
        return null
    }

}
