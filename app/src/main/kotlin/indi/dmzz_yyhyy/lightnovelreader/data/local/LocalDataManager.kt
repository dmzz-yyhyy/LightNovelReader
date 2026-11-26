package indi.dmzz_yyhyy.lightnovelreader.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupSelection
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.LegacyArchive
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.SQLiteBackup
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.LightNovelReaderDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.storage.StorageUsageRepository
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: LightNovelReaderDatabase,
    private val storageUsageRepository: StorageUsageRepository
) {
    private val backup get() = SQLiteBackup(context, database)
    suspend fun export(output: OutputStream, selection: BackupSelection) =
        backup.export(output, selection)

    suspend fun import(input: InputStream, overwrite: Boolean) {
        backup.import(input, overwrite)
        storageUsageRepository.invalidateSnapshot()
    }

    /** 暂存全部旧书源归档, 在同一事务中导入, 提交后再重命名原目录 */
    suspend fun migrateSourceArchives() = withContext(Dispatchers.IO) {
        val folder = File(context.dataDir, "local_data")
        val archives = folder.listFiles()?.filter { it.isFile } ?: return@withContext
        if (archives.isEmpty()) return@withContext
        val staging =
            File(context.cacheDir, "source-migration-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val selected =
                database.userDataDao().get(UserDataPath.Settings.Data.WebDataSourceId.identifier())
                    ?.let { Json.decodeFromString(Identifier.DataSource.Serializer, it) }
                    ?: Identifier.DataSource("lightnovelreader", "Wenku8")
            val reader = LegacyArchive(context)
            val files = archives.filter { reader.sourceIdentity(it) != selected }
                .mapIndexed { index, file ->
                    reader.convertSource(
                        file,
                        File(staging, "$index.sqlite")
                    )
                }
            backup.importFiles(
                files, overwrite = false, migrationMarker = Identifier.SettingPath(
                    "lightnovelreader", "internal.migrations.source_archives"
                ), legacyArchive = true
            )
            // 数据库提交成功后再归档原目录
            val migrated = File(context.dataDir, "local_data_migrated")
            check(!migrated.exists() && folder.renameTo(migrated)) { "Could not retire source archives" }
        } finally {
            staging.deleteRecursively()
        }
    }
}
