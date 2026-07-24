package io.nightfish.lightnovelreader.api.book

/**
 * 章节基础信息
 *
 * @param id 章节id
 * @param title 章节标题
 *
 * @since Api 4
 */
data class ChapterInformation(
    val id: String,
    val title: String
)