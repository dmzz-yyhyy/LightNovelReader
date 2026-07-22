package io.nightfish.lightnovelreader.api.bookshelf

import androidx.compose.runtime.Stable
import kotlin.time.Clock

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
    val id: Int,
    val name: String,
    val sortType: BookshelfSortType,
    val sortReversed: Boolean,
    val autoCache: Boolean,
    val systemUpdateReminder: Boolean,
    val allBookIds: List<String> = emptyList(),
    val pinnedBookIds: List<String> = emptyList(),
    val updatedBookIds: List<String> = emptyList()
) {
    companion object {
        fun create() =
            Bookshelf(
                id = Clock.System.now().epochSeconds.hashCode(),
                name = "",
                sortType = BookshelfSortType.Default,
                sortReversed = false,
                autoCache = false,
                systemUpdateReminder = false,
                allBookIds = emptyList(),
                pinnedBookIds = emptyList(),
                updatedBookIds = emptyList()
            )
    }
}