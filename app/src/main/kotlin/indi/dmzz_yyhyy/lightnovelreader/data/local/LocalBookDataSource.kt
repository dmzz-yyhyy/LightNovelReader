package indi.dmzz_yyhyy.lightnovelreader.data.local

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserReadingDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.UserReadingData
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBookDataSource @Inject constructor(
    private val sourceProvider: WebBookDataSourceProvider,
    private val bookInformationDao: BookInformationDao,
    private val bookVolumesDao: BookVolumesDao,
    private val chapterContentDao: ChapterContentDao,
    private val userReadingDataDao: UserReadingDataDao
) : LocalBookDataSourceApi {
    override suspend fun getBookInformation(id: Identifier.Book): BookInformation? =
        bookInformationDao.get(id)

    override suspend fun updateBookInformation(info: BookInformation) =
        bookInformationDao.insert(info)

    override suspend fun getBookVolumes(id: Identifier.Book): BookVolumes? =
        bookVolumesDao.getBookVolumes(id)

    override suspend fun updateBookVolumes(bookVolumes: BookVolumes) =
        bookVolumesDao.insertVolume(bookVolumes.bookId, bookVolumes)

    override suspend fun getChapterContent(id: Identifier.Chapter) =
        chapterContentDao.get(id)?.let {
        ChapterContent(
            it.id,
            it.title,
            it.content,
            it.prevChapter,
            it.nextChapter
        )
    }

    override suspend fun updateChapterContent(chapterContent: ChapterContent) =
        chapterContentDao.update(chapterContent)

    override suspend fun getUserReadingData(id: Identifier.Book) =
        userReadingDataDao.getEntity(id).let {
        it ?: return@let UserReadingData(id)
        UserReadingData(
            it.id,
            if (it.lastReadTime == LocalDateTime.MIN) null else it.lastReadTime,
            it.totalReadTime,
            it.readingProgress,
            it.lastReadChapterId,
            it.lastReadChapterTitle.ifEmpty { null },
            it.currentChapterReadingProgressMap,
            it.maxChapterReadingProgressMap

        )
    }

    fun getUserReadingDataFlow(id: Identifier.Book) = userReadingDataDao.getEntityFlow(id).map {
        it ?: return@map UserReadingData(
            id,
            null,
            0,
            0f,
            null,
            null,
            emptyMap(),
            emptyMap()
        )
        UserReadingData(
            it.id,
            if (it.lastReadTime == LocalDateTime.MIN) null else it.lastReadTime,
            it.totalReadTime,
            it.readingProgress,
            it.lastReadChapterId,
            it.lastReadChapterTitle.ifEmpty { null },
            it.currentChapterReadingProgressMap,
            it.maxChapterReadingProgressMap
        )
    }

    override suspend fun updateUserReadingData(
        id: Identifier.Book,
        update: (UserReadingData) -> UserReadingData
    ) {
        val userReadingData = userReadingDataDao.getEntity(id)?.let {
            UserReadingData(
                it.id,
                it.lastReadTime,
                it.totalReadTime,
                it.readingProgress,
                it.lastReadChapterId,
                it.lastReadChapterTitle,
                it.currentChapterReadingProgressMap,
                it.maxChapterReadingProgressMap
            )
        } ?: UserReadingData(id)
        val new = update(userReadingData)
        userReadingDataDao.insert(
            id = new.id,
            lastReadTime = new.lastReadTime ?: LocalDateTime.MIN,
            totalReadTime = new.totalReadTime,
            readingProgress = new.readingProgress,
            lastReadChapterId = new.lastReadChapterId,
            lastReadChapterTitle = new.lastReadChapterTitle ?: "",
            currentChapterReadingProgressMap = new.currentChapterReadingProgressMap,
            maxChapterReadingProgressMap = new.maxChapterReadingProgressMap
        )
    }

    override suspend fun getAllUserReadingData(): List<UserReadingData> =
        userReadingDataDao.getAll(sourceProvider.value.id.sourceNamespace).map {
            UserReadingData(
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

    override suspend fun isChapterContentExists(id: Identifier.Chapter): Boolean =
        chapterContentDao.getId(id) != null

    override suspend fun clear() {
        userReadingDataDao.clear(sourceProvider.value.id.sourceNamespace)
        bookInformationDao.clear(sourceProvider.value.id.sourceNamespace)
        bookVolumesDao.clear(sourceProvider.value.id.sourceNamespace)
        chapterContentDao.clear(sourceProvider.value.id.sourceNamespace)
    }
}
