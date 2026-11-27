package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import io.nightfish.lightnovelreader.api.identifier.Identifier

data class ChapterStorageRow(
    val id: Identifier.Chapter,
    val bytes: Long
)
