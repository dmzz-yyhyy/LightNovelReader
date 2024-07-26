package indi.dmzz_yyhyy.lightnovelreader.data.local

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserReadingDataDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

@Singleton
class LocalBookDataSource @Inject constructor(
    private val bookInformationDao: BookInformationDao,
    private val bookVolumesDao: BookVolumesDao,
    private val chapterContentDao: ChapterContentDao,
    private val userReadingDataDao: UserReadingDataDao
) {
    suspend fun getBookInformation(id: Int): BookInformation? = bookInformationDao.get(id)
    suspend fun updateBookInformation(info: BookInformation) = bookInformationDao.update(info)
    suspend fun getBookVolumes(id: Int): BookVolumes? = bookVolumesDao.getBookVolumes(id)
    suspend fun updateBookVolumes(bookId: Int, bookVolumes: BookVolumes) = bookVolumesDao.update(bookId, bookVolumes)
    suspend fun getChapterContent(id: Int) = chapterContentDao.get(id)
    suspend fun updateChapterContent(chapterContent: ChapterContent) = chapterContentDao.update(chapterContent)
    fun getUserReadingData(id: Int) = userReadingDataDao.getEntity(id).map {
        it ?: return@map UserReadingData.empty().copy(id = id)
        UserReadingData(
            it.id,
            it.lastReadTime,
            it.totalReadTime,
            it.readingProgress,
            it.lastReadChapterId,
            it.lastReadChapterTitle,
            it.lastReadChapterProgress
        )
    }
    suspend fun updateUserReadingData(id: Int, update: (UserReadingData) -> UserReadingData) {
        getUserReadingData(id).collect { it ->
            userReadingDataDao.update(update(it.copy(id = id)).let {
                var data = it
                if (it.readingProgress.isNaN()) data = data.copy(readingProgress = 0.0f)
                if (it.lastReadChapterProgress.isNaN()) data = data.copy(lastReadChapterProgress = 0.0f)
                return@let data
            })
        }
    }
}
