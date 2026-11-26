package indi.dmzz_yyhyy.lightnovelreader.data.local.backup

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.local.legacy.AppLocalData
import indi.dmzz_yyhyy.lightnovelreader.data.local.legacy.LocalData
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.LightNovelReaderDatabase
import indi.dmzz_yyhyy.lightnovelreader.utils.convertOldId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.zip.ZipInputStream

/** 读取旧 CBOR 归档, 将其转换到可由 Room 增量迁移的历史数据库 */
@OptIn(ExperimentalSerializationApi::class)
class LegacyArchive(private val context: Context) {
    private val cbor = Cbor { ignoreUnknownKeys = true }

    suspend fun prepareRuleIds(
        source: LightNovelReaderDatabase,
        target: LightNovelReaderDatabase
    ) {
        val existing = target.formattingRuleDao().getAllBookRuleEntity().toMutableList()
        var next = existing.maxOfOrNull { it.id ?: 0 } ?: 0
        val incoming = source.formattingRuleDao().getAllBookRuleEntity()
        next = maxOf(next, incoming.maxOfOrNull { it.id ?: 0 } ?: 0)
        for (rule in incoming) {
            val same = existing.firstOrNull { it.copy(id = null) == rule.copy(id = null) }
            val conflict = existing.firstOrNull { it.id == rule.id }
            if (same != null) source.formattingRuleDao().deleteRule(requireNotNull(rule.id))
            else if (conflict != null && (conflict.bookId != rule.bookId || rule.bookId == Identifier.ALL_BOOKS)) {
                source.formattingRuleDao().deleteRule(requireNotNull(rule.id))
                val remapped = rule.copy(id = ++next)
                source.formattingRuleDao().insertRuleEntity(remapped)
                existing.add(remapped)
            } else existing.add(rule)
        }
    }

    fun convertApp(file: File, directory: File): List<File> {
        val app = cbor.decodeFromByteArray<AppLocalData>(file.readBytes())
        require(app.version == 0) { "Unsupported legacy archive version: ${app.version}" }
        // 旧导出会将当前书源数据追加在归档之后, 同书源重复时以末尾数据为准
        val selectedSource =
            app.globalLocalData.userDataEntities.firstOrNull { it.path == "settings.data.web_data_source_id" }
                ?.value?.convertOldId() ?: app.localDataList.lastOrNull()?.webBookDataSourceId?.id
            ?: "Wenku8".convertOldId()
        val sources =
            app.localDataList.associateBy { requireNotNull(it.webBookDataSourceId).id }.values.toList()
        val result = sources.mapIndexed { index, data ->
            val source = requireNotNull(data.webBookDataSourceId) { "Missing source identity" }.id
            createDatabase(
                File(directory, "legacy-$index.sqlite"),
                data,
                source
            ).also(::removeSourceSelection)
        }.toMutableList()
        if (app.globalLocalData.userDataEntities.isNotEmpty()) {
            result.add(
                createDatabase(
                    File(directory, "legacy-global.sqlite"), app.globalLocalData,
                    selectedSource
                )
            )
        }
        return result
    }

    fun sourceIdentity(file: File): Identifier.DataSource =
        readSource(file).webBookDataSourceId?.id ?: file.name.convertOldId()

    fun convertSource(file: File, destination: File): File {
        val data = readSource(file)
        val source = data.webBookDataSourceId?.id ?: file.name.convertOldId()
        return createDatabase(destination, data, source).also(::removeSourceSelection)
    }

    private fun removeSourceSelection(file: File) {
        LightNovelReaderDatabase.openFile(context, file).use { database ->
            database.openHelper.writableDatabase.execSQL(
                "DELETE FROM user_data WHERE id = ?",
                arrayOf("setting_path:lightnovelreader:settings.data.web_data_source_id")
            )
        }
    }

    private fun readSource(file: File): LocalData {
        val zip = file.inputStream().use { it.read() == 0x50 && it.read() == 0x4b }
        val bytes = if (zip) ZipInputStream(file.inputStream()).use {
            require(it.nextEntry?.name == "data") { "Invalid legacy source archive" }
            it.readBytes()
        } else file.readBytes()
        return cbor.decodeFromByteArray(bytes)
    }

