package indi.dmzz_yyhyy.lightnovelreader.data.local

import android.content.Context
import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.asErr
import com.github.michaelbull.result.runCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.local.cbor.AppLocalData
import indi.dmzz_yyhyy.lightnovelreader.data.local.cbor.LocalData
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookshelfDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.FormattingRuleDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ReadingStatisticsDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserReadingDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.utils.readAppLocalData
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("OPT_IN_USAGE")
@Singleton
class LocalDataManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val webDataSourceProvider: WebBookDataSourceProvider,
    private val bookBookInformationDao: BookInformationDao,
    private val bookRecordDao: BookRecordDao,
    private val bookshelfDao: BookshelfDao,
    private val chapterContentDao: ChapterContentDao,
    private val bookVolumesDao: BookVolumesDao,
    private val formattingRuleDao: FormattingRuleDao,
    private val readingStatisticsDao: ReadingStatisticsDao,
    private val userReadingDataDao: UserReadingDataDao,
    private val userDataDao: UserDataDao
) {
    companion object {
        const val TAG = "LocalDataManager"
    }

    val currentAppDataVersion = 0
    val localDataDir = context.dataDir.resolve("local_data").also {
        if (!it.exists()) it.mkdirs()
    }
    val webDataSourceUserDataPathSet = mutableSetOf<String>()

    fun registerWebDataSourceUserData(path: String) {
        webDataSourceUserDataPathSet.add(path)
    }

    suspend fun exportAppLocalData(
        localBookCache: Boolean = true,
        bookshelf: Boolean = true,
        readingRecord: Boolean = true,
        settings: Boolean = true
    ): Result<AppLocalData, Throwable> {
        val localDataList = mutableListOf<LocalData>()
        localDataDir.listFiles()?.forEach {
            it.inputStream().use { inputStream ->
                Cbor.decodeFromByteArray<LocalData>(inputStream.readAppLocalData())
            }.let(localDataList::add)
        }
        exportCurrentLocalData(
            localBookCache, bookshelf, readingRecord, settings
        ).let {
            it.component1() ?: return it.asErr()
        }.let(localDataList::add)
        val globalLocalData = LocalData.empty()
            .copy(userDataEntities = if (settings) userDataDao.getAllEntities().filter {
                !webDataSourceUserDataPathSet.contains(it.path)
            }
            else emptyList())
        return Ok(
            AppLocalData(
                version = currentAppDataVersion,
                localDataList = localDataList,
                globalLocalData = globalLocalData
            )
        )
    }

    suspend fun exportCurrentLocalData(
        localBookCache: Boolean = true,
        bookshelf: Boolean = true,
        readingRecord: Boolean = true,
        settings: Boolean = true
    ): Result<LocalData, Throwable> {
        val exportOptionLocalData = ExportOptionLocalData(
            bookBookInformationDao = bookBookInformationDao,
            bookRecordDao = bookRecordDao,
            bookshelfDao = bookshelfDao,
            chapterContentDao = chapterContentDao,
            bookVolumesDao = bookVolumesDao,
            formattingRuleDao = formattingRuleDao,
            readingStatisticsDao = readingStatisticsDao,
            userReadingDataDao = userReadingDataDao,
            userDataDao = userDataDao,
            webDataSourceUserDataPathSet = webDataSourceUserDataPathSet
        ).apply {
            this.localBookCache.enable = localBookCache
            this.bookshelf.enable = bookshelf
            this.readingRecord.enable = readingRecord
            this.settings.enable = settings
        }

        return runCatching {
            exportOptionLocalData.solve()
        }.andThen {
            Ok(
                LocalData(
                    webBookDataSourceId = webDataSourceProvider.default.id,
                    bookInformationEntities = exportOptionLocalData.bookInformationEntities,
                    bookRecordEntities = exportOptionLocalData.bookRecordEntities,
                    bookshelfEntities = exportOptionLocalData.bookshelfEntities,
                    bookshelfBookMetadataEntities = exportOptionLocalData.bookshelfBookMetadataEntities,
                    chapterContentEntities = exportOptionLocalData.chapterContentEntities,
                    chapterInformationEntities = exportOptionLocalData.chapterInformationEntities,
                    formattingRuleEntities = exportOptionLocalData.formattingRuleEntities,
                    readingStatisticsEntities = exportOptionLocalData.readingStatisticsEntities,
                    userReadingDataEntities = exportOptionLocalData.userReadingDataEntities,
                    volumeEntities = exportOptionLocalData.volumeEntities,
                    userDataEntities = exportOptionLocalData.userDataEntities
                )
            )
        }
    }

    suspend fun importAppLocalData(appLocalData: AppLocalData): Result<Unit, Throwable> {
        if (currentAppDataVersion != appLocalData.version) {
            Log.e(TAG, "Unsupported data versions")
            return Err(Error("Unsupported data versions"))
        }
        importLocalDataToDatabase(appLocalData.globalLocalData)
        for (localData in appLocalData.localDataList) {
            importLocalData(localData).let {
                it.component1() ?: return it.asErr()
            }
        }
        return Ok(Unit)
    }

    suspend fun importLocalData(localData: LocalData): Result<Unit, Throwable> {
        val webDataSourceId = webDataSourceProvider.default.id
        return if (localData.webBookDataSourceId == webDataSourceId) {
            importLocalDataToDatabase(localData)
        } else importLocalDataToFile(localData)
    }

    fun importLocalDataToFile(localData: LocalData): Result<Unit, Throwable> {
        val webDataSourceId = localData.webBookDataSourceId
        val oldLocalDataFile = localDataDir.resolve(webDataSourceId.toString())
        if (!oldLocalDataFile.exists()) {
            return oldLocalDataFile.outputStream().buffered().use {
                runCatching {
                    it.write(Cbor.encodeToByteArray(localData))
                }
            }
        }
        val oldLocalData = oldLocalDataFile.inputStream().buffered().use {
            runCatching {
                Cbor.decodeFromByteArray<LocalData>(it.readBytes())
            }
        }.let {
            it.component1() ?: return it.asErr()
        }
        val newBookInformationEntitiesMap = mapOf(
            *localData.bookInformationEntities.map { Pair(it.id, it) }.toTypedArray()
        )
        val newBookRecordEntitiesMap = mapOf(
            *localData.bookRecordEntities.map { Pair(it.id, it) }.toTypedArray()
        )
        val newBookshelfEntitiesMap = mapOf(
            *localData.bookshelfEntities.map { Pair(it.id, it) }.toTypedArray()
        )
        val newBookshelfBookMetadataEntitiesMap = mapOf(
            *localData.bookshelfBookMetadataEntities.map { Pair(it.id, it) }.toTypedArray()
        )
        val newChapterContentEntitiesMap = mapOf(
            *localData.chapterContentEntities.map { Pair(it.id, it) }.toTypedArray()
        )
        val newChapterInformationEntitiesMap = mapOf(
            *localData.chapterInformationEntities.map { Pair(it.id, it) }.toTypedArray()
        )
        val newFormattingRuleEntitiesMap = mapOf(
            *localData.formattingRuleEntities.map { Pair(it.id, it) }.toTypedArray()
        )
        val newReadingStatisticsEntitiesMap = mapOf(
            *localData.readingStatisticsEntities.map { Pair(it.date, it) }.toTypedArray()
        )
        val newUserDataEntitiesMap = mapOf(
            *localData.userDataEntities.map { Pair(it.path, it) }.toTypedArray()
        )
        val newUserReadingDataEntitiesMap = mapOf(
            *localData.userReadingDataEntities.map { Pair(it.id, it) }.toTypedArray()
        )
        val newVolumeEntitiesMap = mapOf(
            *localData.volumeEntities.map { Pair(it.volumeId, it) }.toTypedArray()
        )
        val mergedLocalData = localData.copy(
            bookInformationEntities = oldLocalData.bookInformationEntities.map { old ->
                newBookInformationEntitiesMap[old.id]?.let {
                    old.merge(it)
                } ?: old
            },
            bookRecordEntities = oldLocalData.bookRecordEntities.map { old ->
                newBookRecordEntitiesMap[old.id]?.let {
                    old.merge(it)
                } ?: old
            },
            bookshelfEntities = oldLocalData.bookshelfEntities.map { old ->
                newBookshelfEntitiesMap[old.id]?.let {
                    old.merge(it)
                } ?: old
            },
            bookshelfBookMetadataEntities = oldLocalData.bookshelfBookMetadataEntities.map { old ->
                newBookshelfBookMetadataEntitiesMap[old.id]?.let {
                    old.merge(it)
                } ?: old
            },
            chapterContentEntities = oldLocalData.chapterContentEntities.map { old ->
                newChapterContentEntitiesMap[old.id]?.let {
                    old.merge(it)
                } ?: old
            },
            chapterInformationEntities = oldLocalData.chapterInformationEntities.map { old ->
                newChapterInformationEntitiesMap[old.id]?.let {
                    old.merge(it)
                } ?: old
            },
            formattingRuleEntities = oldLocalData.formattingRuleEntities.map { old ->
                newFormattingRuleEntitiesMap[old.id]?.let {
                    old.merge(it)
                } ?: old
            },
            readingStatisticsEntities = oldLocalData.readingStatisticsEntities.map { old ->
                newReadingStatisticsEntitiesMap[old.date]?.let {
                    old.merge(it)
                } ?: old
            },
            userDataEntities = oldLocalData.userDataEntities.map { old ->
                newUserDataEntitiesMap[old.path]?.let {
                    old.merge(it)
                } ?: old
            },
            userReadingDataEntities = oldLocalData.userReadingDataEntities.map { old ->
                newUserReadingDataEntitiesMap[old.id]?.let {
                    old.merge(it)
                } ?: old
            },
            volumeEntities = oldLocalData.volumeEntities.map { old ->
                newVolumeEntitiesMap[old.volumeId]?.let {
                    old.merge(it)
                } ?: old
            },
        )
        return oldLocalDataFile.outputStream().buffered().use {
            runCatching {
                it.write(Cbor.encodeToByteArray(mergedLocalData))
            }
        }
    }

    suspend fun importLocalDataToDatabase(localData: LocalData): Result<Unit, Throwable> {
        for (entity in localData.bookInformationEntities) {
            bookBookInformationDao.insert(
                bookBookInformationDao.getEntity(entity.id)?.let(entity::merge) ?: entity
            )
        }
        for (entity in localData.bookRecordEntities) {
            bookRecordDao.insertBookRecord(
                bookRecordDao.getEntity(entity.id)?.let(entity::merge) ?: entity
            )
        }
        for (entity in localData.bookshelfEntities) {
            bookshelfDao.insertBookshelf(
                bookshelfDao.getBookshelf(entity.id)?.let(entity::merge) ?: entity
            )
        }
        for (entity in localData.bookshelfBookMetadataEntities) {
            bookshelfDao.insertBookshelfBookMetadata(
                bookshelfDao.getBookshelfBookMetadataEntity(
                    entity.id
                )?.let(entity::merge) ?: entity
            )
        }
        for (entity in localData.chapterContentEntities) {
            chapterContentDao.update(chapterContentDao.get(entity.id)?.let(entity::merge) ?: entity)
        }
        for (entity in localData.chapterInformationEntities) {
            bookVolumesDao.insertChapterInformationEntities(
                bookVolumesDao.getChapterInformationEntity(
                    entity.id
                )?.let(entity::merge) ?: entity
            )
        }
        for (entity in localData.volumeEntities) {
            bookVolumesDao.insertVolume(
                bookVolumesDao.getVolumeEntity(entity.volumeId)?.let(entity::merge) ?: entity
            )
        }
        for (entity in localData.formattingRuleEntities) {
            formattingRuleDao.update(
                formattingRuleDao.getBookRuleEntity(entity.id)?.let(entity::merge) ?: entity
            )
        }
        for (entity in localData.readingStatisticsEntities) {
            readingStatisticsDao.insertReadingStatistics(
                readingStatisticsDao.getReadingStatisticsForDate(
                    entity.date
                )?.let(entity::merge) ?: entity
            )
        }
        for (entity in localData.userReadingDataEntities) {
            userReadingDataDao.insert(
                userReadingDataDao.getEntity(entity.id)?.let(entity::merge) ?: entity
            )
        }
        for (entity in localData.userDataEntities) {
            userDataDao.insert(userDataDao.getEntity(entity.path)?.let(entity::merge) ?: entity)
        }
        return Ok(Unit)
    }

    fun cleanDatabaseWithoutGlobalUserData() {
        bookBookInformationDao.clear()
        bookRecordDao.clear()
        bookshelfDao.clear()
        bookVolumesDao.clear()
        chapterContentDao.clear()
        formattingRuleDao.clear()
        readingStatisticsDao.clear()
        userReadingDataDao.clear()

        for (entity in userDataDao.getAllEntities()) {
            if (!webDataSourceUserDataPathSet.contains(entity.path)) continue
            userDataDao.remove(entity.path)
        }
    }

    init {
        registerWebDataSourceUserData(UserDataPath.Settings.Data.WebDataSourceId.path)
        registerWebDataSourceUserData(UserDataPath.ReadingBooks.path)
        registerWebDataSourceUserData(UserDataPath.CompletedDownloadBookList.path)
        registerWebDataSourceUserData(UserDataPath.Search.History.path)
    }
}