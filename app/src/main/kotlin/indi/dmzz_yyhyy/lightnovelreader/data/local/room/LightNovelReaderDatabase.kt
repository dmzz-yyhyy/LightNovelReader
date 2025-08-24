package indi.dmzz_yyhyy.lightnovelreader.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookshelfDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.FormattingRuleDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ReadingStatisticsDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserReadingDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.ExtensionDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.InstalledExtensionDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.RepositoryDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfBookMetadataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.FormattingRuleEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserReadingDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.VolumeEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.ExtensionBookDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.ExtensionSettingDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionBookEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionSettingEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepositoryEntity

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
        ReadingStatisticsEntity::class,
        BookRecordEntity::class,
        FormattingRuleEntity::class,
        RepositoryEntity::class,
        ExtensionEntity::class,
        InstalledExtensionEntity::class,
        ExtensionBookEntity::class,
        ExtensionSettingEntity::class
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
    abstract fun readingStatisticsDao(): ReadingStatisticsDao
    abstract fun bookRecordDao(): BookRecordDao
    abstract fun formattingRuleDao(): FormattingRuleDao
    abstract fun repositoryDao(): RepositoryDao
    abstract fun extensionDao(): ExtensionDao
    abstract fun installedExtensionDao(): InstalledExtensionDao
    abstract fun extensionBookDao(): ExtensionBookDao
    abstract fun extensionSettingDao(): ExtensionSettingDao

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
                        "light_novel_reader_database")
                        .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, Migration_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
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
                db.execSQL( "create table book_information (" +
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
                        "PRIMARY KEY(id))" )
                db.execSQL("delete from volume")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL( "create table book_shelf (" +
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
                db.execSQL( "create table book_shelf_book_metadata (" +
                        "id INTEGER NOT NULL," +
                        "last_update TEXT NOT NULL, " +
                        "book_shelf_ids TEXT NOT NULL, " +
                        "PRIMARY KEY(id))"
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("alter table user_reading_data " +
                        "add read_completed_chapter_ids text default '' not null")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("drop table book_information")
                db.execSQL( "create table book_information (" +
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
                        "PRIMARY KEY(id))" )
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("drop table volume")
                db.execSQL( "create table volume (" +
                        "book_id INTEGER NOT NULL," +
                        "volume_id INTEGER NOT NULL," +
                        "volume_title TEXT NOT NULL, " +
                        "chapter_id_list TEXT NOT NULL, " +
                        "volume_index INTEGER NOT NULL, " +
                        "PRIMARY KEY(volume_id))" )
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                CREATE TABLE reading_statistics (
                    date INTEGER NOT NULL PRIMARY KEY,
                    reading_time_count BLOB NOT NULL,
                    foreground_time INTEGER NOT NULL,
                    favorite_books TEXT NOT NULL,
                    started_books TEXT NOT NULL,
                    finished_books TEXT NOT NULL)
                """)

                db.execSQL("""
                CREATE TABLE book_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    sessions INTEGER NOT NULL,
                    total_time INTEGER NOT NULL,
                    first_seen INTEGER NOT NULL,
                    last_seen INTEGER NOT NULL)
                """)
            }
        }

        private val Migration_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                CREATE TABLE formatting_rule (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    is_regex INTEGER NOT NULL,
                    match TEXT NOT NULL,
                    replacement TEXT NOT NULL,
                    is_enabled INTEGER NOT NULL)
                """)
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                CREATE TABLE repositories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    url TEXT NOT NULL,
                    isEnabled INTEGER NOT NULL DEFAULT 1,
                    lastUpdated INTEGER NOT NULL)
                """)

                db.execSQL("""
                CREATE TABLE repository_extensions (
                    id INTEGER NOT NULL,
                    repoId INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    fileName TEXT NOT NULL,
                    imageURL TEXT NOT NULL,
                    lang TEXT NOT NULL,
                    version TEXT NOT NULL,
                    md5 TEXT NOT NULL,
                    type TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    PRIMARY KEY(repoId, id),
                    FOREIGN KEY(repoId) REFERENCES repositories(id) ON DELETE CASCADE)
                """)

                db.execSQL("""
                CREATE TABLE installed_extensions (
                    id INTEGER PRIMARY KEY,
                    repoId INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    fileName TEXT NOT NULL,
                    imageURL TEXT NOT NULL,
                    lang TEXT NOT NULL,
                    version TEXT NOT NULL,
                    md5 TEXT NOT NULL,
                    type TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    isEnabled INTEGER NOT NULL DEFAULT 1,
                    installDate INTEGER NOT NULL)
                """)

                db.execSQL("CREATE INDEX index_repository_extensions_repoId ON repository_extensions(repoId)")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // The installed_extensions table already exists with the correct Shosetsu schema
                // We just need to add our new extension_books table
                
                // Create extension_books table with correct schema matching ExtensionBookEntity
                db.execSQL("""
                CREATE TABLE extension_books (
                    internalBookId INTEGER PRIMARY KEY NOT NULL,
                    extensionId INTEGER NOT NULL,
                    originalBookId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    description TEXT NOT NULL,
                    imageUrl TEXT NOT NULL,
                    url TEXT NOT NULL,
                    isInLibrary INTEGER NOT NULL,
                    addedToLibraryDate INTEGER,
                    lastUpdated INTEGER NOT NULL,
                    FOREIGN KEY(extensionId) REFERENCES installed_extensions(id) ON DELETE CASCADE)
                """)

                db.execSQL("CREATE INDEX index_extension_books_extensionId ON extension_books(extensionId)")
                db.execSQL("CREATE INDEX index_extension_books_originalBookId ON extension_books(originalBookId)")
                db.execSQL("CREATE UNIQUE INDEX index_extension_books_extensionId_originalBookId ON extension_books(extensionId, originalBookId)")
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add extension_settings table for managing extension settings
                db.execSQL("""
                CREATE TABLE extension_settings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    extensionId INTEGER NOT NULL,
                    key TEXT NOT NULL,
                    value TEXT NOT NULL,
                    type TEXT NOT NULL,
                    defaultValue TEXT NOT NULL,
                    title TEXT NOT NULL,
                    summary TEXT NOT NULL,
                    isVisible INTEGER NOT NULL,
                    FOREIGN KEY(extensionId) REFERENCES installed_extensions(id) ON DELETE CASCADE)
                """)

                db.execSQL("CREATE INDEX index_extension_settings_extensionId ON extension_settings(extensionId)")
                db.execSQL("CREATE UNIQUE INDEX index_extension_settings_extensionId_key ON extension_settings(extensionId, key)")
            }
        }
    }
}