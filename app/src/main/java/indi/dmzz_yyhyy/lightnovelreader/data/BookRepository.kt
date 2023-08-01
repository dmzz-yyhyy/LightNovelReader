package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
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

}
