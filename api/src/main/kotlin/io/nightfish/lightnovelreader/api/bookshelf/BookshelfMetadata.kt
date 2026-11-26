package io.nightfish.lightnovelreader.api.bookshelf

import io.nightfish.lightnovelreader.api.identifier.Identifier
import java.time.LocalDateTime

/**
 * 书架中书本的元数据
 *
 * @param id 书本id
 * @param lastUpdate 书本最后一次内容更新时间
 * @param bookShelfIds 该书本所属的书架id列表
 *
 * @since Api 2
 */
data class BookshelfBookMetadata(
    val id: Identifier.Book,
    val lastUpdate: LocalDateTime,
    val bookShelfIds: List<Identifier.Bookshelf>,
)

