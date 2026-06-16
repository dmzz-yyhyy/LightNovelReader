package io.nightfish.lightnovelreader.api.web

import android.content.Context
import android.net.Uri
import androidx.navigation.NavController
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import kotlinx.coroutines.flow.StateFlow

/**
 * LightNovelReader 的网络数据提供源接口
 * 可以通过实现此接口使软件支持新的数据源
 * 软件加载WebBookDataSource时会对构造器进行依赖注入
 *
 * @since Api 2
 */
interface WebBookDataSource {
    /**
     * 最大协程并发量
     *
     * @since Api 2
     */
    val permits: Int get() = 64

    /**
     * 数据源的缓存对象
     * 用于指定最大缓存条数与过期时间
     * 如为null则不设缓存
     *
     * @since Api 2
     */
    val cache: Cache? get() = null

    /**
     * 数据源的唯一整数标识
     *
     * @since Api 4
     */
    val id: Identifier

    /**
     * 当数据源被加载时调用
     *
     * @since Api 2
     */
    fun onLoad() {}

    /**
     * 获取当前软件整体是否处于离线状态
     *
     * @return 是否处于离线状态
     *
     * @since Api 2
     */
    suspend fun isOffLine(): Boolean

    /**
     * 当前软件整体是否处于离线状态
     *
     * @since Api 2
     */
    val offLine: Boolean

    /**
     * 获取当前软件整体是否处于离线状态的数据流
     * 此数据流应当为热数据流, 并且不断对状态进行更新
     *
     * @since Api 2
     */
    val isOffLineFlow: StateFlow<Boolean>

    /**
     * 搜索提供器
     *
     * @since Api 2
     */
    val searchProvider: SearchProvider

    /**
     * 探索页面内容提供器
     *
     * @since Api 2
     */
    val explorePageProvider: ExplorePageProvider

    /**
     * 请求图片时附带的请求头Map
     *
     * @since Api 2
     */
    val imageHeader: Map<String, String>
        get() = emptyMap()

    /**
     * 获取书本详情
     * 此函数无需保证主线程安全性, 为阻塞函数, 获取到数据前应当保持阻塞
     * 此函数应当自行实现断线重连等逻辑
     *
     * @param id 书本id
     *
     * @return 经过格式化后的书本详情
     *
     * @since Api 2
     */
    suspend fun getBookInformation(id: String): BookInformation

    /**
     * 获取书本卷目录
     * 此函数无需保证主线程安全性, 为阻塞函数, 获取到数据前应当保持阻塞
     * 此函数应当自行实现断线重连等逻辑
     *
     * @param id 书本id
     *
     * @return 经过格式化后的书本卷目录数据, 如未找到该书则返回BookVolumes.empty
     *
     * @since Api 2
     */
    suspend fun getBookVolumes(id: String): BookVolumes

    /**
     * 获取章节内容
     * 此函数无需保证主线程安全性, 为阻塞函数, 获取到数据前应当保持阻塞
     * 此函数应当自行实现断线重连等逻辑
     *
     * @param chapterId 章节id
     * @param bookId 章节所属书本id
     *
     * @return 经过格式化后的章节内容, 如未找到则返回ChapterContent.empty()
     *
     * @since Api 2
     */
    suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent

    /**
     * 用于处理书本tag的点击跳转事件
     *
     * @param tag 被点击的tag内容
     * @param navController 导航控制器
     *
     * @since Api 2
     */
    fun progressBookTagClick(tag: String, navController: NavController) {  }

    /**
     * 根据卷获取该卷封面的Uri, 用于EPUB分卷导出
     * 如无则返回null
     *
     * @param bookId 书本id
     * @param volume 需要搜索封面的卷对象
     * @param volumeChapterContentMap 包含搜索卷全部章节的Map, 以章节id为key
     * @param context Android上下文
     *
     * @return 封面图片的[Uri], 如无则返回null
     *
     * @since Api 2
     */
    suspend fun getCoverUriInVolume(
        bookId: String,
        volume: Volume,
        volumeChapterContentMap: MutableMap<String, ChapterContent>,
        context: Context
    ): Uri? = null
}