package indi.dmzz_yyhyy.lightnovelreader.benchmark.migration

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** 旧版本导出格式转换与安装数据库升级的迁移测试 */
@RunWith(AndroidJUnit4::class)
class LegacyDataMigrationScenarios {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val directory = File(context.cacheDir, "migration-fixtures").apply { mkdirs() }

    @Before
    fun reset() {
        clearApp()
    }

    @Test
    fun legacyExportsFrom12AndApi4MigrateToRoomSqlite() {
        for (fixture in listOf("legacy-1.2.0.lnr", "legacy-1.2.4.lnr", "legacy-multi-source.lnr")) {
            clearApp()
            val sourceCount = if (fixture.contains("multi-source")) 2L else 1L
            action(
                "IMPORT",
                "migration=IMPORTED",
                mapOf("input" to uri(asset(fixture)), "overwrite" to "true")
            )
            assertPreserved("lightnovelreader.Wenku8", old12 = fixture.contains("1.2"))
            assertEquals(
                if (fixture.contains("1.2.4")) 1 else 0,
                query("SELECT sort_reversed FROM book_shelf WHERE id = 'bookshelf:lightnovelreader.Wenku8:1000001'").getJSONObject(
                    0
                ).getInt("sort_reversed")
            )
            if (sourceCount > 1) assertPreserved("fixture.sourceA", checkGlobalSettings = false)
            val exported = File(directory, "converted-$fixture")
            action("EXPORT", "migration=EXPORTED", mapOf("output" to uri(exported)))
            withExportDatabase(exported) { db ->
                assertEquals(19L, scalar(db, "PRAGMA user_version"))
                assertEquals(2L * sourceCount, scalar(db, "SELECT count(*) FROM book_information"))
                assertEquals(2L * sourceCount, scalar(db, "SELECT count(*) FROM user_reading_data"))
                assertEquals(sourceCount, scalar(db, "SELECT count(*) FROM book_shelf"))
                assertEquals(4L * sourceCount, scalar(db, "SELECT count(*) FROM chapter_content"))
                assertEquals(
                    0L,
                    scalar(db, "SELECT count(*) FROM user_data WHERE id NOT LIKE 'setting_path:%'")
                )
            }
            // 清空后导入转换后的 SQLite 归档, 检查历史数据经过完整转换后仍被保留
            clearApp()
            action(
                "IMPORT",
                "migration=IMPORTED",
                mapOf("input" to uri(exported), "overwrite" to "true")
            )
            assertPreserved("lightnovelreader.Wenku8", old12 = fixture.contains("1.2"))
            assertEquals(
                if (fixture.contains("1.2.4")) 1 else 0,
                query("SELECT sort_reversed FROM book_shelf WHERE id = 'bookshelf:lightnovelreader.Wenku8:1000001'").getJSONObject(
                    0
                ).getInt("sort_reversed")
            )

            // 分别检查迁移后数据的各个导出类别
            for (category in listOf("cache", "bookshelf", "reading", "settings")) {
                val partial = File(directory, "converted-$category.lnr")
                val options = mutableMapOf("output" to uri(partial))
                for (key in listOf("cache", "bookshelf", "reading", "settings")) options[key] =
                    (key == category).toString()
                action("EXPORT", "migration=EXPORTED", options)
                withExportDatabase(partial) { db ->
                    assertEquals(
                        if (category == "cache") 2L * sourceCount else 0L,
                        scalar(db, "SELECT count(*) FROM book_information")
                    )
                    assertEquals(
                        if (category == "cache") 4L * sourceCount else 0L,
                        scalar(db, "SELECT count(*) FROM chapter_content")
                    )
                    assertEquals(
                        if (category == "bookshelf") sourceCount else 0L,
                        scalar(db, "SELECT count(*) FROM book_shelf")
                    )
                    assertEquals(
                        if (category == "reading") 2L * sourceCount else 0L,
                        scalar(db, "SELECT count(*) FROM user_reading_data")
                    )
                    assertEquals(
                        if (category == "reading") sourceCount else 0L,
                        scalar(db, "SELECT count(*) FROM daily_count")
                    )
                    assertEquals(
                        if (category == "settings") 1L else 0L,
                        scalar(
                            db,
                            "SELECT count(*) FROM user_data WHERE id = 'setting_path:lightnovelreader:reader.font_size'"
                        )
                    )
                    assertEquals(
                        if (category == "reading") 1L else 0L,
                        scalar(
                            db,
                            "SELECT count(*) FROM user_data WHERE id = 'setting_path:lightnovelreader.Wenku8:reading_books'"
                        )
                    )
                }
            }
        }
    }

