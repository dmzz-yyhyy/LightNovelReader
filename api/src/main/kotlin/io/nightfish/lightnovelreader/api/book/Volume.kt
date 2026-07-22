package io.nightfish.lightnovelreader.api.book

/**
 * 书本卷信息
 *
 * @param volumeId 卷id
 * @param volumeTitle 卷标题
 * @param chapters 该卷包含的章节列表
 *
 * @since Api 2
 */
data class Volume(
    val volumeId: String,
    val volumeTitle: String,
    val chapters: List<ChapterInformation>,
)