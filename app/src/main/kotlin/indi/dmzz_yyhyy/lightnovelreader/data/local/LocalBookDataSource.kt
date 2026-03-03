package indi.dmzz_yyhyy.lightnovelreader.data.local

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserReadingDataDao
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.book.MutableUserReadingData
import io.nightfish.lightnovelreader.api.book.UserReadingData
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBookDataSource @Inject constructor(
    private val bookInformationDao: BookInformationDao,
    private val bookVolumesDao: BookVolumesDao,
    private val chapterContentDao: ChapterContentDao,
    private val userReadingDataDao: UserReadingDataDao
): LocalBookDataSourceApi {
    override suspend fun getBookInformation(id: String): BookInformation? = bookInformationDao.get(id)
    override fun updateBookInformation(info: BookInformation) = bookInformationDao.insert(info)
    override suspend fun getBookVolumes(id: String): BookVolumes? = bookVolumesDao.getBookVolumes(id)
    override fun updateBookVolumes(bookVolumes: BookVolumes) =
        bookVolumesDao.insertVolume(bookVolumes.bookId, bookVolumes)

    override suspend fun getChapterContent(id: String) = chapterContentDao.get(id)?.let {
        MutableChapterContent(
            it.id,
            it.title,
            it.content,
            it.lastChapter,
            it.nextChapter
        )
    }
    override fun updateChapterContent(chapterContent: ChapterContent) =
        chapterContentDao.update(chapterContent)

    override fun getUserReadingData(id: String) = userReadingDataDao.getEntity(id).let {
        it ?: return@let MutableUserReadingData.empty().apply { this.id = id }
        MutableUserReadingData(
            it.id,
            it.lastReadTime,
            it.totalReadTime,
            it.readingProgress,
            it.lastReadChapterId,
            it.lastReadChapterTitle,
            it.currentChapterReadingProgressMap,
            it.maxChapterReadingProgressMap

        )
    }

    override fun getUserReadingDataFlow(id: String) = userReadingDataDao.getEntityFlow(id).map {
        it ?: return@map MutableUserReadingData.empty().apply { this.id = id }
        MutableUserReadingData(
            it.id,
            it.lastReadTime,
            it.totalReadTime,
            it.readingProgress,
            it.lastReadChapterId,
            it.lastReadChapterTitle,
            it.currentChapterReadingProgressMap,
            it.maxChapterReadingProgressMap
        )
    }

    override fun updateUserReadingData(id: String, update: (MutableUserReadingData) -> UserReadingData) {
        val userReadingData = userReadingDataDao.getEntityWithoutFlow(id)?.let {
            MutableUserReadingData(
                it.id,
                it.lastReadTime,
                it.totalReadTime,
                it.readingProgress,
                it.lastReadChapterId,
                it.lastReadChapterTitle,
                it.currentChapterReadingProgressMap,
                it.maxChapterReadingProgressMap
            )
        } ?: MutableUserReadingData.empty().apply { this.id = id }
        val new = update(userReadingData.apply { this.id = id })
        userReadingDataDao.insert(
            id = new.id,
            lastReadTime = new.lastReadTime,
            totalReadTime = new.totalReadTime,
            readingProgress = new.readingProgress,
            lastReadChapterId = new.lastReadChapterId,
            lastReadChapterTitle = new.lastReadChapterTitle,
            currentChapterReadingProgressMap = new.currentChapterReadingProgressMap,
            maxChapterReadingProgressMap = new.maxChapterReadingProgressMap
        )
    }

    override fun getAllUserReadingData(): List<UserReadingData> =
        userReadingDataDao.getAll().map {
            MutableUserReadingData(
                it.id,
                it.lastReadTime,
                it.totalReadTime,
                it.readingProgress,
                it.lastReadChapterId,
                it.lastReadChapterTitle,
                it.currentChapterReadingProgressMap,
                it.maxChapterReadingProgressMap
            )
        }

    override suspend fun isChapterContentExists(id: String): Boolean =
        chapterContentDao.getId(id) != null

    override fun clear() {
        userReadingDataDao.clear()
        bookInformationDao.clear()
        bookVolumesDao.clear()
        chapterContentDao.clear()
    }
}