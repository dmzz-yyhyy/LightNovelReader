package indi.dmzz_yyhyy.lightnovelreader.data.local.room.migration

import android.content.ContentValues
import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ChapterReadingProgressMapConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.insertRow
import indi.dmzz_yyhyy.lightnovelreader.utils.convertOldId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** 将版本 18 的数据迁入带命名空间的共库结构 */
@OptIn(ExperimentalSerializationApi::class)
object NamespacedDataMigration : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val source =
            db.query("SELECT value FROM user_data WHERE path = 'settings.data.web_data_source_id'")
                .use {
                    if (it.moveToFirst()) it.getString(0)
                        .convertOldId() else "Wenku8".convertOldId()
                }
        migrateSource(db, source)
    }

    fun migrateSource(db: SupportSQLiteDatabase, source: Identifier.DataSource) {
        db.execSQL("CREATE TABLE datasource (id TEXT NOT NULL, PRIMARY KEY(id))")
        db.execSQL("INSERT INTO datasource(id) VALUES (?)", arrayOf(source.toString()))
        val namespace = source.sourceNamespace
        fun qualify(type: (String, String) -> Identifier, value: String): String =
            source.child(type, value).toString()

        fun ids(type: (String, String) -> Identifier, value: String): String = JsonArray(
            if (value.isEmpty()) emptyList() else value.split(',')
                .map { JsonPrimitive(qualify(type, it)) }
        ).toString()

        val keyColumns = mapOf(
            "book_information" to mapOf("id" to Identifier::Book),
            "volume" to mapOf("book_id" to Identifier::Book, "volume_id" to Identifier::Volume),
            "chapter_information" to mapOf("id" to Identifier::Chapter),
            "chapter_content" to mapOf(
                "id" to Identifier::Chapter,
                "lastChapter" to Identifier::Chapter,
                "nextChapter" to Identifier::Chapter
            ),
            "user_reading_data" to mapOf(
                "id" to Identifier::Book,
                "last_read_chapter_id" to Identifier::Chapter
            ),
            "book_shelf" to mapOf("id" to Identifier::Bookshelf),
            "book_shelf_book_metadata" to mapOf("id" to Identifier::Book),
            "book_records" to mapOf("book_id" to Identifier::Book),
            "formatting_rule" to mapOf("book_id" to Identifier::Book)
        )
        for ((table, columns) in keyColumns) {
            rebuild(db, table, { column, type, notNull ->
                Triple(
                    column, if (table == "book_shelf" && column == "id") "TEXT" else type,
                    notNull && column !in setOf(
                        "lastChapter",
                        "nextChapter",
                        "last_read_chapter_id"
                    )
                )
            }) { row ->
                columns.forEach { (column, type) ->
                    val value = row.getAsString(column)
                    if (value.isNullOrEmpty() && column in setOf(
                            "lastChapter",
                            "nextChapter",
                            "last_read_chapter_id"
                        )
                    ) row.putNull(column)
                    else if (table == "formatting_rule" && value.isEmpty()) row.put(
                        column,
                        Identifier.ALL_BOOKS.toString()
                    )
                    else row.put(column, qualify(type, value))
                }
                when (table) {
                    "volume" -> row.put(
                        "chapter_id_list",
                        ids(Identifier::Chapter, row.getAsString("chapter_id_list"))
                    )

                    "book_shelf" -> listOf(
                        "all_book_ids",
                        "pinned_book_ids",
                        "updated_book_ids"
                    ).forEach { row.put(it, ids(Identifier::Book, row.getAsString(it))) }

                    "book_shelf_book_metadata" -> row.put(
                        "book_shelf_ids",
                        ids(Identifier::Bookshelf, row.getAsString("book_shelf_ids"))
                    )

                    "user_reading_data" -> listOf(
                        "current_chapter_reading_progress_map",
                        "max_chapter_reading_progress_map"
                    ).forEach { column ->
                        val old = Cbor.decodeFromByteArray(
                            MapSerializer(
                                String.serializer(),
                                Float.serializer()
                            ), row.getAsByteArray(column)
                        )
                        val migrated: Map<Identifier.Chapter, Float> =
                            old.mapKeys { source.child(Identifier::Chapter, it.key) }
                        row.put(column, ChapterReadingProgressMapConverter.mapToByteArray(migrated))
                    }

                    "chapter_content" -> row.put(
                        "content",
                        migrateComponentIds(row.getAsString("content"))
                    )
                }
                row
            }
        }
        db.execSQL("ALTER TABLE daily_count RENAME TO legacy_daily_count")
        db.execSQL("CREATE TABLE daily_count (source_namespace TEXT NOT NULL, date INTEGER NOT NULL, time_count BLOB NOT NULL, PRIMARY KEY(source_namespace, date))")
        db.execSQL(
            "INSERT INTO daily_count SELECT ?, date, time_count FROM legacy_daily_count",
            arrayOf(namespace)
        )
        db.execSQL("DROP TABLE legacy_daily_count")
        // 1.2 使用 sp 保存附加行距, 新阅读器使用相对于字体大小的倍数
        db.query("SELECT value FROM user_data WHERE path = 'reader.font_line_height'")
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    val spacing = cursor.getString(0).toFloat()
                    val font =
                        db.query("SELECT value FROM user_data WHERE path = 'reader.font_size'")
                            .use { if (it.moveToFirst()) it.getString(0).toFloat() else 15f }
                    if (font > 0f) db.execSQL(
                        "INSERT OR IGNORE INTO user_data(path, `group`, type, value) VALUES ('reader.line_height_new', 'reader', 'Float', ?)",
                        arrayOf(((font + spacing) / font).toString())
                    )
                }
            }
        db.execSQL("ALTER TABLE user_data RENAME TO legacy_user_data")
        db.execSQL("CREATE TABLE user_data (id TEXT NOT NULL, value TEXT NOT NULL, PRIMARY KEY(id))")
        db.query("SELECT path, type, value FROM legacy_user_data ORDER BY path = 'reader.font_family_uri'")
            .use { cursor ->
                while (cursor.moveToNext()) {
                    val path =
                        if (cursor.getString(0) == "reader.font_family_uri") "reader.font_uri" else cursor.getString(
                            0
                        )
                    val owner = if (path in setOf(
                            "reading_books",
                            "bookshelf_order",
                            "search.history",
                            "completed_download_book_list"
                        )
                    ) namespace else Identifier.HOST_NAMESPACE
                    val key = Identifier.SettingPath(owner, path)
                    val value = cursor.getString(2)
                    val json = when (path) {
                        "settings.data.web_data_source_id" -> JsonPrimitive(
                            value.convertOldId().toString()
                        )

                        "reading_books", "local_book_ids" -> Json.parseToJsonElement(
                            ids(
                                Identifier::Book,
                                value
                            )
                        )

                        "completed_download_book_list" -> JsonArray(
                            value.split(',').filter(String::isNotBlank).map { item ->
                                val parts = item.trim().split('|', limit = 2)
                                require(parts.size == 2) { "Invalid legacy download record" }
                                buildJsonObject {
                                    put("type", parts[0].trim()); put(
                                    "bookId",
                                    qualify(Identifier::Book, parts[1])
                                )
                                }
                            })

                        "bookshelf_order" -> Json.parseToJsonElement(
                            ids(
                                Identifier::Bookshelf,
                                value
                            )
                        )

                        else -> legacyJson(cursor.getString(1), value)
                    }
                    db.execSQL(
                        "INSERT OR IGNORE INTO user_data(id, value) VALUES (?, ?)",
                        arrayOf(key.toString(), json.toString())
                    )
                }
            }
        db.execSQL(
            "INSERT OR IGNORE INTO user_data(id, value) VALUES (?, ?)", arrayOf(
                "setting_path:lightnovelreader:settings.data.web_data_source_id",
                JsonPrimitive(source.toString()).toString()
            )
        )
        db.execSQL("DROP TABLE legacy_user_data")
    }

    private fun legacyJson(type: String, value: String): JsonElement = when (type) {
        "String", "Uri", "Color" -> JsonPrimitive(value)
        "StringList" -> JsonArray(
            if (value.isEmpty()) emptyList() else value.split(',').map(::JsonPrimitive)
        )

        "IntList" -> JsonArray(
            value.split(',').filter(String::isNotBlank).map { JsonPrimitive(it.toInt()) })

        "Boolean" -> JsonPrimitive(value.toBooleanStrict())
        // 旧 BooleanUserData 将类型误写为 Float, 按实际内容保留布尔值
        "Float" -> if (value == "true" || value == "false") JsonPrimitive(value.toBooleanStrict()) else JsonPrimitive(
            value.toFloat()
        )

        "Int" -> JsonPrimitive(value.toInt())
        else -> JsonPrimitive(value)
    }

    private fun migrateComponentIds(content: String): String {
        val root = Json.parseToJsonElement(content).jsonObject
        val components = root["components"] as? JsonArray ?: return content
        return JsonObject(root + ("components" to JsonArray(components.map { value ->
            val component = value.jsonObject
            val old = component["id"]?.jsonPrimitive?.content ?: return@map value
            val parts = old.split(':', limit = 2)
            val id = if (parts.size == 2) Identifier.ContentComponent(parts[0], parts[1])
            else Identifier.ContentComponent(Identifier.HOST_NAMESPACE, old)
            JsonObject(component + ("id" to JsonPrimitive(id.toString())))
        }))).toString()
    }

    /** 按旧表的列重建表结构, 保留默认值、主键和索引 */
    private fun rebuild(
        db: SupportSQLiteDatabase,
        table: String,
        column: (String, String, Boolean) -> Triple<String, String, Boolean>,
        transform: (ContentValues) -> ContentValues
    ) {
        data class Column(
            val name: String,
            val type: String,
            val notNull: Boolean,
            val default: String?,
            val pk: Int
        )

        val columns = db.query("PRAGMA table_info(`$table`)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(
                    Column(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3) != 0,
                        cursor.getString(4),
                        cursor.getInt(5)
                    )
                )
            }
        }
        val indexes = db.query(
            "SELECT sql FROM sqlite_master WHERE type = 'index' AND tbl_name = ? AND sql IS NOT NULL",
            arrayOf(table)
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
        }
        val originalSql = db.query("SELECT sql FROM sqlite_master WHERE name = ?", arrayOf(table))
            .use { it.moveToFirst(); it.getString(0) }
        val autoIncrement = originalSql.contains("AUTOINCREMENT", ignoreCase = true)
        db.execSQL("ALTER TABLE `$table` RENAME TO `legacy_$table`")
        val definitions = columns.map { old ->
            val (name, type, notNull) = column(old.name, old.type, old.notNull)
            "`$name` $type" + (if (notNull) " NOT NULL" else "") +
                    (if (old.default != null) " DEFAULT ${old.default}" else "") +
                    (if (autoIncrement && old.pk != 0) " PRIMARY KEY AUTOINCREMENT" else "")
        }.toMutableList()
        val primary = columns.filter { it.pk != 0 }.sortedBy { it.pk }
        if (primary.isNotEmpty() && !autoIncrement) definitions.add("PRIMARY KEY (" + primary.joinToString { "`${it.name}`" } + ")")
        db.execSQL("CREATE TABLE `$table` (${definitions.joinToString()})")
        db.query("SELECT * FROM `legacy_$table`").use { cursor ->
            while (cursor.moveToNext()) {
                val row = ContentValues()
                columns.forEachIndexed { index, c ->
                    when (cursor.getType(index)) {
                        Cursor.FIELD_TYPE_NULL -> row.putNull(c.name)
                        Cursor.FIELD_TYPE_INTEGER -> row.put(c.name, cursor.getLong(index))
                        Cursor.FIELD_TYPE_FLOAT -> row.put(c.name, cursor.getDouble(index))
                        Cursor.FIELD_TYPE_BLOB -> row.put(c.name, cursor.getBlob(index))
                        else -> row.put(c.name, cursor.getString(index))
                    }
                }
                db.insertRow(table, transform(row))
            }
        }
        db.execSQL("DROP TABLE `legacy_$table`")
        indexes.forEach(db::execSQL)
    }
}
