package indi.dmzz_yyhyy.lightnovelreader.data.local.room

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBookDataSource @Inject constructor(
    private val bookInformationDao: BookInformationDao,
    private val bookVolumesDao: BookVolumesDao
) {
    suspend fun getBookInformation(id: Int): BookInformation? = bookInformationDao.get(id)
    suspend fun updateBookInformation(info: BookInformation) = bookInformationDao.update(info)
    suspend fun getBookVolumes(id: Int): BookVolumes? = bookVolumesDao.getBookVolumes(id)
    suspend fun updateBookVolumes(bookId: Int, info: BookVolumes) = bookVolumesDao.update(bookId, info)
}
