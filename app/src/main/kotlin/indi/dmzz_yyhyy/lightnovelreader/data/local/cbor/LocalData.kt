package indi.dmzz_yyhyy.lightnovelreader.data.local.cbor

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
import kotlinx.serialization.Serializable

@Serializable
data class LocalData(
    val webBookDataSourceId: Int,
    val bookInformationEntities: List<BookInformationEntity>,
    val bookRecordEntities: List<BookRecordEntity>,
    val bookshelfEntities: List<BookshelfEntity>,
    val bookshelfBookMetadataEntities: List<BookshelfBookMetadataEntity>,
    val chapterContentEntities: List<ChapterContentEntity>,
    val chapterInformationEntities: List<ChapterInformationEntity>,
    val formattingRuleEntities: List<FormattingRuleEntity>,
    val readingStatisticsEntities: List<ReadingStatisticsEntity>,
    val userDataEntities: List<UserDataEntity>,
    val userReadingDataEntities: List<UserReadingDataEntity>,
    val volumeEntities: List<VolumeEntity>
) {
    companion object {
        fun empty() = LocalData(
            webBookDataSourceId = -1,
            bookInformationEntities = emptyList(),
            bookRecordEntities = emptyList(),
            bookshelfEntities = emptyList(),
            bookshelfBookMetadataEntities = emptyList(),
            chapterContentEntities = emptyList(),
            chapterInformationEntities = emptyList(),
            formattingRuleEntities = emptyList(),
            readingStatisticsEntities = emptyList(),
            userDataEntities = emptyList(),
            userReadingDataEntities = emptyList(),
            volumeEntities = emptyList()
        )
    }
}
