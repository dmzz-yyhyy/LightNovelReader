package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import io.nightfish.lightnovelreader.api.identifier.Identifier

data class VolumeStorageRow(
    val bookId: Identifier.Book,
    val chapterIdList: String,
    val bytes: Long
)
