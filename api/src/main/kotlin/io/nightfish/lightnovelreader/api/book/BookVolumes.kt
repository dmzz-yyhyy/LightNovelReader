package io.nightfish.lightnovelreader.api.book

/**
 * 书本卷目录
 *
 * @param bookId 书本id
 * @param volumes 卷列表
 *
 * @since Api 4
 */
data class BookVolumes(
    val bookId: String,
    val volumes: List<Volume>
)