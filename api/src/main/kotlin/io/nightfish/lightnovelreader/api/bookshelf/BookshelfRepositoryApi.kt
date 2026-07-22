package io.nightfish.lightnovelreader.api.bookshelf

import io.nightfish.lightnovelreader.api.book.BookInformation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * 书架相关的Api
 *
 * @since Api 2
 */
interface BookshelfRepositoryApi {
    /**
     * 获取所有书架的id列表
     *
     * @return 书架id列表
     *
     * @since Api 4
     */
    suspend fun getAllBookshelfIds(): List<Int>

    /**
     * 删除指定书架
     *
     * @param bookshelfId 需要删除的书架id
     *
     * @since Api 4
     */
    suspend fun deleteBookshelf(bookshelfId: Int)

    /**
     * 将书本添加到指定书架
     *
     * @param bookshelfId 目标书架id
     * @param bookInformation 需要添加的书本详情
     *
     * @since Api 4
     */
    suspend fun addBookIntoBookShelf(bookshelfId: Int, bookInformation: BookInformation)

    /**
     * 将书本标记为书架中已更新的书本
     *
     * @param bookShelfId 目标书架id
     * @param bookId 需要标记的书本id
     *
     * @since Api 4
     */
    suspend fun addUpdatedBooksIntoBookShelf(bookShelfId: Int, bookId: String)

    /**
     * 获取所有书架中书本id的流
     *
     * @return 所有书架书本id列表的流
     *
     * @since Api 2
     */
    fun getAllBookshelfBookIdsFlow(): Flow<List<String>>

    /**
     * 从指定书架中删除书本
     *
     * @param bookshelfId 目标书架id
     * @param bookId 需要移除的书本id
     *
     * @since Api 4
     */
    suspend fun deleteBookFromBookshelf(bookshelfId: Int, bookId: String)

    /**
     * 从指定书架的已更新列表中移除书本
     *
     * @param bookshelfId 目标书架id
     * @param bookId 需要移除的书本id
     *
     * @since Api 4
     */
    suspend fun deleteBookFromBookshelfUpdatedBookIds(bookshelfId: Int, bookId: String)

    /**
     * 更新书架书本元数据的最后更新时间
     *
     * @param bookId 书本id
     * @param time 新的最后更新时间
     *
     * @since Api 4
     */
    suspend fun updateBookshelfBookMetadataLastUpdateTime(bookId: String, time: LocalDateTime)

    /**
     * 清空所有书架数据
     *
     * @since Api 4
     */
    suspend fun clear()

    /**
     * 获取所有书架对象的流
     *
     * @return [Bookshelf]列表的流
     *
     * @since Api 2
     */
    fun getAllBookshelvesFlow(): Flow<List<Bookshelf>>

    /**
     * 获取所有书架对象列表
     *
     * @return [Bookshelf]列表
     *
     * @since Api 4
     */
    suspend fun getAllBookshelves(): List<Bookshelf>

    /**
     * 通过id获取单个书架对象
     *
     * @param id 书架id
     *
     * @return 书架对象, 如果不存在则返回null
     *
     * @since Api 4
     */
    suspend fun getBookshelf(id: Int): Bookshelf?

    /**
     * 获取单个书架对象的流
     *
     * @param id 书架id
     *
     * @return [Bookshelf]对象的流, 如果不存在则发射null
     *
     * @since Api 2
     */
    fun getBookshelfFlow(id: Int): Flow<Bookshelf?>

    /**
     * 添加新的书架实例
     *
     * @param bookshelf 书架实例
     *
     * @since Api 4
     */
    suspend fun addBookshelf(bookshelf: Bookshelf)

    /**
     * 更新书架信息
     * 如果书架不存在则不执行任何操作
     *
     * @param bookshelfId 需要更新的书架id
     * @param updater 接收当前书架对象并返回更新后书架对象的函数
     *
     * @since Api 4
     */
    suspend fun updateBookshelf(bookshelfId: Int, updater: (Bookshelf) -> Bookshelf)

    /**
     * 获取所有书架书本的元数据列表
     *
     * @return [BookshelfBookMetadata]列表
     *
     * @since Api 4
     */
    suspend fun getAllBookshelfBooksMetadata(): List<BookshelfBookMetadata>

    /**
     * 通过书本id获取书架书本元数据
     *
     * @param id 书本id
     *
     * @return 书架书本元数据, 如果不存在则返回null
     *
     * @since Api 4
     */
    suspend fun getBookshelfBookMetadata(id: String): BookshelfBookMetadata?

    /**
     * 获取书架书本元数据的流
     *
     * @param id 书本id
     *
     * @return [BookshelfBookMetadata]对象的流, 如果不存在则发射null
     *
     * @since Api 2
     */
    fun getBookshelfBookMetadataFlow(id: String): Flow<BookshelfBookMetadata?>
}