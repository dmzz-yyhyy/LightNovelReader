package io.nightfish.lightnovelreader.api.book

import androidx.navigation.NavController
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.flow.Flow

/**
 * 书本相关的Api
 *
 * @since Api 2
 */
interface BookRepositoryApi {
    /**
     * 获取书本详情的流
     * 流遵照先本地后远程的顺序发射数据
     *
     * @param id 需要获取的书本id
     * @param priority 此请求的优先级, 后端可能会依据优先级处理网络请求
     *
     * @return 数据流, 如果存在本地数据则先emit一次本地数据, 之后emit远程数据; 如果远程无法访问, 但本地数据存在则只会有本地的一次emit; 如果本地和远程都无法访问, 则只emit一次远程请求错误
     *
     * @since Api 4
     */
    fun getBookInformationFlow(
        id: String,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): Flow<Result<BookInformation, WebRequestError>>

    /**
     * 获取书本卷目录的流
     * 流遵照先本地后远程的顺序发射数据
     *
     * @param id 需要获取的书本id
     * @param priority 此请求的优先级, 后端可能会依据优先级处理网络请求
     *
     * @return 数据流, 如果存在本地数据则先emit一次本地数据, 之后emit远程数据; 如果远程无法访问, 但本地数据存在则只会有本地的一次emit; 如果本地和远程都无法访问, 则只emit一次远程请求错误
     *
     * @since Api 4
     */
    fun getBookVolumesFlow(
        id: String,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): Flow<Result<BookVolumes, WebRequestError>>

    /**
     * 获取章节内容的流
     * 流遵照先本地后远程的顺序发射数据
     *
     * @param chapterId 需要获取的章节id
     * @param bookId 需要获取章节所属的书本id
     * @param priority 此请求的优先级, 后端可能会依据优先级处理网络请求
     *
     * @return 数据流, 如果存在本地数据则先emit一次本地数据, 之后emit远程数据; 如果远程无法访问, 但本地数据存在则只会有本地的一次emit; 如果本地和远程都无法访问, 则只emit一次远程请求错误
     *
     * @since Api 4
     */
    fun getChapterContentFlow(
        chapterId: String,
        bookId: String,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): Flow<Result<ChapterContent, WebRequestError>>

    /**
     * 预加载章节内容
     * 从网络请求内容加载到本地数据库内
     *
     * @param chapterId 需要获取的章节id
     * @param bookId 需要获取章节所属的书本id
     * @param priority 此请求的优先级, 后端可能会依据优先级处理网络请求
     *
     * @since Api 4
     */
    suspend fun preloadChapterContent(
        chapterId: String,
        bookId: String,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    )



    /**
     * 获取阅读数据
     *
     * @param bookId 请求的书本阅读数据所属的书本id
     *
     * @return 书本阅读数据
     *
     * @since Api 2
     */
    suspend fun getUserReadingData(bookId: String): UserReadingData

    /**
     * 获取阅读数据的流
     *
     * @param bookId 请求的书本阅读数据所属的书本id
     *
     * @return [UserReadingData]对象的流
     *
     * @return 数据流, 如果存在本地数据则先emit一次本地数据, 之后emit远程数据; 如果远程无法访问, 但本地数据存在则只会有本地的一次emit; 如果本地和远程都无法访问, 则只emit一次远程请求错误
     *
     * @since Api 2
     */
    fun getUserReadingDataFlow(bookId: String): Flow<UserReadingData>

    /**
     * 获取全部存在的书本阅读数据
     *
     * @return 书本阅读数据的列表
     *
     * @since Api 2
     */
    suspend fun getAllUserReadingData(): List<UserReadingData>

    /**
     * 更新用户书本阅读数据
     *
     * @param id 需要更新的书本id
     *
     * @sample io.nightfish.lightnovelreader.api.sample.updateUserReadingData
     *
     * @since Api 2
     */
    suspend fun updateUserReadingData(id: String, update: (UserReadingData) -> UserReadingData)

    /**
     * 获取书本是缓存状态
     *
     * @param bookId 查询的书本id
     *
     * @return 是否缓存
     *
     * @since Api 2
     */
    suspend fun getIsBookCached(bookId: String): Boolean

    /**
     * 将书本标签点击事件交于数据源处处理
     *
     * @param tag 书本标签名称
     * @param navController 导航控制器
     *
     */
    fun progressBookTagClick(tag: String, navController: NavController)
}