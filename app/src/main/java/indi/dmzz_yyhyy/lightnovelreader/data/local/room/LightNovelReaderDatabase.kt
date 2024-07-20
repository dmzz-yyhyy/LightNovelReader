package indi.dmzz_yyhyy.lightnovelreader.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity.VolumeEntity

@Database(
    entities = [BookInformationEntity::class, VolumeEntity::class, ChapterInformationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LightNovelReaderDatabase : RoomDatabase() {
    abstract fun bookInformationDao(): BookInformationDao
    abstract fun bookVolumesDao(): BookVolumesDao

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
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