    @Test
    fun installed12AndApi4DatabasesUpgradeWithAllArchivedSources() {
        for (schema in listOf(16, 17, 18)) {
            clearApp()
            action(
                "PREPARE_UPGRADE", "migration=PREPARED", mapOf(
                    "input" to uri(asset("schema$schema.sqlite")),
                    "sourceA" to uri(asset(if (schema < 18) "source-12-a.raw" else "source-a.raw")),
                    "sourceB" to uri(asset(if (schema < 18) "source-12-b.zip" else "source-b.zip")),
                    "active" to uri(asset(if (schema < 18) "active-12-stale.zip" else "active-stale.zip")),
                )
            )
            stopApp() // 下次启动执行 Room 升级和旧书源归档迁移
            assertPreserved("lightnovelreader.Wenku8", old12 = schema < 18)
            assertPreserved("fixture.sourceA", old12 = schema < 18, checkGlobalSettings = false)
            assertPreserved(
                if (schema < 18) "lightnovelreader.-456" else "fixture.sourceB",
                old12 = schema < 18,
                checkGlobalSettings = false
            )
            assertEquals(6, query("SELECT id FROM book_information").length())
            assertEquals(3, query("SELECT id FROM book_shelf").length())
            assertEquals(3, query("SELECT source_namespace FROM daily_count").length())
            assertEquals(19, query("PRAGMA user_version").getJSONObject(0).getInt("user_version"))
            val records =
                query("SELECT book_id, date, reads, seconds FROM book_records ORDER BY book_id, date").toString()
            assertEquals(
                "A stale current-source archive must not overwrite/accumulate live records",
                0,
                query("SELECT reads FROM book_records WHERE reads = 999").length()
            )
            assertEquals(4, query("SELECT id FROM formatting_rule").length())
            stopApp()
            assertEquals(
                records,
                query("SELECT book_id, date, reads, seconds FROM book_records ORDER BY book_id, date").toString()
            )
            assertEquals(6, query("SELECT id FROM book_information").length())
            val allSources = File(directory, "all-sources-$schema.lnr")
            action("EXPORT", "migration=EXPORTED", mapOf("output" to uri(allSources)))
            withExportDatabase(allSources) { db ->
                assertEquals(6L, scalar(db, "SELECT count(*) FROM book_information"))
                assertEquals(3L, scalar(db, "SELECT count(*) FROM book_shelf"))
                assertEquals(3L, scalar(db, "SELECT count(*) FROM daily_count"))
                assertEquals(4L, scalar(db, "SELECT count(*) FROM formatting_rule"))
            }
            val shelfOnly = File(directory, "one-shelf-$schema.lnr")
            action(
                "EXPORT",
                "migration=EXPORTED",
                mapOf(
                    "output" to uri(shelfOnly),
                    "shelf" to "bookshelf:lightnovelreader.Wenku8:1000001"
                )
            )
            withExportDatabase(shelfOnly) { db ->
                assertEquals(1L, scalar(db, "SELECT count(*) FROM book_shelf"))
                assertEquals(0L, scalar(db, "SELECT count(*) FROM book_information"))
                assertEquals(1L, scalar(db, "SELECT count(*) FROM datasource"))
            }
            assertOverwriteScopeAfterUpgrade(schema, shelfOnly)
        }
    }

