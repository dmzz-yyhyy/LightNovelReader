package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class BookInformationEntity(

    val id: String,
    val title: String,
    val subtitle: String,

    val coverUri: String,
    val author: String,
    val description: String,
    val tags: List<String>,
    val publishingHouse: String,
    val wordCount: LegacyWordCount,

    val lastUpdated: String,
    val isComplete: Boolean
)
