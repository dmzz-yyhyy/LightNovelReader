package indi.dmzz_yyhyy.lightnovelreader

import androidx.test.ext.junit.runners.AndroidJUnit4
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import javax.inject.Inject
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookRepositoryTest @Inject constructor(
    private val bookRepository: BookRepository
) {
    @Test
    @Throws(Exception::class)
    fun getBookInformation() {
    }
}