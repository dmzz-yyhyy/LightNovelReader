package io.nightfish.lightnovelreader.api.web.search

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.util.LocalString
import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.BookPageResult
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter

/** 提供按需分页搜索、设置和结果筛选。 */
interface SearchProvider {
    /** 按展示顺序提供的搜索设置。 */
    val settings: List<SearchSetting<*>>
        get() = emptyList()

    /** 按展示顺序提供的结果筛选器。 */
    val filters: List<Filter<*>>
        get() = emptyList()

    /**
     * 获取当前设置对应的搜索提示。
     *
     * @param settings 当前搜索设置
     */
    fun getSearchTip(settings: OptionValues): LocalString = "".local()

    /**
     * 请求一页搜索结果。只有首页可声明书籍直达结果。
     *
     * @param keyword 搜索关键词
     * @param settings 本轮搜索设置
     * @param filters 本轮数据源筛选值
     * @param cursor 上一页返回的游标，首页传入 null
     */
    suspend fun loadPage(
        keyword: String,
        settings: OptionValues,
        filters: OptionValues,
        cursor: String?
    ): Result<BookPageResult, WebRequestError>

    /**
     * 生成搜索建议。
     *
     * @param history 搜索历史
     * @param keyword 尚未提交的输入
     */
    fun getSearchSuggestions(history: List<String>, keyword: String): List<String> = emptyList()
}
