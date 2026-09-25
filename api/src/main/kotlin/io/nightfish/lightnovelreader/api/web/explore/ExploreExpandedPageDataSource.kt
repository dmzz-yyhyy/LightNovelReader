package io.nightfish.lightnovelreader.api.web.explore

import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.flow.Flow

/**
 * 探索展开页的数据源接口
 * 展开页支持过滤器、切换不同内容以及分页加载
 *
 * @since Api 2
 */
interface ExploreExpandedPageDataSource {
    /**
     * 展开页的显示标题
     *
     * @since Api 2
     */
    val title: String

    /**
     * 展开页支持的过滤器列表
     *
     * @since Api 2
     */
    val filters: List<Filter<*>>

    /**
     * 加载更多搜索结果
     * 只作用于[getResultFlow]的当前收集轮次，未消费的请求不能带入下一轮。
     *
     * @since Api 2
     */
    fun loadMore()

    /**
     * 探索展开页的数据流，提供[SearchResult]
     * 返回可重新收集的冷流，每轮从第一页开始并保留已选筛选。
     * 同一数据源最多有一个有效收集者，宿主在重收集前取消并等待旧轮次结束。
     * 取消必须停止当前轮次的网络请求和分页等待，不能在后台继续生产结果。
     * 多本结果逐条发送[SearchResult.MultipleBook]，完成后发送[SearchResult.End]或正常结束。
     *
     * @return 探索展开页的数据流
     *
     * @since Api 2
     */
    fun getResultFlow(): Flow<SearchResult>
}