    private fun assertOverwriteScopeAfterUpgrade(schema: Int, shelfOnly: File) {
        val otherNamespace = if (schema < 18) "lightnovelreader.-456" else "fixture.sourceB"
        val otherBookId = "book:$otherNamespace:9999999"
        val otherRule =
            query("SELECT * FROM formatting_rule WHERE book_id = '$otherBookId'").getJSONObject(0)
        val otherRuleId = otherRule.getInt("id")
        val globalRules =
            query("SELECT * FROM formatting_rule WHERE book_id = 'book:*:*' ORDER BY id").toString()
        val registration = query("SELECT id FROM datasource ORDER BY id").toString()
        val sourceBackup = File(directory, "source-a-$schema.lnr")
        action(
            "EXPORT",
            "migration=EXPORTED",
            mapOf("output" to uri(sourceBackup), "namespace" to "fixture.sourceA")
        )
        // 模拟另一设备为不同书源分配了相同的自增主键
        rewriteExportDatabase(sourceBackup) { db ->
            db.execSQL(
                "UPDATE formatting_rule SET id = ? WHERE book_id = ?",
                arrayOf<Any>(otherRuleId, "book:fixture.sourceA:9999999")
            )
            db.execSQL(
                "INSERT INTO formatting_rule(id, book_id, name, is_regex, \"match\", replacement, is_enabled) " +
                        "SELECT id + 1, book_id, 'Imported second rule', is_regex, 'old-book-second', 'new-book-second', is_enabled " +
                        "FROM formatting_rule WHERE id = ?", arrayOf(otherRuleId)
            )
        }

        // 合并保留未导出的数据类别
        action(
            "IMPORT",
            "migration=IMPORTED",
            mapOf("input" to uri(shelfOnly), "overwrite" to "false")
        )
        assertEquals(6, query("SELECT id FROM book_information").length())
        assertEquals(3, query("SELECT source_namespace FROM daily_count").length())

        // 单书架备份选择覆盖时也替换所属书源的全部数据
        action(
            "IMPORT",
            "migration=IMPORTED",
            mapOf("input" to uri(shelfOnly), "overwrite" to "true")
        )
        assertEquals(4, query("SELECT id FROM book_information").length())
        assertEquals(4, query("SELECT id FROM user_reading_data").length())
        assertEquals(2, query("SELECT source_namespace FROM daily_count").length())
        assertEquals(3, query("SELECT id FROM book_shelf").length())
        assertEquals(
            0,
            query("SELECT id FROM chapter_content WHERE id LIKE 'chapter:lightnovelreader.Wenku8:%'").length()
        )
        assertEquals(
            0,
            query("SELECT id FROM user_data WHERE id LIKE 'setting_path:lightnovelreader.Wenku8:%'").length()
        )
        assertEquals(
            0,
            query("SELECT id FROM formatting_rule WHERE book_id LIKE 'book:lightnovelreader.Wenku8:%'").length()
        )
        assertPreserved("fixture.sourceA", old12 = schema < 18, checkGlobalSettings = false)
        assertPreserved(otherNamespace, old12 = schema < 18, checkGlobalSettings = false)
        assertEquals(
            globalRules,
            query("SELECT * FROM formatting_rule WHERE book_id = 'book:*:*' ORDER BY id").toString()
        )
        assertEquals(registration, query("SELECT id FROM datasource ORDER BY id").toString())
        assertEquals("21.5", setting("setting_path:lightnovelreader:reader.font_size"))

        // SQLite 归档中的主键冲突不能替换未包含书源的规则
        action(
            "IMPORT",
            "migration=IMPORTED",
            mapOf("input" to uri(sourceBackup), "overwrite" to "true")
        )
        assertEquals(
            otherRule.toString(),
            query("SELECT * FROM formatting_rule WHERE id = $otherRuleId").getJSONObject(0)
                .toString()
        )
        val importedRule =
            query("SELECT id FROM formatting_rule WHERE book_id = 'book:fixture.sourceA:9999999'")
        assertEquals(2, importedRule.length())
        for (index in 0 until importedRule.length()) assertNotEquals(
            otherRuleId,
            importedRule.getJSONObject(index).getInt("id")
        )
        assertEquals(
            2,
            query("SELECT name FROM formatting_rule WHERE book_id = 'book:fixture.sourceA:9999999' AND name IN ('Legacy book rule', 'Imported second rule')").length()
        )
        assertPreserved("fixture.sourceA", old12 = schema < 18, checkGlobalSettings = false)
        assertPreserved(otherNamespace, old12 = schema < 18, checkGlobalSettings = false)
        assertEquals(
            1,
            query("SELECT id FROM book_shelf WHERE id = 'bookshelf:lightnovelreader.Wenku8:1000001'").length()
        )

        // 旧多书源 CBOR 归档使用相同的覆盖范围
        action(
            "IMPORT",
            "migration=IMPORTED",
            mapOf("input" to uri(asset("legacy-multi-source.lnr")), "overwrite" to "true")
        )
        assertPreserved("lightnovelreader.Wenku8")
        assertPreserved("fixture.sourceA", checkGlobalSettings = false)
        assertPreserved(otherNamespace, old12 = schema < 18, checkGlobalSettings = false)
        assertEquals(6, query("SELECT id FROM book_information").length())
        assertEquals(4, query("SELECT id FROM formatting_rule").length())
        assertEquals(
            otherRule.toString(),
            query("SELECT * FROM formatting_rule WHERE id = $otherRuleId").getJSONObject(0)
                .toString()
        )
    }