    private fun createDatabase(file: File, data: LocalData, source: Identifier.DataSource): File {
        // CBOR v0 没有记录数据库版本, 补齐 sortReversed 默认值后统一转换为版本 17
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.beginTransaction()
            try {
                context.assets.open("migration/v17.sql").bufferedReader().useLines { lines ->
                    lines.filter(String::isNotBlank).forEach { db.execSQL(it) }
                }
                fun insert(table: String, vararg values: Pair<String, Any?>) {
                    val row = ContentValues()
                    for ((key, value) in values) {
                        when (value) {
                            null -> row.putNull(key)
                            is String -> row.put(key, value)
                            is Int -> row.put(key, value)
                            is Float -> row.put(key, value)
                            is Boolean -> row.put(key, value)
                            is ByteArray -> row.put(key, value)
                            else -> error("Unsupported frozen archive value: $key")
                        }
                    }
                    val columns = row.keySet().toList()
                    db.execSQL(
                        "INSERT INTO `$table` (${columns.joinToString { "`$it`" }}) VALUES (${columns.joinToString { "?" }})",
                        columns.map { row.get(it) }.toTypedArray()
                    )
                }
                data.bookInformationEntities.forEach { e ->
                    insert(
                        "book_information",
                        "id" to e.id,
                        "title" to e.title,
                        "subtitle" to e.subtitle,
                        "cover_uri" to e.coverUri,
                        "author" to e.author,
                        "description" to e.description,
                        "tags" to e.tags.joinToString(","),
                        "publishing_house" to e.publishingHouse,
                        "word_count" to "${e.wordCount.count}|.:.|${e.wordCount.unit}|.:.|${e.wordCount.unitResId}",
                        "last_update" to e.lastUpdated,
                        "is_complete" to e.isComplete
                    )
                }
                data.volumeEntities.forEach { e ->
                    insert(
                        "volume",
                        "book_id" to e.bookId,
                        "volume_id" to e.volumeId,
                        "volume_title" to e.volumeTitle,
                        "chapter_id_list" to e.chapterIds.joinToString(","),
                        "volume_index" to e.index
                    )
                }
                data.chapterInformationEntities.forEach { e ->
                    insert(
                        "chapter_information",
                        "id" to e.id,
                        "title" to e.title
                    )
                }
                data.chapterContentEntities.forEach { e ->
                    insert(
                        "chapter_content",
                        "id" to e.id,
                        "title" to e.title,
                        "content" to e.content,
                        "lastChapter" to e.prevChapter,
                        "nextChapter" to e.nextChapter
                    )
                }
                data.userReadingDataEntities.forEach { e ->
                    insert(
                        "user_reading_data",
                        "id" to e.id,
                        "last_read_time" to e.lastReadTime,
                        "total_read_time" to e.totalReadTime,
                        "reading_progress" to e.readingProgress,
                        "last_read_chapter_id" to e.lastReadChapterId,
                        "last_read_chapter_title" to e.lastReadChapterTitle,
                        "current_chapter_reading_progress_map" to cbor.encodeToByteArray(e.currentChapterReadingProgressMap),
                        "max_chapter_reading_progress_map" to cbor.encodeToByteArray(e.maxChapterReadingProgressMap)
                    )
                }
                data.bookshelfEntities.forEach { e ->
                    insert(
                        "book_shelf",
                        "id" to e.id,
                        "name" to e.name,
                        "sort_type" to e.sortType,
                        "sort_reversed" to e.sortReversed,
                        "auto_cache" to e.autoCache,
                        "system_update_reminder" to e.systemUpdateReminder,
                        "all_book_ids" to e.allBookIds.joinToString(","),
                        "pinned_book_ids" to e.pinnedBookIds.joinToString(","),
                        "updated_book_ids" to e.updatedBookIds.joinToString(",")
                    )
                }
                data.bookshelfBookMetadataEntities.forEach { e ->
                    insert(
                        "book_shelf_book_metadata",
                        "id" to e.id,
                        "last_update" to e.lastUpdate,
                        "book_shelf_ids" to e.bookShelfIds.joinToString(",")
                    )
                }
                data.bookRecordEntities.forEach { e ->
                    insert(
                        "book_records",
                        "book_id" to e.bookId,
                        "date" to e.date.replace("-", "").toInt(),
                        "reads" to e.reads,
                        "seconds" to e.seconds,
                        "is_finished" to e.isFinished,
                        "is_favorited" to e.isFavorited,
                        "first_seen" to LocalTime.parse(e.firstSeen)
                            .format(DateTimeFormatter.ofPattern("HHmmss")).toInt(),
                        "last_seen" to LocalTime.parse(e.lastSeen)
                            .format(DateTimeFormatter.ofPattern("HHmmss")).toInt()
                    )
                }
                data.dailyCountEntities.forEach { e ->
                    insert(
                        "daily_count",
                        "date" to e.date.replace("-", "").toInt(),
                        "time_count" to Base64.getDecoder().decode(e.timeCount)
                    )
                }
                data.formattingRuleEntities.forEach { e ->
                    insert(
                        "formatting_rule",
                        "id" to e.id,
                        "book_id" to e.bookId,
                        "name" to e.name,
                        "is_regex" to e.isRegex,
                        "match" to e.match,
                        "replacement" to e.replacement,
                        "is_enabled" to e.isEnabled
                    )
                }
                data.userDataEntities.forEach { e ->
                    insert(
                        "user_data",
                        "path" to e.path,
                        "group" to e.group,
                        "type" to e.type,
                        "value" to e.value
                    )
                }
                // 每个旧书源归档使用自身的书源身份进行迁移
                db.execSQL(
                    "INSERT OR REPLACE INTO user_data(path, `group`, type, value) VALUES ('settings.data.web_data_source_id', 'settings.data', 'String', ?)",
                    arrayOf(source.toString())
                )
                db.version = 17
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        return file
    }
}
