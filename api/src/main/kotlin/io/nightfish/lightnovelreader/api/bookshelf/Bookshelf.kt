package io.nightfish.lightnovelreader.api.bookshelf

import androidx.compose.runtime.Stable
import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 书架接口
 *
 * @property id 书架id
 * @property name 书架名称
 * @property sortType 书架排序方式
 * @property sortReversed 是否反向排序
 * @property autoCache 是否开启自动缓存
 * @property systemUpdateReminder 是否通过系统通知提醒更新
 * @property allBookIds 书架中所有书本的id列表
 * @property pinnedBookIds 置顶的书本的id列表
 * @property updatedBookIds 有新章节更新的书本的id列表
 *
 * @since Api 4
 */
@Stable
data class Bookshelf(
    val id: Identifier.Bookshelf,
    val name: String = "",
    val sortType: BookshelfSortType = BookshelfSortType.Default,
    val sortReversed: Boolean = false,
    val autoCache: Boolean = false,
    val systemUpdateReminder: Boolean = false,
    val allBookIds: List<Identifier.Book> = emptyList(),
    val pinnedBookIds: List<Identifier.Book> = emptyList(),
    val updatedBookIds: List<Identifier.Book> = emptyList()
)