    private fun assertPreserved(
        namespace: String,
        old12: Boolean = false,
        checkGlobalSettings: Boolean = true
    ) {
        val book =
            query("SELECT * FROM book_information WHERE id = 'book:$namespace:9999999'").getJSONObject(
                0
            )
        assertEquals("Benchmark Sample Novel", book.getString("title"))
        val shelf =
            query("SELECT * FROM book_shelf WHERE id = 'bookshelf:$namespace:1000001'").getJSONObject(
                0
            )
        assertEquals("Benchmark Shelf", shelf.getString("name"))
        assertTrue(
            JSONArray(shelf.getString("all_book_ids")).toString()
                .contains("book:$namespace:9999999")
        )
        val progress =
            query("SELECT * FROM user_reading_data WHERE id = 'book:$namespace:9999999'").getJSONObject(
                0
            )
        assertEquals(3600, progress.getInt("total_read_time"))
        assertEquals(
            2,
            query("SELECT reads FROM book_records WHERE book_id = 'book:$namespace:9999999'").getJSONObject(
                0
            ).getInt("reads")
        )
        assertEquals(0.25, progress.getDouble("reading_progress"), 0.00001)
        assertEquals(
            "chapter:$namespace:benchmark-chapter-1",
            progress.getString("last_read_chapter_id")
        )
        val progressFile = File(directory, "reading.json")
        action(
            "READING",
            "migration=READING",
            mapOf("id" to "book:$namespace:9999999", "output" to uri(progressFile))
        )
        val progressMaps = JSONObject(progressFile.readText())
        assertEquals(
            0.25,
            progressMaps.getJSONObject("current")
                .getDouble("chapter:$namespace:benchmark-chapter-1"),
            0.00001
        )
        assertEquals(
            0.5,
            progressMaps.getJSONObject("maximum")
                .getDouble("chapter:$namespace:benchmark-chapter-1"),
            0.00001
        )
        assertEquals(
            1,
            query("SELECT * FROM daily_count WHERE source_namespace = '$namespace'").length()
        )
        assertTrue(setting("setting_path:$namespace:reading_books").contains("book:$namespace:9999999"))
        assertEquals("[\"first\",\"second\"]", setting("setting_path:$namespace:search.history"))
        val downloads = JSONArray(setting("setting_path:$namespace:completed_download_book_list"))
        assertEquals("CACHE", downloads.getJSONObject(0).getString("type"))
        assertEquals("book:$namespace:9999999", downloads.getJSONObject(0).getString("bookId"))
        val metadata =
            query("SELECT book_shelf_ids FROM book_shelf_book_metadata WHERE id = 'book:$namespace:9999999'").getJSONObject(
                0
            )
        assertEquals("[\"bookshelf:$namespace:1000001\"]", metadata.getString("book_shelf_ids"))
        val chapter =
            query("SELECT content FROM chapter_content WHERE id = 'chapter:$namespace:benchmark-chapter-1'").getJSONObject(
                0
            ).getString("content")
        assertTrue(chapter.contains("content_component:lightnovelreader:paragraph"))
        if (old12) {
            assertTrue(chapter.contains("Legacy 1.2 first paragraph."))
            assertTrue(chapter.contains("Legacy 1.2 second paragraph."))
            assertFalse(chapter.contains("simple_text"))
        }
        if (checkGlobalSettings) {
            assertEquals("true", setting("setting_path:lightnovelreader:reader.keep_screen_on"))
            assertEquals("21.5", setting("setting_path:lightnovelreader:reader.font_size"))
            assertEquals(
                "\"18446462598732840960\"",
                setting("setting_path:lightnovelreader:reader.text_color")
            )
            assertEquals(
                "\"content://fixture/font\"",
                setting("setting_path:lightnovelreader:reader.font_uri")
            )
            assertEquals(
                "\"datasource:lightnovelreader:Wenku8\"",
                setting("setting_path:lightnovelreader:settings.data.web_data_source_id")
            )
            if (old12) assertEquals(
                (28.5 / 21.5),
                setting("setting_path:lightnovelreader:reader.line_height_new").toDouble(),
                0.00001
            )
        }
    }

