package indi.dmzz_yyhyy.lightnovelreader.data.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.LightNovelReaderDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageUsageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: LightNovelReaderDatabase,
    private val pluginManager: PluginManager,
    private val userDataRepository: UserDataRepository
) {
    companion object {
        private const val DB_NAME = "light_novel_reader_database"
    }

    private val snapshotUserData = userDataRepository.stringUserData(UserDataPath.Settings.Data.StorageUsageSnapshot.path)
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getCachedSnapshot(): StorageUsageSnapshot? = withContext(Dispatchers.IO) {
        snapshotUserData.get()?.let {
            runCatching { json.decodeFromString<StorageUsageSnapshot>(it) }.getOrNull()
        }
    }

    suspend fun refreshSnapshot(): StorageUsageSnapshot = withContext(Dispatchers.IO) {
        val storageStatsDao = database.storageStatsDao()

        val bookInfoBytesMap = storageStatsDao.getBookInformationBytes()
            .associate { it.id to it.bytes }

        val volumeRows = storageStatsDao.getVolumeStorageRows()
        val bookVolumeBytesMap = mutableMapOf<String, Long>()
        val chapterToBookMap = mutableMapOf<String, String>()
        volumeRows.forEach { row ->
            bookVolumeBytesMap[row.bookId] =
                (bookVolumeBytesMap[row.bookId] ?: 0L) + row.bytes
            ListConverter.stringToStringList(row.chapterIdList)
                .filter { it.isNotBlank() }
                .forEach { chapterToBookMap[it] = row.bookId }
        }

        var orphanChapterInformationBytes = 0L
        val bookChapterInfoBytesMap = mutableMapOf<String, Long>()
        storageStatsDao.getChapterInformationBytes().forEach { row ->
            val bookId = chapterToBookMap[row.id]
            if (bookId == null) {
                orphanChapterInformationBytes += row.bytes
            } else {
                bookChapterInfoBytesMap[bookId] =
                    (bookChapterInfoBytesMap[bookId] ?: 0L) + row.bytes
            }
        }

        var orphanChapterContentBytes = 0L
        val bookChapterContentBytesMap = mutableMapOf<String, Long>()
        storageStatsDao.getChapterContentBytes().forEach { row ->
            val bookId = chapterToBookMap[row.id]
            if (bookId == null) {
                orphanChapterContentBytes += row.bytes
            } else {
                bookChapterContentBytesMap[bookId] =
                    (bookChapterContentBytesMap[bookId] ?: 0L) + row.bytes
            }
        }

        val bookIds = linkedSetOf<String>().apply {
            addAll(bookInfoBytesMap.keys)
            addAll(bookVolumeBytesMap.keys)
            addAll(bookChapterInfoBytesMap.keys)
            addAll(bookChapterContentBytesMap.keys)
        }

        val books = bookIds.map { bookId ->
            BookStorageUsage(
                bookId = bookId,
                bookInformationBytes = bookInfoBytesMap[bookId] ?: 0L,
                volumeBytes = bookVolumeBytesMap[bookId] ?: 0L,
                chapterInformationBytes = bookChapterInfoBytesMap[bookId] ?: 0L,
                chapterContentBytes = bookChapterContentBytesMap[bookId] ?: 0L
            )
        }.sortedByDescending { it.totalBytes }

        val appBytes = getAppFileBytes(context)
        val databaseDiskBytes = getRoomFileBytes(context, DB_NAME)
        val pluginRoots = setOf(pluginManager.pluginsDir.canonicalPath, pluginManager.pluginsTempDir.canonicalPath)
        val pluginBytes = pluginRoots.sumOf { fileSize(File(it)) }
        val cacheBytes = childrenSizeExcept(context.cacheDir, pluginRoots)
        val otherFileBytes = childrenSizeExcept(
            root = context.dataDir,
            excludedPaths = setOf(
                context.cacheDir.canonicalPath,
                context.getDatabasePath(DB_NAME).parentFile?.canonicalPath ?: "",
                context.dataDir.resolve("code_cache").canonicalPath,
                context.dataDir.resolve("app_webview").canonicalPath,
                context.filesDir.canonicalPath,
                context.dataDir.resolve("no_backup").canonicalPath,
            ) + pluginRoots
        ) + fileSize(context.filesDir)

        val allBookMetadataBytes =
            bookInfoBytesMap.values.sum() +
                    bookVolumeBytesMap.values.sum() +
                    bookChapterInfoBytesMap.values.sum() +
                    orphanChapterInformationBytes

        val snapshot = StorageUsageSnapshot(
            totalBytes = appBytes + databaseDiskBytes + pluginBytes + cacheBytes + otherFileBytes,
            appBytes = appBytes,
            databaseDiskBytes = databaseDiskBytes,
            pluginBytes = pluginBytes,
            cacheBytes = cacheBytes,
            otherFileBytes = otherFileBytes,
            allBookMetadataBytes = allBookMetadataBytes,
            orphanChapterInfoBytes = orphanChapterInformationBytes,
            orphanChapterContentBytes = orphanChapterContentBytes,
            books = books,
            calculatedAt = System.currentTimeMillis()
        )
        snapshotUserData.set(json.encodeToString(StorageUsageSnapshot.serializer(), snapshot))
        snapshot
    }

    suspend fun invalidateSnapshot() = withContext(Dispatchers.IO) {
        userDataRepository.remove(UserDataPath.Settings.Data.StorageUsageSnapshot.path)
    }

    private fun fileSize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        return file.listFiles()?.sumOf(::fileSize) ?: 0L
    }

    private fun childrenSizeExcept(root: File, excludedPaths: Set<String>): Long {
        if (!root.exists()) return 0L
        return root.listFiles()
            ?.filterNot { excludedPaths.contains(it.canonicalPath) }
            ?.sumOf(::fileSize)
            ?: 0L
    }
}

fun getRoomFileBytes(context: Context, dbName: String): Long {
    val db = context.getDatabasePath(dbName)
    val wal = File(db.path + "-wal")
    val shm = File(db.path + "-shm")
    return listOf(db, wal, shm)
        .filter { it.exists() }
        .sumOf { it.length() }
}

fun getAppFileBytes(context: Context): Long {
    val appInfo = context.applicationInfo
    return buildSet {
        appInfo.sourceDir?.let(::add)
        appInfo.publicSourceDir?.let(::add)
        appInfo.splitSourceDirs?.forEach(::add)
        appInfo.splitPublicSourceDirs?.forEach(::add)
    }.sumOf { path ->
        val file = File(path)
        if (file.exists()) file.length() else 0L
    }
}
