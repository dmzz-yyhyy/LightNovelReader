package indi.dmzz_yyhyy.lightnovelreader.data.local

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookshelfDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.DailyCountDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.FormattingRuleDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserReadingDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfBookMetadataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.DailyCountEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.FormattingRuleEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserReadingDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.VolumeEntity

class ExportOptionLocalData(
    private val bookBookInformationDao: BookInformationDao,
    private val bookRecordDao: BookRecordDao,
    private val dailyCountDao: DailyCountDao,
    private val bookshelfDao: BookshelfDao,
    private val chapterContentDao: ChapterContentDao,
    private val bookVolumesDao: BookVolumesDao,
    private val formattingRuleDao: FormattingRuleDao,
    private val userReadingDataDao: UserReadingDataDao,
    private val userDataDao: UserDataDao,
    webDataSourceUserDataPathSet: Set<String>
) {
    abstract class OptionSolver {
        var enable: Boolean = false
        abstract suspend fun solve()
    }
    
    val bookInformationEntities = mutableListOf<BookInformationEntity>()
    val bookRecordEntities = mutableListOf<BookRecordEntity>()
    val dailyCountEntities = mutableListOf<DailyCountEntity>()
    val bookshelfEntities = mutableListOf<BookshelfEntity>()
    val bookshelfBookMetadataEntities = mutableListOf<BookshelfBookMetadataEntity>()
    val chapterContentEntities = mutableListOf<ChapterContentEntity>()
    val chapterInformationEntities = mutableListOf<ChapterInformationEntity>()
    val formattingRuleEntities = mutableListOf<FormattingRuleEntity>()
    val userDataEntities = mutableListOf<UserDataEntity>()
    val userReadingDataEntities = mutableListOf<UserReadingDataEntity>()
    val volumeEntities = mutableListOf<VolumeEntity>()

    private val options = mutableListOf<OptionSolver>()

    val localBookCache = object : OptionSolver() {
        override suspend fun solve() {
            bookInformationEntities.addAll(bookBookInformationDao.getAllEntities())
            volumeEntities.addAll(bookVolumesDao.getAllVolumeEntities())
            chapterContentEntities.addAll(chapterContentDao.getAllEntities())
            chapterInformationEntities.addAll(bookVolumesDao.getAllChapterInformationEntities())
        }
    }.also(options::add)

    val bookshelf = object : OptionSolver() {
        override suspend fun solve() {
            bookshelfEntities.addAll(bookshelfDao.getAllBookshelves())
            bookshelfBookMetadataEntities.addAll(bookshelfDao.getAllBookshelfBookEntities())
        }
    }.also(options::add)

    val readingRecord = object : OptionSolver() {
        override suspend fun solve() {
            bookRecordEntities.addAll(bookRecordDao.getAllBookRecords())
            dailyCountEntities.addAll(dailyCountDao.getAll())
            userReadingDataEntities.addAll(userReadingDataDao.getAll())
            userDataEntities.addAll(userDataDao.getAllEntities().filter {
                webDataSourceUserDataPathSet.contains(it.path)
            }
            )
        }
    }.also(options::add)

    val settings = object : OptionSolver() {
        override suspend fun solve() {
            formattingRuleEntities.addAll(formattingRuleDao.getAllBookRuleEntity())
        }
    }.also(options::add)

    suspend fun solve() {
        for (solver in options) {
            if (solver.enable)
                solver.solve()
        }
    }
}