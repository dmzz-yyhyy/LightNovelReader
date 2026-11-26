package indi.dmzz_yyhyy.lightnovelreader.benchmark

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.LightNovelReaderDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfBookMetadataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.DailyCountEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserReadingDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.VolumeEntity
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.paragraph
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import android.database.Cursor
import android.util.Base64 as AndroidUtilBase64
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalDataManager
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupSelection
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.DataSourceEntity
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import java.io.File
import java.util.Base64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

class BenchmarkFixtureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        Thread {
            try {
                val result = when (intent.action) {
                    "indi.dmzz_yyhyy.lightnovelreader.benchmark.MIGRATION_PREPARE_UPGRADE" -> {
                        val database = LightNovelReaderDatabase.getInstance(context)
                        database.close()
                        val file = context.getDatabasePath("light_novel_reader_database")
                        listOf(
                            "",
                            "-wal",
                            "-shm",
                            "-journal"
                        ).forEach { File(file.absolutePath + it).delete() }
                        context.contentResolver.openInputStream(
                            Uri.parse(
                                migrationArgument(
                                    intent,
                                    "input"
                                )
                            )
                        )!!.use { input -> file.outputStream().use { input.copyTo(it) } }
                        val folder = File(context.dataDir, "local_data").apply { mkdirs() }
                        for ((extra, name) in listOf(
                            "sourceA" to "fixture:sourceA",
                            "sourceB" to "fixture:sourceB",
                            "active" to "lightnovelreader:Wenku8"
                        )) {
                            migrationArgument(
                                intent,
                                extra
                            )?.let { uri ->
                                context.contentResolver.openInputStream(Uri.parse(uri))!!
                                    .use { input ->
                                        File(folder, name).outputStream().use { input.copyTo(it) }
                                    }
                            }
                        }
                        "migration=PREPARED"
                    }

                    "indi.dmzz_yyhyy.lightnovelreader.benchmark.MIGRATION_IMPORT" -> {
                        runBlocking {
                            context.contentResolver.openInputStream(
                                Uri.parse(
                                    migrationArgument(intent, "input")
                                )
                            )!!.use {
                                migrationManager(context).import(
                                    it,
                                    intent.getBooleanExtra("overwrite", false)
                                )
                            }
                        }
                        "migration=IMPORTED"
                    }

                    "indi.dmzz_yyhyy.lightnovelreader.benchmark.MIGRATION_EXPORT" -> {
                        runBlocking {
                            context.contentResolver.openOutputStream(
                                Uri.parse(
                                    migrationArgument(
                                        intent,
                                        "output"
                                    )
                                ), "wt"
                            )!!.use { output ->
                                migrationManager(context).export(
                                    output, BackupSelection(
                                        localBookCache = intent.getBooleanExtra("cache", true),
                                        bookshelf = intent.getBooleanExtra("bookshelf", true),
                                        readingRecord = intent.getBooleanExtra("reading", true),
                                        settings = intent.getBooleanExtra("settings", true),
                                        bookshelfId = migrationArgument(
                                            intent,
                                            "shelf"
                                        )?.let { Identifier.parseAs<Identifier.Bookshelf>(it) },
                                        sourceNamespace = migrationArgument(intent, "namespace")
                                    )
                                )
                            }
                        }
                        "migration=EXPORTED"
                    }

                    "indi.dmzz_yyhyy.lightnovelreader.benchmark.MIGRATION_READING" -> {
                        val id = Identifier.parseAs<Identifier.Book>(
                            requireNotNull(
                                migrationArgument(
                                    intent,
                                    "id"
                                )
                            )
                        )
                        val data = runBlocking {
                            requireNotNull(
                                LightNovelReaderDatabase.getInstance(context).userReadingDataDao()
                                    .getEntity(id)
                            )
                        }
                        val json = JSONObject().put(
                            "current",
                            JSONObject(Json.encodeToString(data.currentChapterReadingProgressMap))
                        )
                            .put(
                                "maximum",
                                JSONObject(Json.encodeToString(data.maxChapterReadingProgressMap))
                            )
                        context.contentResolver.openOutputStream(
                            Uri.parse(
                                migrationArgument(
                                    intent,
                                    "output"
                                )
                            ), "wt"
                        )!!.bufferedWriter().use { it.write(json.toString()) }
                        "migration=READING"
                    }

                    "indi.dmzz_yyhyy.lightnovelreader.benchmark.MIGRATION_QUERY" -> {
                        val sql = requireNotNull(migrationArgument(intent, "sql"))
                        require(sql.startsWith("SELECT ") || sql.startsWith("PRAGMA "))
                        val rows = JSONArray()
                        LightNovelReaderDatabase.getInstance(context).openHelper.writableDatabase.query(
                            sql
                        ).use { cursor ->
                            while (cursor.moveToNext()) {
                                val row = JSONObject()
                                for (index in 0 until cursor.columnCount) row.put(
                                    cursor.getColumnName(index), when (cursor.getType(index)) {
                                        Cursor.FIELD_TYPE_NULL -> JSONObject.NULL
                                        Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                                        Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(index)
                                        Cursor.FIELD_TYPE_BLOB -> Base64.getEncoder()
                                            .encodeToString(cursor.getBlob(index))

                                        else -> cursor.getString(index)
                                    }
                                )
                                rows.put(row)
                            }
                        }
                        context.contentResolver.openOutputStream(
                            Uri.parse(
                                migrationArgument(
                                    intent,
                                    "output"
                                )
                            ), "wt"
                        )!!.bufferedWriter().use { it.write(rows.toString()) }
                        "migration=QUERIED"
                    }
                    ACTION_SEED -> {
                        runBlocking {
                            seed(LightNovelReaderDatabase.getInstance(context))
                        }
                        "seed=SUCCEEDED"
                    }

                    ACTION_REEMIT_CHAPTER -> {
                        runBlocking {
                            val database = LightNovelReaderDatabase.getInstance(context)
                            database.chapterContentDao().get(CHAPTER_ONE_ID)?.let {
                                database.chapterContentDao().update(it)
                            }
                        }
                        "reemit=SUCCEEDED"
                    }

                    ACTION_REPORT_PROGRESS -> {
                        val progress = runBlocking {
                            LightNovelReaderDatabase.getInstance(context)
                                .userReadingDataDao()
                                .getEntity(BOOK_ID)
                                ?.currentChapterReadingProgressMap
                                ?.get(CHAPTER_ONE_ID)
                        }
                        "progress=${progress ?: -1f}"
                    }

                    ACTION_REPORT_USER_DATA -> {
                        val path = intent.getStringExtra(EXTRA_USER_DATA_PATH).orEmpty()
                        val value = runBlocking {
                            LightNovelReaderDatabase.getInstance(context)
                                .userDataDao()
                                .get(
                                    Identifier.SettingPath(
                                        if (path in setOf(
                                                "reading_books",
                                                "bookshelf_order",
                                                "search.history",
                                                "completed_download_book_list"
                                            )
                                        ) SOURCE.sourceNamespace else Identifier.HOST_NAMESPACE,
                                        path
                                    )
                                )
                        }
                        "user-data-path=$path;value=${value ?: "<null>"}"
                    }

                    ACTION_REPORT_BOOKSHELF -> {
                        val id = SOURCE.child(
                            Identifier::Bookshelf,
                            intent.getIntExtra(EXTRA_BOOKSHELF_ID, -1).toString()
                        )
                        val shelf = runBlocking {
                            LightNovelReaderDatabase.getInstance(context)
                                .bookshelfDao()
                                .getBookshelf(id)
                        }
                        if (shelf == null) {
                            "bookshelf=$id;value=<null>"
                        } else {
                            "bookshelf=$id;name=${shelf.name};sort=${shelf.sortType};" +
                                "reversed=${shelf.sortReversed};auto-cache=${shelf.autoCache};" +
                                "reminder=${shelf.systemUpdateReminder};" +
                                "all=${shelf.allBookIds.joinToString(",")};" +
                                "pinned=${shelf.pinnedBookIds.joinToString(",")}"
                        }
                    }

                    ACTION_REPORT_BOOK_METADATA -> {
                        val id = intent.getStringExtra(EXTRA_BOOK_ID)?.let {
                            if (it.startsWith("book:")) Identifier.parseAs<Identifier.Book>(it) else SOURCE.child(
                                Identifier::Book,
                                it
                            )
                        } ?: BOOK_ID
                        val metadata = runBlocking {
                            LightNovelReaderDatabase.getInstance(context)
                                .bookshelfDao()
                                .getBookshelfBookMetadataEntity(id)
                        }
                        "book=$id;shelves=${metadata?.bookShelfIds?.joinToString(",") ?: "<null>"}"
                    }

                    ACTION_REPORT_FORMATTING_RULES -> {
                        val rules = runBlocking {
                            LightNovelReaderDatabase.getInstance(context)
                                .formattingRuleDao()
                                .getAllBookRuleEntity()
                        }
                        "formatting-rules=" + rules.joinToString("|") {
                            "book=${it.bookId};name=${it.name};regex=${it.isRegex};" +
                                "match=${it.match};replacement=${it.replacement};" +
                                "enabled=${it.isEnabled}"
                        }
                    }

                    ACTION_EXTEND_RAPID_CHAPTER_CHAIN -> {
                        runBlocking {
                            extendRapidChapterChain(LightNovelReaderDatabase.getInstance(context))
                        }
                        "rapid-chapters=SUCCEEDED"
                    }

                    ACTION_PREPARE_BACKGROUND_IMAGES -> {
                        prepareBackgroundImages(context)
                        "background-images=SUCCEEDED"
                    }

                    ACTION_DELETE_BACKGROUND_IMAGES -> {
                        context.contentResolver.delete(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "${MediaStore.MediaColumns.RELATIVE_PATH} = ?",
                            arrayOf("${Environment.DIRECTORY_PICTURES}/LnrBenchmark/"),
                        )
                        "background-images=DELETED"
                    }

                    ACTION_PREPARE_READER_LAYOUT -> {
                        runBlocking {
                            val database = LightNovelReaderDatabase.getInstance(context)
                            extendRapidChapterChain(database)
                            resetReaderProgress(database)
                        }
                        "reader-layout=SUCCEEDED"
                    }

                    ACTION_LONG_PARAGRAPH -> {
                        runBlocking {
                            val database = LightNovelReaderDatabase.getInstance(context)
                            val chapter = checkNotNull(database.chapterContentDao().get(CHAPTER_ONE_ID))
                            database.chapterContentDao().update(chapter.copy(
                                content = ContentBuilder()
                                    .paragraph { text(LONG_PARAGRAPH) }
                                    .paragraph { }
                                    .paragraph { }
                                    .build(),
                            ))
                            resetReaderProgress(database)
                        }
                        "long-paragraph=SUCCEEDED"
                    }

                    ACTION_REPORT_READING_POSITION -> {
                        val data = runBlocking {
                            LightNovelReaderDatabase.getInstance(context).userReadingDataDao().getEntity(BOOK_ID)
                        }
                        "chapter=${data?.lastReadChapterId};progress=${data?.currentChapterReadingProgressMap?.get(data.lastReadChapterId)}"
                    }

                    else -> "unsupported-action=${intent.action}"
                }
                pending.resultCode = Activity.RESULT_OK
                pending.resultData = result
            } catch (throwable: Throwable) {
                pending.resultCode = Activity.RESULT_CANCELED
                pending.resultData =
                    "exception=${throwable::class.java.simpleName}:${throwable.message}"
            } finally {
                pending.finish()
            }
        }.start()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MigrationAccess {
        fun localDataManager(): LocalDataManager
    }

    private fun migrationArgument(intent: Intent, name: String): String? =
        intent.getStringExtra(name)?.let {
            String(AndroidUtilBase64.decode(it, AndroidUtilBase64.NO_WRAP), Charsets.UTF_8)
        }

    private fun migrationManager(context: Context) =
        EntryPointAccessors.fromApplication(context, MigrationAccess::class.java).localDataManager()

    private suspend fun seed(database: LightNovelReaderDatabase) {
        database.dataSourceDao().register(DataSourceEntity(SOURCE))
        val now = LocalDateTime.now()
        val book = BookInformationEntity(
            id = BOOK_ID,
            title = "Benchmark Sample Novel",
            subtitle = "A deterministic local test fixture",
            coverUri = Uri.EMPTY,
            author = "Benchmark Author",
            description = "Local content used to exercise every book and reader workflow without network access.",
            tags = listOf("Benchmark", "Local", "Automation"),
            publishingHouse = "Benchmark Press",
            wordCount = WordCount(12_345),
            lastUpdated = now.minusDays(1),
            isComplete = false,
        )
        database.bookInformationDao().insert(book)
        database.bookInformationDao().insert(
            BookInformationEntity(
                id = SECOND_BOOK_ID,
                title = "Second Benchmark Novel",
                subtitle = "Independent progress restoration fixture",
                coverUri = Uri.EMPTY,
                author = "Second Benchmark Author",
                description = "A second local book used to prove that chapters and progress do not leak between books.",
                tags = listOf("Benchmark", "Progress"),
                publishingHouse = "Benchmark Press",
                wordCount = WordCount(9_876),
                lastUpdated = now.minusDays(2),
                isComplete = false,
            )
        )

        database.bookVolumesDao().insertVolume(
            VolumeEntity(
                bookId = BOOK_ID,
                volumeId = VOLUME_ID,
                volumeTitle = "Benchmark Volume",
                chapterIds = listOf(CHAPTER_ONE_ID),
                index = 0,
            )
        )
        database.bookVolumesDao().insertVolume(
            VolumeEntity(
                bookId = BOOK_ID,
                volumeId = SECOND_VOLUME_ID,
                volumeTitle = "Benchmark Bonus Volume",
                chapterIds = listOf(CHAPTER_TWO_ID),
                index = 1,
            )
        )
        database.bookVolumesDao().insertChapterInformationEntities(
            ChapterInformationEntity(CHAPTER_ONE_ID, "Benchmark Chapter One"),
            ChapterInformationEntity(CHAPTER_TWO_ID, "Benchmark Chapter Two"),
        )
        database.bookVolumesDao().insertVolume(
            VolumeEntity(
                bookId = SECOND_BOOK_ID,
                volumeId = SECOND_BOOK_VOLUME_ID,
                volumeTitle = "Second Benchmark Volume",
                chapterIds = listOf(SECOND_BOOK_CHAPTER_ONE_ID, SECOND_BOOK_CHAPTER_TWO_ID),
                index = 0,
            )
        )
        database.bookVolumesDao().insertChapterInformationEntities(
            ChapterInformationEntity(SECOND_BOOK_CHAPTER_ONE_ID, "Second Book Chapter One"),
            ChapterInformationEntity(SECOND_BOOK_CHAPTER_TWO_ID, "Second Book Chapter Two"),
        )

        val firstContent = ContentBuilder().apply {
            appendProgressParagraphs("Benchmark progress paragraph")
        }
            .paragraph { text(CHAPTER_ONE_END_MARKER) }
            .build()
        val secondContent = ContentBuilder()
            .paragraph { text(CHAPTER_TWO_START_MARKER) }
            .apply { appendProgressParagraphs("Benchmark chapter two progress paragraph") }
            .build()
        database.chapterContentDao().update(
            ChapterContentEntity(
                id = CHAPTER_ONE_ID,
                title = "Benchmark Chapter One",
                content = firstContent,
                prevChapter = null,
                nextChapter = CHAPTER_TWO_ID,
            )
        )
        database.chapterContentDao().update(
            ChapterContentEntity(
                id = CHAPTER_TWO_ID,
                title = "Benchmark Chapter Two",
                content = secondContent,
                prevChapter = CHAPTER_ONE_ID,
                nextChapter = null,
            )
        )
        val secondBookFirstContent = ContentBuilder().apply {
            appendProgressParagraphs("Second book chapter one progress paragraph")
        }.build()
        val secondBookSecondContent = ContentBuilder().apply {
            appendProgressParagraphs("Second book chapter two progress paragraph")
        }.build()
        database.chapterContentDao().update(
            ChapterContentEntity(
                id = SECOND_BOOK_CHAPTER_ONE_ID,
                title = "Second Book Chapter One",
                content = secondBookFirstContent,
                prevChapter = null,
                nextChapter = SECOND_BOOK_CHAPTER_TWO_ID,
            )
        )
        database.chapterContentDao().update(
            ChapterContentEntity(
                id = SECOND_BOOK_CHAPTER_TWO_ID,
                title = "Second Book Chapter Two",
                content = secondBookSecondContent,
                prevChapter = SECOND_BOOK_CHAPTER_ONE_ID,
                nextChapter = null,
            )
        )

        database.userReadingDataDao().insert(
            UserReadingDataEntity(
                id = BOOK_ID,
                lastReadTime = now,
                totalReadTime = 3_600,
                readingProgress = 0.25f,
                lastReadChapterId = CHAPTER_ONE_ID,
                lastReadChapterTitle = "Benchmark Chapter One",
                currentChapterReadingProgressMap = mapOf(CHAPTER_ONE_ID to 0.25f),
                maxChapterReadingProgressMap = mapOf(CHAPTER_ONE_ID to 0.5f),
            )
        )
        database.userReadingDataDao().insert(
            UserReadingDataEntity(
                id = SECOND_BOOK_ID,
                lastReadTime = now.minusHours(1),
                totalReadTime = 1_800,
                readingProgress = 0.7f,
                lastReadChapterId = SECOND_BOOK_CHAPTER_TWO_ID,
                lastReadChapterTitle = "Second Book Chapter Two",
                currentChapterReadingProgressMap = mapOf(
                    SECOND_BOOK_CHAPTER_ONE_ID to 0.15f,
                    SECOND_BOOK_CHAPTER_TWO_ID to 0.7f,
                ),
                maxChapterReadingProgressMap = mapOf(
                    SECOND_BOOK_CHAPTER_ONE_ID to 0.4f,
                    SECOND_BOOK_CHAPTER_TWO_ID to 0.8f,
                ),
            )
        )
        database.userDataDao().insert(
            UserDataEntity(
                id = UserDataPath.ReadingBooks.identifier(SOURCE),
                value = Json.encodeToString(listOf(BOOK_ID, SECOND_BOOK_ID)),
            )
        )
        database.userDataDao().insert(
            UserDataEntity(
                id = UserDataPath.BookshelfOrder.identifier(SOURCE),
                value = Json.encodeToString(listOf(BOOKSHELF_ID)),
            )
        )
        database.bookshelfDao().insertBookshelf(
            BookshelfEntity(
                id = BOOKSHELF_ID,
                name = "Benchmark Shelf",
                sortType = "default",
                sortReversed = false,
                autoCache = false,
                systemUpdateReminder = false,
                allBookIds = listOf(BOOK_ID, SECOND_BOOK_ID),
                pinnedBookIds = emptyList(),
                updatedBookIds = listOf(BOOK_ID, SECOND_BOOK_ID),
            )
        )
        database.bookshelfDao().insertBookshelfBookMetadata(
            BookshelfBookMetadataEntity(
                id = BOOK_ID,
                lastUpdate = now,
                bookShelfIds = listOf(BOOKSHELF_ID),
            )
        )
        database.bookshelfDao().insertBookshelfBookMetadata(
            BookshelfBookMetadataEntity(
                id = SECOND_BOOK_ID,
                lastUpdate = now.minusHours(1),
                bookShelfIds = listOf(BOOKSHELF_ID),
            )
        )

        val today = LocalDate.now()
        database.bookRecordDao().insertBookRecord(
            BookRecordEntity(
                bookId = BOOK_ID,
                date = today,
                reads = 2,
                seconds = 3_600,
                isFavorited = true,
                firstSeen = LocalTime.of(9, 0),
                lastSeen = LocalTime.of(10, 0),
            )
        )
        val count = Count().apply {
            setMinute(9, 30)
            setMinute(10, 30)
        }
        database.dailyCountDao().insert(DailyCountEntity(SOURCE.sourceNamespace, today, count))
    }

    private fun ContentBuilder.appendProgressParagraphs(prefix: String) {
        repeat(PROGRESS_PARAGRAPH_COUNT) { index ->
            paragraph {
                text(
                    "$prefix ${index + 1}. This deterministic component exercises layout, " +
                        "scrolling, pagination, progress restoration, and formatting. " +
                        "汉语阅读测试。"
                )
            }
        }
    }

    private suspend fun resetReaderProgress(database: LightNovelReaderDatabase) {
        val data = checkNotNull(database.userReadingDataDao().getEntity(BOOK_ID))
        database.userReadingDataDao().insert(data.copy(
            readingProgress = 0f,
            lastReadChapterId = CHAPTER_ONE_ID,
            lastReadChapterTitle = "Benchmark Chapter One",
            currentChapterReadingProgressMap = mapOf(CHAPTER_ONE_ID to 0f),
            maxChapterReadingProgressMap = mapOf(CHAPTER_ONE_ID to 0f),
        ))
    }

    private fun prepareBackgroundImages(context: Context) {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        listOf("lnr-background-light.png" to Color.rgb(24, 188, 104),
            "lnr-background-dark.png" to Color.rgb(164, 48, 196)).forEach { (name, color) ->
            context.contentResolver.delete(collection, "${MediaStore.MediaColumns.DISPLAY_NAME} = ?", arrayOf(name))
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/LnrBenchmark")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = checkNotNull(context.contentResolver.insert(collection, values))
            val bitmap = Bitmap.createBitmap(240, 400, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(color)
                checkNotNull(context.contentResolver.openOutputStream(uri)).use {
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            } finally {
                bitmap.recycle()
            }
        }
    }

    private suspend fun extendRapidChapterChain(database: LightNovelReaderDatabase) {
        val extraChapterIds =
            (3..12).map { SOURCE.child(Identifier::Chapter, "benchmark-chapter-$it") }
        database.bookVolumesDao().insertVolume(
            VolumeEntity(
                bookId = BOOK_ID,
                volumeId = SECOND_VOLUME_ID,
                volumeTitle = "Benchmark Bonus Volume",
                chapterIds = listOf(CHAPTER_TWO_ID) + extraChapterIds,
                index = 1,
            )
        )
        database.bookVolumesDao().insertChapterInformationEntities(
            *extraChapterIds.mapIndexed { index, id ->
                ChapterInformationEntity(id, "Benchmark Chapter ${index + 3}")
            }.toTypedArray()
        )

        database.chapterContentDao().get(CHAPTER_TWO_ID)?.let { chapterTwo ->
            database.chapterContentDao().update(
                chapterTwo.copy(nextChapter = extraChapterIds.first())
            )
        }
        extraChapterIds.forEachIndexed { index, id ->
            val chapterNumber = index + 3
            val content = ContentBuilder()
                .paragraph { text("Benchmark rapid chapter $chapterNumber start marker.") }
                .apply { appendProgressParagraphs("Benchmark rapid chapter $chapterNumber paragraph") }
                .build()
            database.chapterContentDao().update(
                ChapterContentEntity(
                    id = id,
                    title = "Benchmark Chapter $chapterNumber",
                    content = content,
                    prevChapter = if (index == 0) CHAPTER_TWO_ID else extraChapterIds[index - 1],
                    nextChapter = extraChapterIds.getOrNull(index + 1),
                )
            )
        }
    }

    companion object {
        val SOURCE = Identifier.DataSource("lightnovelreader", "Wenku8")
        const val ACTION_SEED = "indi.dmzz_yyhyy.lightnovelreader.benchmark.SEED"
        const val ACTION_REEMIT_CHAPTER =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.REEMIT_CHAPTER"
        const val ACTION_REPORT_PROGRESS =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.REPORT_PROGRESS"
        const val ACTION_EXTEND_RAPID_CHAPTER_CHAIN =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.EXTEND_RAPID_CHAPTER_CHAIN"
        const val ACTION_PREPARE_BACKGROUND_IMAGES =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.PREPARE_BACKGROUND_IMAGES"
        const val ACTION_DELETE_BACKGROUND_IMAGES =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.DELETE_BACKGROUND_IMAGES"
        const val ACTION_PREPARE_READER_LAYOUT =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.PREPARE_READER_LAYOUT"
        const val ACTION_LONG_PARAGRAPH =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.LONG_PARAGRAPH"
        const val ACTION_REPORT_READING_POSITION =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.REPORT_READING_POSITION"
        const val ACTION_REPORT_USER_DATA =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.REPORT_USER_DATA"
        const val ACTION_REPORT_BOOKSHELF =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.REPORT_BOOKSHELF"
        const val ACTION_REPORT_BOOK_METADATA =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.REPORT_BOOK_METADATA"
        const val ACTION_REPORT_FORMATTING_RULES =
            "indi.dmzz_yyhyy.lightnovelreader.benchmark.REPORT_FORMATTING_RULES"
        const val EXTRA_USER_DATA_PATH = "path"
        const val EXTRA_BOOKSHELF_ID = "bookshelf-id"
        const val EXTRA_BOOK_ID = "book-id"

        val BOOK_ID = SOURCE.child(Identifier::Book, "9999999")
        val SECOND_BOOK_ID = SOURCE.child(Identifier::Book, "9999998")
        val VOLUME_ID = SOURCE.child(Identifier::Volume, "benchmark-volume")
        val SECOND_VOLUME_ID = SOURCE.child(Identifier::Volume, "benchmark-volume-2")
        val CHAPTER_ONE_ID = SOURCE.child(Identifier::Chapter, "benchmark-chapter-1")
        val CHAPTER_TWO_ID = SOURCE.child(Identifier::Chapter, "benchmark-chapter-2")
        val SECOND_BOOK_VOLUME_ID = SOURCE.child(Identifier::Volume, "second-benchmark-volume")
        val SECOND_BOOK_CHAPTER_ONE_ID =
            SOURCE.child(Identifier::Chapter, "second-benchmark-chapter-1")
        val SECOND_BOOK_CHAPTER_TWO_ID =
            SOURCE.child(Identifier::Chapter, "second-benchmark-chapter-2")
        const val CHAPTER_ONE_END_MARKER = "Benchmark chapter one end marker."
        const val CHAPTER_TWO_START_MARKER = "Benchmark chapter two start marker."
        val BOOKSHELF_ID = SOURCE.child(Identifier::Bookshelf, "1000001")
        const val PROGRESS_PARAGRAPH_COUNT = 30
        val LONG_PARAGRAPH = (1..100).joinToString("") {
            "跨页段落${it.toString().padStart(3, '0')}，测绘切分必须保持全部文字的顺序，不得遗漏或重复。"
        }
    }
}
