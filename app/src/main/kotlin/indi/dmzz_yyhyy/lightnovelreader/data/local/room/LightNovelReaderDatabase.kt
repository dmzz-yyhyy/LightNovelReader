package indi.dmzz_yyhyy.lightnovelreader.data.local.room

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.net.toUri
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ChapterReadingProgressMapConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.UriConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.WorldCountConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookshelfDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.FormattingRuleDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserReadingDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfBookMetadataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.FormattingRuleEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserReadingDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.VolumeEntity
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import io.nightfish.lightnovelreader.api.book.WorldCount
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.image
import io.nightfish.lightnovelreader.api.content.builder.simpleText

@Database(
    entities = [
        BookInformationEntity::class,
        VolumeEntity::class,
        ChapterInformationEntity::class,
        ChapterContentEntity::class,
        UserReadingDataEntity::class,
        UserDataEntity::class,
        BookshelfEntity::class,
        BookshelfBookMetadataEntity::class,
        BookRecordEntity::class,
        FormattingRuleEntity::class
    ],
    version = 16,
    exportSchema = false
)
abstract class LightNovelReaderDatabase : RoomDatabase() {
    abstract fun bookInformationDao(): BookInformationDao
    abstract fun bookVolumesDao(): BookVolumesDao
    abstract fun chapterContentDao(): ChapterContentDao
    abstract fun userReadingDataDao(): UserReadingDataDao
    abstract fun userDataDao(): UserDataDao
    abstract fun bookshelfDao(): BookshelfDao
    abstract fun bookRecordDao(): BookRecordDao
    abstract fun formattingRuleDao(): FormattingRuleDao

