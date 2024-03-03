package indi.dmzz_yyhyy.lightnovelreader.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.room.dao.BookChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.room.dao.BookMetadataDao
import indi.dmzz_yyhyy.lightnovelreader.data.room.dao.BookVolumeDao
import indi.dmzz_yyhyy.lightnovelreader.data.room.dao.ReadingBookDao
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.*


@Database(
    entities = [ReadingBook::class, BookChapterContent::class, BookVolume::class, BookChapter::class, BookMetadata::class],
    version = 30400004
)
abstract class LightNovelReaderDatabase : RoomDatabase() {
    abstract fun readingBookDao(): ReadingBookDao
    abstract fun bookMetadataDao(): BookMetadataDao
    abstract fun bookChapterListDao(): BookVolumeDao
    abstract fun bookChapterContentDao(): BookChapterContentDao

    companion object {
        private val instance: LightNovelReaderDatabase? = null

        @Synchronized
        fun getDB(context: Context): LightNovelReaderDatabase {
            return instance ?: buildDB(context)
        }

        private fun buildDB(context: Context): LightNovelReaderDatabase {
            val builder = Room.databaseBuilder(
                context,
                LightNovelReaderDatabase::class.java,
                "lightNovelReaderDatabase"
            ).fallbackToDestructiveMigration()
            return builder.build()
        }
    }
}