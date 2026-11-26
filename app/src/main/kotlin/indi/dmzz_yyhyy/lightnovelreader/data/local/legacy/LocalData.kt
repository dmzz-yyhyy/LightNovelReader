package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class LocalData(
    @Serializable(LegacySourceIdSerializer::class)
    val webBookDataSourceId: LegacySourceId?,
    val bookInformationEntities: List<BookInformationEntity>,
    val bookRecordEntities: List<BookRecordEntity>,
    val dailyCountEntities: List<DailyCountEntity>,
    val bookshelfEntities: List<BookshelfEntity>,
    val bookshelfBookMetadataEntities: List<BookshelfBookMetadataEntity>,
    val chapterContentEntities: List<ChapterContentEntity>,
    val chapterInformationEntities: List<ChapterInformationEntity>,
    val formattingRuleEntities: List<FormattingRuleEntity>,
    val userDataEntities: List<UserDataEntity>,
    val userReadingDataEntities: List<UserReadingDataEntity>,
    val volumeEntities: List<VolumeEntity>
) {
    companion object {
        fun empty() = LocalData(
            webBookDataSourceId = null,
            bookInformationEntities = emptyList(),
            bookRecordEntities = emptyList(),
            dailyCountEntities = emptyList(),
            bookshelfEntities = emptyList(),
            bookshelfBookMetadataEntities = emptyList(),
            chapterContentEntities = emptyList(),
            chapterInformationEntities = emptyList(),
            formattingRuleEntities = emptyList(),
            userDataEntities = emptyList(),
            userReadingDataEntities = emptyList(),
            volumeEntities = emptyList()
        )
    }
}
