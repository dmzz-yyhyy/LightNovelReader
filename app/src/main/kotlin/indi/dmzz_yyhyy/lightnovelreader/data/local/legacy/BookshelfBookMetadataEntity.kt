package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class BookshelfBookMetadataEntity(

    val id: String,

    val lastUpdate: String,
    val bookShelfIds: List<Int>,
)
