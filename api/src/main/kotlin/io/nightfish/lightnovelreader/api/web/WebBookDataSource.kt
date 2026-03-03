package io.nightfish.lightnovelreader.api.web

import android.content.Context
import android.net.Uri
import androidx.navigation.NavController
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import kotlinx.coroutines.flow.StateFlow

/**
 * LightNovelReader 的网络数据提供源接口，可以通过实现此接口使软件支持新的数据源
 * 软件加载WebBookDataSource时会对构造器进行接依赖注入
 * 版本: 0.7.0
 */
interface WebBookDataSource {
    /**
     * 最大协程并发量
     */
    val permits: Int get() = 64

    /**
     * 数据源的缓存对象
     * 用于指定最大缓存数目与过期时间
     * 如为null则不设缓存
     */
    val cache: Cache? get() = null
    val id: Int

    /**
     * 当数据源被加载时调用
     */
    fun onLoad() {}

    /**
     * 获取当前软件整体是否处于离线状态
     */
    suspend fun isOffLine(): Boolean

    /**
     * 当前软件整体是否处于离线状态
     */
    val offLine: Boolean

    /**
     * 获取当前软件整体是否处于离线状态的数据流
     * 此数据流应当为热数据流, 并且不断对状态进行更新
     */
    val isOffLineFlow: StateFlow<Boolean>

    /**
     * 搜索提供器
     */
    val searchProvider: SearchProvider

    /**
     * 探索页面内容提供器
     */
    val explorePageProvider: ExplorePageProvider

    /***
     * 请求图片时的Header
     */
    val imageHeader: Map<String, String>
        get() = emptyMap()

    /**
     * 此函数无需保证主线程安全性, 为阻塞函数, 获取到数据前应当保持阻塞
     * 此函数应当自行实现断线重连等逻辑
     *
     * @param id 书本id
     * @return 经过格式化后的书本元数据, getBookInformation.empty()
     */
    suspend fun getBookInformation(id: String): BookInformation

    /**
     * 此函数无需保证主线程安全性, 为阻塞函数, 获取到数据前应当保持阻塞
     * 此函数应当自行实现断线重连等逻辑
     *
     * @param id 书本id
     * @return 经过格式化后的书本章节目录数据, 如未找到改书则返回BookVolumes.empty
     */
    suspend fun getBookVolumes(id: String): BookVolumes

    /**
     * 此函数无需保证主线程安全性, 为阻塞函数, 获取到数据前应当保持阻塞
     * 此函数应当自行实现断线重连等逻辑
     *
     * @param chapterId 章节id
     * @param bookId 章节所属书本id
     * @return 经过格式化后的书本章节类容录数据, 如未找到改书则返回ChapterContent.empty()
     */
    suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent

    /**
     * 用于处理书本tag的点击跳转事件
     */
    fun progressBookTagClick(tag: String, navController: NavController) {  }

    /**
     * 根据卷获取该卷封面的Uri, 用于EPUB分卷导出
     * 如无, 则返回null
     *
     * @param bookId 书本id
     * @param volume 需要搜索封面的卷id
     * @param volumeChapterContentMap 包含搜索卷全部章节的Map, 以章节id为key
     */
    suspend fun getCoverUriInVolume(
        bookId: String,
        volume: Volume,
        volumeChapterContentMap: MutableMap<String, ChapterContent>,
        context: Context
    ): Uri? = null
}