package io.nightfish.lightnovelreader.api.web.explore

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.web.BookPageResult
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter

/** 由宿主渲染并按需加载的探索展开页。 */
interface ExploreExpandedPageDataSource : ExploreExpandedPage {
    /** 按展示顺序提供的数据源和本地筛选器。 */
    val filters: List<Filter<*>>
        get() = emptyList()

    /**
     * 加载一页。同一轮分页使用同一份数据源筛选值。
     *
     * @param cursor 上一页返回的游标，首页传入 null
     * @param filters 数据源筛选值
     * @return 书目和下一页游标，或该页的请求错误
     */
    suspend fun loadPage(cursor: String?, filters: OptionValues): Result<BookPageResult.Page, WebRequestError>
}
