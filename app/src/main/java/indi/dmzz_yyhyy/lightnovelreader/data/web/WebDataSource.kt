import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes

interface WebDataSource {
    fun getBookInformation(id: Int): BookInformation
    fun getBookVolumes(id: Int): BookVolumes
}
