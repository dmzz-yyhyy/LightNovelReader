package io.nightfish.lightnovelreader.api.web.search

import androidx.compose.runtime.Composable
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.web.BookPageResult
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter

/**
 * 搜索功能提供器接口
 * 实现此接口以支持分页搜索、搜索设置、结果过滤和搜索建议
 *
 * @since Api 2
 */
interface SearchProvider {
    /**
     * 搜索设置列表
     * 列表顺序决定设置的显示顺序
     *
     * @since Api 4
     */
    val settings: List<SearchSetting<*>>
        get() = emptyList()

    /**
     * 搜索结果页支持的过滤器列表
     * 列表顺序决定过滤器的显示顺序
     *
     * @since Api 4
     */
    val filters: List<Filter<*>>
        get() = emptyList()

    /**
     * 获取当前搜索设置对应的输入提示
     *
     * @param settings 当前搜索设置的选项快照
     * @return 用于显示在搜索输入框中的本地化提示文字，返回空字符串时使用宿主的默认提示
     *
     * @since Api 4
     */
    @Composable
    fun getSearchTip(settings: OptionValues): String = ""

    /**
     * 加载一页搜索结果
     * 请求首页时，可返回[BookPageResult.OpenBook]以直接打开书本详情
     *
     * @param keyword 搜索关键词
     * @param settings 本轮搜索使用的搜索设置快照
     * @param filters 本轮搜索使用的数据源过滤器选项快照
     * @param cursor 上一页返回的游标，请求首页时为 `null`
     * @return 包含[BookPageResult]或[WebRequestError]的结果
     *
     * @since Api 4
     */
    suspend fun loadPage(
        keyword: String,
        settings: OptionValues,
        filters: OptionValues,
        cursor: String?
    ): Result<BookPageResult, WebRequestError>

    /**
     * 用于提供搜索建议
     * 会在输入的关键词发生变化时调用
     *
     * @param history 用户的搜索历史记录
     * @param keyword 当前输入的搜索关键词
     * @return 所有搜索建议
     *
     * @since Api 2
     */
    fun getSearchSuggestions(history: List<String>, keyword: String): List<String> = emptyList()
}
