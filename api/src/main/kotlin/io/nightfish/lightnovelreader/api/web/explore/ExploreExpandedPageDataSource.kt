package io.nightfish.lightnovelreader.api.web.explore

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.web.BookPageResult
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter

/**
 * 探索展开页的数据源接口
 * 宿主负责展示书目、应用过滤器和请求后续页
 *
 * @since Api 2
 */
interface ExploreExpandedPageDataSource : ExploreExpandedPage {
    /**
     * 展开页支持的过滤器列表
     * 列表顺序决定数据源过滤器和本地过滤器的显示顺序
     *
     * @since Api 2
     */
    val filters: List<Filter<*>>
        get() = emptyList()

    /**
     * 加载一页探索展开页的书目
     * 同一轮分页使用相同的数据源过滤条件
     *
     * @param cursor 上一页返回的游标，请求首页时为 `null`
     * @param filters 本轮分页使用的数据源过滤器选项快照
     * @return 包含[BookPageResult.Page]或[WebRequestError]的结果
     *
     * @since Api 4
     */
    suspend fun loadPage(
        cursor: String?,
        filters: OptionValues
    ): Result<BookPageResult.Page, WebRequestError>
}