    companion object {
        @Volatile
        private var INSTANCE: LightNovelReaderDatabase? = null

        fun getInstance(context: Context): LightNovelReaderDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        LightNovelReaderDatabase::class.java,
                        "light_novel_reader_database"
                    )
                        .addMigrations(
                            MIGRATION_6_7,
                            MIGRATION_7_8,
                            MIGRATION_8_9,
                            MIGRATION_9_10,
                            MIGRATION_10_11,
                            MIGRATION_11_12,
                            MIGRATION_12_13,
                            MIGRATION_13_14,
                            MIGRATION_14_15,
                            MIGRATION_15_16
                        )
                        .allowMainThreadQueries()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("drop table book_information")
                db.execSQL(
                    "create table book_information (" +
                            "id INTEGER NOT NULL," +
                            "title TEXT NOT NULL, " +
                            "cover_url TEXT NOT NULL, " +
                            "author TEXT NOT NULL, " +
                            "description TEXT NOT NULL, " +
                            "tags TEXT NOT NULL, " +
                            "publishing_house TEXT NOT NULL, " +
                            "word_count INTEGER NOT NULL," +
                            "last_update TEXT NOT NULL, " +
                            "is_complete INTEGER NOT NULL, " +
                            "PRIMARY KEY(id))"
                )
                db.execSQL("delete from volume")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "create table book_shelf (" +
                            "id INTEGER NOT NULL," +
                            "name TEXT NOT NULL, " +
                            "sort_type TEXT NOT NULL, " +
                            "auto_cache INTEGER NOT NULL, " +
                            "system_update_reminder INTEGER NOT NULL, " +
                            "all_book_ids TEXT NOT NULL, " +
                            "pinned_book_ids TEXT NOT NULL," +
                            "updated_book_ids TEXT NOT NULL, " +
                            "PRIMARY KEY(id))"
                )
                db.execSQL(
                    "create table book_shelf_book_metadata (" +
                            "id INTEGER NOT NULL," +
                            "last_update TEXT NOT NULL, " +
                            "book_shelf_ids TEXT NOT NULL, " +
                            "PRIMARY KEY(id))"
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "alter table user_reading_data " +
                            "add read_completed_chapter_ids text default '' not null"
                )
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("drop table book_information")
                db.execSQL(
                    "create table book_information (" +
                            "id INTEGER NOT NULL," +
                            "title TEXT NOT NULL, " +
                            "subtitle TEXT NOT NULL, " +
                            "cover_url TEXT NOT NULL, " +
                            "author TEXT NOT NULL, " +
                            "description TEXT NOT NULL, " +
                            "tags TEXT NOT NULL, " +
                            "publishing_house TEXT NOT NULL, " +
                            "word_count INTEGER NOT NULL," +
                            "last_update TEXT NOT NULL, " +
                            "is_complete INTEGER NOT NULL, " +
                            "PRIMARY KEY(id))"
                )
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("drop table volume")
                db.execSQL(
                    "create table volume (" +
                            "book_id INTEGER NOT NULL," +
                            "volume_id INTEGER NOT NULL," +
                            "volume_title TEXT NOT NULL, " +
                            "chapter_id_list TEXT NOT NULL, " +
                            "volume_index INTEGER NOT NULL, " +
                            "PRIMARY KEY(volume_id))"
                )
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                CREATE TABLE reading_statistics (
                    date INTEGER NOT NULL PRIMARY KEY,
                    reading_time_count BLOB NOT NULL,
                    foreground_time INTEGER NOT NULL,
                    favorite_books TEXT NOT NULL,
                    started_books TEXT NOT NULL,
                    finished_books TEXT NOT NULL)
                """
                )

                db.execSQL(
                    """
                CREATE TABLE book_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    sessions INTEGER NOT NULL,
                    total_time INTEGER NOT NULL,
                    first_seen INTEGER NOT NULL,
                    last_seen INTEGER NOT NULL)
                """
                )
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                CREATE TABLE formatting_rule (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    is_regex INTEGER NOT NULL,
                    match TEXT NOT NULL,
                    replacement TEXT NOT NULL,
                    is_enabled INTEGER NOT NULL)
                """
                )
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("alter table book_information rename to temp")
                var cursor = db.query("select * from temp")
                db.execSQL(
                    "create table book_information (" +
                            "id TEXT NOT NULL," +
                            "title TEXT NOT NULL, " +
                            "subtitle TEXT NOT NULL, " +
                            "cover_uri TEXT NOT NULL, " +
                            "author TEXT NOT NULL, " +
                            "description TEXT NOT NULL, " +
                            "tags TEXT NOT NULL, " +
                            "publishing_house TEXT NOT NULL, " +
                            "word_count TEXT NOT NULL," +
                            "last_update TEXT NOT NULL, " +
                            "is_complete INTEGER NOT NULL, " +
                            "PRIMARY KEY(id))"
                )
                if (cursor.moveToFirst()) {
                    do {
                        val contentValues = ContentValues()
                        contentValues.put(
                            "id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "id" }).toString()
                        )
                        contentValues.put(
                            "title",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "title" })
                        )
                        contentValues.put(
                            "subtitle",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "subtitle" })
                        )
                        contentValues.put(
                            "cover_uri",
                            UriConverter.uriToString(
                                cursor.getString(cursor.columnNames.indexOfFirst { it == "cover_url" })
                                    .toUri()
                            )
                        )
                        contentValues.put(
                            "author",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "author" })
                        )
                        contentValues.put(
                            "description",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "description" })
                        )
                        contentValues.put(
                            "tags",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "tags" })
                        )
                        contentValues.put(
                            "word_count",
                            WorldCountConverter.worldCountToString(WorldCount(cursor.getInt(cursor.columnNames.indexOfFirst { it == "word_count" })))
                        )
                        contentValues.put(
                            "publishing_house",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "publishing_house" })
                        )
                        contentValues.put(
                            "last_update",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "last_update" })
                        )
                        contentValues.put(
                            "is_complete",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "is_complete" })
                        )
                        db.insert(
                            "book_information",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues
                        )
                    } while (cursor.moveToNext())
                }
                db.execSQL("drop table temp")

                db.execSQL("alter table volume rename to temp")
                cursor = db.query("select * from temp")
                db.execSQL(
                    "create table volume (" +
                            "book_id TEXT NOT NULL," +
                            "volume_id TEXT NOT NULL," +
                            "volume_title TEXT NOT NULL, " +
                            "chapter_id_list TEXT NOT NULL, " +
                            "volume_index INTEGER NOT NULL, " +
                            "PRIMARY KEY(volume_id))"
                )
                if (cursor.moveToFirst()) {
                    do {
                        val contentValues = ContentValues()
                        contentValues.put(
                            "book_id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "book_id" })
                                .toString()
                        )
                        contentValues.put(
                            "volume_id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "volume_id" })
                        )
                        contentValues.put(
                            "volume_title",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "volume_title" })
                        )
                        contentValues.put(
                            "chapter_id_list",
                            UriConverter.uriToString(
                                cursor.getString(cursor.columnNames.indexOfFirst { it == "chapter_id_list" })
                                    .toUri()
                            )
                        )
                        contentValues.put(
                            "volume_index",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "volume_index" })
                        )
                        db.insert(
                            "volume",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues
                        )
                    } while (cursor.moveToNext())
                }
                db.execSQL("drop table temp")

                db.execSQL("alter table chapter_information rename to temp")
                cursor = db.query("select * from temp")
                db.execSQL(
                    "create table chapter_information (" +
                            "id TEXT NOT NULL," +
                            "title TEXT NOT NULL," +
                            "PRIMARY KEY(id))"
                )
                if (cursor.moveToFirst()) {
                    do {
                        val contentValues = ContentValues()
                        contentValues.put(
                            "id",
                            cursor.getString(cursor.getColumnIndexOrThrow("id"))
                        )
                        contentValues.put(
                            "title",
                            cursor.getString(cursor.getColumnIndexOrThrow("title"))
                        )
                        db.insert(
                            "chapter_information",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues
                        )
                    } while (cursor.moveToNext())
                }
                db.execSQL("drop table temp")

                db.execSQL("alter table chapter_content rename to temp")
                cursor = db.query("select * from temp")
                db.execSQL(
                    "create table chapter_content (" +
                            "id TEXT NOT NULL," +
                            "content TEXT NOT NULL," +
                            "lastChapter TEXT NOT NULL," +
                            "nextChapter TEXT NOT NULL," +
                            "title TEXT NOT NULL," +
                            "PRIMARY KEY(id))"
                )
                if (cursor.moveToFirst()) {
                    do {
                        val contentValues = ContentValues()
                        val textContent =
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "content" })

                        val content = ContentBuilder().apply {
                            textContent.split("[image]").forEach {
                                if (it.trim().startsWith("http")) image(it.toUri())
                                else simpleText(it)
                            }
                        }.build()
                        contentValues.put(
                            "id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "id" }).toString()
                        )
                        contentValues.put("content", content.toString())
                        contentValues.put(
                            "lastChapter",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "lastChapter" })
                                .toString()
                        )
                        contentValues.put(
                            "nextChapter",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "nextChapter" })
                                .toString()
                        )
                        contentValues.put(
                            "title",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "title" })
                        )
                        db.insert(
                            "chapter_content",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues
                        )
                    } while (cursor.moveToNext())
                }
                db.execSQL("drop table temp")

                db.execSQL("alter table user_reading_data rename to temp")
                cursor = db.query("select * from temp")
                db.execSQL(
                    "create table user_reading_data (" +
                            "id TEXT NOT NULL," +
                            "last_read_chapter_id TEXT NOT NULL," +
                            "last_read_chapter_progress REAL NOT NULL," +
                            "last_read_chapter_title TEXT NOT NULL," +
                            "last_read_time TEXT NOT NULL," +
                            "read_completed_chapter_ids TEXT DEFAULT '' NOT NULL ," +
                            "reading_progress REAL NOT NULL," +
                            "total_read_time INTEGER NOT NULL," +
                            "PRIMARY KEY(id))"
                )
                if (cursor.moveToFirst()) {
                    do {
                        val contentValues = ContentValues()
                        contentValues.put(
                            "id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "id" }).toString()
                        )
                        contentValues.put(
                            "last_read_chapter_id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "last_read_chapter_id" })
                                .toString()
                        )
                        contentValues.put(
                            "last_read_chapter_progress",
                            cursor.getFloat(cursor.columnNames.indexOfFirst { it == "last_read_chapter_progress" })
                        )
                        contentValues.put(
                            "last_read_chapter_title",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "last_read_chapter_title" })
                        )
                        contentValues.put(
                            "last_read_time",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "last_read_time" })
                        )
                        contentValues.put(
                            "read_completed_chapter_ids",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "read_completed_chapter_ids" })
                        )
                        contentValues.put(
                            "reading_progress",
                            cursor.getFloat(cursor.columnNames.indexOfFirst { it == "reading_progress" })
                        )
                        contentValues.put(
                            "total_read_time",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "total_read_time" })
                        )
                        db.insert(
                            "user_reading_data",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues
                        )
                    } while (cursor.moveToNext())
                }
                db.execSQL("drop table temp")

                db.execSQL("alter table book_shelf_book_metadata rename to temp")
                cursor = db.query("select * from temp")
                db.execSQL(
                    "create table book_shelf_book_metadata (" +
                            "id TEXT NOT NULL," +
                            "book_shelf_ids TEXT NOT NULL," +
                            "last_update TEXT NOT NULL," +
                            "PRIMARY KEY(id))"
                )
                if (cursor.moveToFirst()) {
                    do {
                        val contentValues = ContentValues()
                        contentValues.put(
                            "id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "id" }).toString()
                        )
                        contentValues.put(
                            "book_shelf_ids",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "book_shelf_ids" })
                        )
                        contentValues.put(
                            "last_update",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "last_update" })
                        )
                        db.insert(
                            "book_shelf_book_metadata",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues
                        )
                    } while (cursor.moveToNext())
                }
                db.execSQL("drop table temp")

                db.execSQL("alter table book_records rename to temp")
                cursor = db.query("select * from temp")
                db.execSQL(
                    """
                    CREATE TABLE book_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        date INTEGER NOT NULL,
                        book_id TEXT NOT NULL,
                        sessions INTEGER NOT NULL,
                        total_time INTEGER NOT NULL,
                        first_seen INTEGER NOT NULL,
                        last_seen INTEGER NOT NULL)
                """
                )
                if (cursor.moveToFirst()) {
                    do {
                        val contentValues = ContentValues()
                        contentValues.put(
                            "id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "id" })
                        )
                        contentValues.put(
                            "date",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "date" })
                        )
                        contentValues.put(
                            "book_id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "book_id" })
                                .toString()
                        )
                        contentValues.put(
                            "sessions",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "sessions" })
                        )
                        contentValues.put(
                            "total_time",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "total_time" })
                        )
                        contentValues.put(
                            "first_seen",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "first_seen" })
                        )
                        contentValues.put(
                            "last_seen",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "last_seen" })
                        )
                        db.insert(
                            "book_records",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues
                        )
                    } while (cursor.moveToNext())
                }
                db.execSQL("drop table temp")

                db.execSQL("alter table formatting_rule rename to temp")
                cursor = db.query("select * from temp")
                db.execSQL(
                    """
                CREATE TABLE formatting_rule (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    is_regex INTEGER NOT NULL,
                    match TEXT NOT NULL,
                    replacement TEXT NOT NULL,
                    is_enabled INTEGER NOT NULL)
                """
                )
                if (cursor.moveToFirst()) {
                    do {
                        val contentValues = ContentValues()
                        contentValues.put(
                            "id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "id" })
                        )
                        contentValues.put(
                            "book_id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "book_id" })
                                .toString()
                        )
                        contentValues.put(
                            "name",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "sessions" })
                        )
                        contentValues.put(
                            "is_regex",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "total_time" })
                        )
                        contentValues.put(
                            "match",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "first_seen" })
                        )
                        contentValues.put(
                            "replacement",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "last_seen" })
                        )
                        contentValues.put(
                            "is_enabled",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "last_seen" })
                        )
                        db.insert(
                            "formatting_rule",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues
                        )
                    } while (cursor.moveToNext())
                }
                db.execSQL("drop table temp")
            }
        }


        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("alter table user_reading_data rename to temp")
                val cursor = db.query("select * from temp")
                db.execSQL(
                    "create table user_reading_data (" +
                            "id TEXT NOT NULL," +
                            "last_read_chapter_id TEXT NOT NULL," +
                            "last_read_chapter_title TEXT NOT NULL," +
                            "last_read_time TEXT NOT NULL," +
                            "current_chapter_reading_progress_map BLOB NOT NULL," +
                            "max_chapter_reading_progress_map BLOB NOT NULL," +
                            "total_read_time INTEGER NOT NULL," +
                            "reading_progress REAL NOT NULL," +
                            "PRIMARY KEY(id))"
                )
                if (cursor.moveToFirst()) {
                    do {
                        val contentValues = ContentValues()
                        val lastReadingChapterId =
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "last_read_chapter_id" })
                                .toString()
                        val lastReadingChapterProgress =
                            cursor.getFloat(cursor.columnNames.indexOfFirst { it == "last_read_chapter_progress" })
                        val readingCompletedChaptersIds =
                            ListConverter.stringToStringList(cursor.getString(cursor.columnNames.indexOfFirst { it == "read_completed_chapter_ids" }))
                        val chapterReadingProgressMap = mutableMapOf<String, Float>().apply {
                            for (id in readingCompletedChaptersIds) {
                                put(id, 1f)
                            }
                            put(lastReadingChapterId, lastReadingChapterProgress)
                        }
                        contentValues.put(
                            "id",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "id" }).toString()
                        )
                        contentValues.put("last_read_chapter_id", lastReadingChapterId)
                        contentValues.put(
                            "last_read_chapter_title",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "last_read_chapter_title" })
                        )
                        contentValues.put(
                            "last_read_time",
                            cursor.getString(cursor.columnNames.indexOfFirst { it == "last_read_time" })
                        )
                        contentValues.put(
                            "reading_progress",
                            cursor.getFloat(cursor.columnNames.indexOfFirst { it == "reading_progress" })
                        )
                        contentValues.put(
                            "total_read_time",
                            cursor.getInt(cursor.columnNames.indexOfFirst { it == "total_read_time" })
                        )
                        contentValues.put(
                            "current_chapter_reading_progress_map",
                            ChapterReadingProgressMapConverter.mapToByteArray(
                                chapterReadingProgressMap
                            )
                        )
                        contentValues.put(
                            "max_chapter_reading_progress_map",
                            ChapterReadingProgressMapConverter.mapToByteArray(
                                chapterReadingProgressMap
                            )
                        )
                        db.insert(
                            "user_reading_data",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues
                        )
                    } while (cursor.moveToNext())
                }
                db.execSQL("drop table temp")
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE book_records_new (
                        book_id TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        reading_time_count BLOB NOT NULL,
                        read_count INTEGER NOT NULL DEFAULT 0,
                        is_finished INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(book_id, date)
                    )
                    """
                )

                val legacyCursor = db.query(
                    "SELECT book_id, date, total_time, sessions FROM book_records"
                )

                if (legacyCursor.moveToFirst()) {
                    val bookIdIndex = legacyCursor.getColumnIndexOrThrow("book_id")
                    val dateIndex = legacyCursor.getColumnIndexOrThrow("date")
                    val totalTimeIndex = legacyCursor.getColumnIndexOrThrow("total_time")
                    val sessionsIndex = legacyCursor.getColumnIndexOrThrow("sessions")

                    do {
                        val bookId = legacyCursor.getString(bookIdIndex)
                        val date = legacyCursor.getInt(dateIndex)
                        val totalSeconds = legacyCursor.getInt(totalTimeIndex)
                        val readCount = legacyCursor.getInt(sessionsIndex)

                        val count = Count()
                        val totalMinutes = (totalSeconds / 60).coerceAtMost(24 * 60)
                        var remaining = totalMinutes
                        for (hour in 0..23) {
                            if (remaining <= 0) break
                            val minutes = remaining.coerceAtMost(60)
                            count.setMinute(hour, minutes)
                            remaining -= minutes
                        }

                        val values = ContentValues()
                        values.put("book_id", bookId)
                        values.put("date", date)
                        values.put("reading_time_count", count.toByteArray())
                        values.put("read_count", readCount)
                        values.put("is_finished", 0)
                        db.insert("book_records_new", SQLiteDatabase.CONFLICT_REPLACE, values)
                    } while (legacyCursor.moveToNext())
                }
                legacyCursor.close()

                db.execSQL("DROP TABLE book_records")
                db.execSQL("ALTER TABLE book_records_new RENAME TO book_records")
                db.execSQL("CREATE INDEX index_book_records_date ON book_records(date)")

                val existsCursor = db.query(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    arrayOf("reading_statistics")
                )
                val exists = existsCursor.moveToFirst()
                existsCursor.close()

                if (exists) {
                    val cursor = db.query(
                        "SELECT date, started_books, finished_books FROM reading_statistics"
                    )
                    if (cursor.moveToFirst()) {
                        val dateIndex = cursor.getColumnIndexOrThrow("date")
                        val startedIndex = cursor.getColumnIndexOrThrow("started_books")
                        val finishedIndex = cursor.getColumnIndexOrThrow("finished_books")

                        do {
                            val date = cursor.getInt(dateIndex)
                            val startedBooks = ListConverter.stringToStringList(
                                cursor.getString(startedIndex) ?: ""
                            )
                            val finishedBooks = ListConverter.stringToStringList(
                                cursor.getString(finishedIndex) ?: ""
                            )

                            startedBooks.forEach { bookId ->
                                val values = ContentValues()
                                values.put("book_id", bookId)
                                values.put("date", date)
                                values.put("reading_time_count", Count().toByteArray())
                                values.put("read_count", 1)
                                values.put("is_finished", 0)
                                db.insert("book_records", SQLiteDatabase.CONFLICT_IGNORE, values)
                                db.execSQL(
                                    "UPDATE book_records " +
                                        "SET read_count = CASE WHEN read_count < 1 THEN 1 ELSE read_count END " +
                                        "WHERE book_id = ? AND date = ?",
                                    arrayOf<Any>(bookId, date)
                                )
                            }

                            finishedBooks.forEach { bookId ->
                                val values = ContentValues()
                                values.put("book_id", bookId)
                                values.put("date", date)
                                values.put("reading_time_count", Count().toByteArray())
                                values.put("read_count", 1)
                                values.put("is_finished", 0)
                                db.insert("book_records", SQLiteDatabase.CONFLICT_IGNORE, values)
                                db.execSQL(
                                    "UPDATE book_records " +
                                        "SET read_count = CASE WHEN read_count < 1 THEN 1 ELSE read_count END " +
                                        "WHERE book_id = ? AND date = ?",
                                    arrayOf<Any>(bookId, date)
                                )
                                db.execSQL(
                                    "UPDATE book_records SET is_finished = 1 WHERE book_id = ? AND date = ?",
                                    arrayOf<Any>(bookId, date)
                                )
                            }
                        } while (cursor.moveToNext())
                    }
                    cursor.close()
                }

                db.execSQL("DROP TABLE IF EXISTS reading_statistics")
            }
        }
    }
}