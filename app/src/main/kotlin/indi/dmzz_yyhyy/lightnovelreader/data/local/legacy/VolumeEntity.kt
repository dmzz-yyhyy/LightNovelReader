package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class VolumeEntity(
    val bookId: String,

    val volumeId: String,
    val volumeTitle: String,
    val chapterIds: List<String>,
    val index: Int
)
