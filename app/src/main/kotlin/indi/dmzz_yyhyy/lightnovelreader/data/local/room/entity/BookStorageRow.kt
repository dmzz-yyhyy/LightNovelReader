package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import io.nightfish.lightnovelreader.api.identifier.Identifier

data class BookStorageRow(
    val id: Identifier.Book,
    val bytes: Long
)
