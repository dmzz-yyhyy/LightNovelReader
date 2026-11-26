package io.nightfish.lightnovelreader.api.book

import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 书本卷目录
 *
 * @param bookId 书本id
 * @param volumes 卷列表
 *
 * @since Api 4
 */
data class BookVolumes(
    val bookId: Identifier.Book,
    val volumes: List<Volume>
) {
    /**
     * 将目录内所有的章节信息铺开
     *
     * @since Api 4
     */
    val allChapters = buildList {
        volumes.forEach { volume ->
            volume.chapters.forEach {
                add(it)
            }
        }
    }
}