    private fun setting(id: String): String =
        query("SELECT value FROM user_data WHERE id = '$id'").getJSONObject(0).getString("value")

    private fun query(sql: String): JSONArray {
        val file = File(directory, "query.json")
        action("QUERY", "migration=QUERIED", mapOf("sql" to sql, "output" to uri(file)))
        return JSONArray(file.readText())
    }

    private fun asset(name: String): File = File(directory, name).also { out ->
        context.assets.open("migration/$name")
            .use { input -> out.outputStream().use { input.copyTo(it) } }
    }

    private fun uri(file: File): String {
        val uri = FileProvider.getUriForFile(context, "$TEST_PACKAGE.files", file)
        context.grantUriPermission(
            APP,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        return uri.toString()
    }

    private fun action(name: String, expected: String, args: Map<String, String>) {
        val arguments = args.entries.joinToString(" ") { (key, value) ->
            if (key in setOf(
                    "cache",
                    "bookshelf",
                    "reading",
                    "settings",
                    "overwrite"
                )
            ) "--ez $key $value" else "--es $key ${
                android.util.Base64.encodeToString(
                    value.toByteArray(
                        Charsets.UTF_8
                    ), android.util.Base64.NO_WRAP
                )
            }"
        }
        val output =
            device.executeShellCommand("am broadcast -W -n $APP/.benchmark.BenchmarkFixtureReceiver -a $APP.benchmark.MIGRATION_$name $arguments")
        assertTrue("$name failed: $output", expected in output)
    }

    private fun clearApp() {
        device.executeShellCommand("pm clear $APP")
    }

    private fun stopApp() {
        device.executeShellCommand("am force-stop $APP")
    }

    private fun scalar(db: SQLiteDatabase, sql: String): Long =
        db.rawQuery(sql, null).use { assertTrue(it.moveToFirst()); it.getLong(0) }

    private fun rewriteExportDatabase(file: File, change: (SQLiteDatabase) -> Unit) {
        val database = File(directory, "rewrite-${file.name}.sqlite")
        val manifest = ZipFile(file).use { zip ->
            zip.getInputStream(zip.getEntry("database.sqlite"))
                .use { input -> database.outputStream().use { input.copyTo(it) } }
            zip.getInputStream(zip.getEntry("manifest.json")).use { it.readBytes() }
        }
        SQLiteDatabase.openDatabase(database.path, null, SQLiteDatabase.OPEN_READWRITE).use(change)
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest)
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("database.sqlite"))
            database.inputStream().use { it.copyTo(zip) }
            zip.closeEntry()
        }
    }

    private fun withExportDatabase(file: File, block: (SQLiteDatabase) -> Unit) {
        val extracted = File(directory, "inspection.sqlite")
        var manifest = false
        var database = false
        ZipInputStream(file.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                when (entry.name) {
                    "manifest.json" -> {
                        assertEquals(
                            1,
                            JSONObject(zip.readBytes().decodeToString()).getInt("containerVersion")
                        ); manifest = true
                    }

                    "database.sqlite" -> {
                        extracted.outputStream().use { zip.copyTo(it) }; database = true
                    }

                    else -> fail("Unexpected export entry: ${entry.name}")
                }
            }
        }
        assertTrue(manifest && database)
        SQLiteDatabase.openDatabase(extracted.path, null, SQLiteDatabase.OPEN_READONLY).use(block)
    }

    companion object {
        private const val APP = "indi.dmzz_yyhyy.lightnovelreader"
        private const val TEST_PACKAGE = "$APP.benchmark"
    }
}
