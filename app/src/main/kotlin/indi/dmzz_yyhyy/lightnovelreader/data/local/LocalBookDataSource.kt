package indi.dmzz_yyhyy.lightnovelreader.data.local

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBookDataSource @Inject constructor(
    private val bookInformationDao: BookInformationDao,
    private val bookVolumesDao: BookVolumesDao,
    private val chapterContentDao: ChapterContentDao
) {
    suspend fun getBookInformation(id: Int): BookInformation? = bookInformationDao.get(id)
    suspend fun updateBookInformation(info: BookInformation) = bookInformationDao.update(info)
    suspend fun getBookVolumes(id: Int): BookVolumes? = bookVolumesDao.getBookVolumes(id)
    suspend fun updateBookVolumes(bookId: Int, bookVolumes: BookVolumes) = bookVolumesDao.update(bookId, bookVolumes)
    suspend fun getChapterContent(id: Int) = chapterContentDao.get(id)
    suspend fun updateChapterContent(chapterContent: ChapterContent) = chapterContentDao.update(chapterContent)
}
