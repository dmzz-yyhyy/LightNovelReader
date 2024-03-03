package indi.dmzz_yyhyy.lightnovelreader.data.`object`

import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.BookMetadata

data class Information(
        val id: Int,
        val name: String,
        val coverUrl: String,
        val writer: String,
        val type: String,
        val tags: List<String>,
        val introduction: String,
) {
    fun toBookMeatData() = BookMetadata(
        id = id,
        name = name,
        coverUrl = coverUrl,
        writer = writer,
        type = type,
        tags = tags,
        introduction = introduction
    )
}