package io.nightfish.lightnovelreader.api.bookshelf

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
 * @since Api 2
 */
@Stable
interface Bookshelf {
    val id: Int
    val name: String
    val sortType: BookshelfSortType
    val sortReversed: Boolean
    val autoCache: Boolean
    val systemUpdateReminder: Boolean
    val allBookIds: List<String>
    val pinnedBookIds: List<String>
    val updatedBookIds: List<String>
    /**
     * 判断书架是否为空
     * id为-1时判断为空
     *
     * @return 书架是否为空
     *
     * @since Api 2
     */
    fun isEmpty() = this.id == -1
}

/**
 * 可变的书架对象
 * 其中每一个成员都是可被UI观测的
 *
 * @property id 书架id，默认为-1表示为空
 * @property name 书架名称
 * @property sortType 书架排序方式
 * @property sortReversed 是否反向排序
 * @property autoCache 是否开启自动缓存
 * @property systemUpdateReminder 是否通过系统通知提醒更新
 * @property allBookIds 书架中所有书本的id列表
 * @property pinnedBookIds 置顶的书本的id列表
 * @property updatedBookIds 有新章节更新的书本的id列表
 *
 * @since Api 2
 */
class MutableBookshelf : Bookshelf {
    override var id by mutableIntStateOf(-1)
    override var name by mutableStateOf("")
    override var sortType by mutableStateOf(BookshelfSortType.Default)
    override var sortReversed by mutableStateOf(false)
    override var autoCache by mutableStateOf(false)
    override var systemUpdateReminder by mutableStateOf(false)
    override var allBookIds by mutableStateOf<List<String>>(listOf())
    override var pinnedBookIds by mutableStateOf<List<String>>(listOf())
    override var updatedBookIds by mutableStateOf<List<String>>(listOf())
}
