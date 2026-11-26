package io.nightfish.lightnovelreader.api.book

import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 章节基础信息
 *
 * @param id 章节id
 * @param title 章节标题
 *
 * @since Api 4
 */
data class ChapterInformation(
    val id: Identifier.Chapter,
    val title: String
)
