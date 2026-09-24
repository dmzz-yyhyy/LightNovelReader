package io.nightfish.lightnovelreader.api.web.search

import kotlinx.coroutines.flow.Flow

/**
 * 搜索功能提供器接口
 * 实现此接口以支持搜索功能及搜索建议
 *
 * @since Api 2
 */
interface SearchProvider {
    /**
     * 搜索类型列表
     *
     * @since Api 2
     */
    val searchTypes: List<SearchType>
    /**
     * 执行搜索任务
     *
     * 应当返回搜索结果的数据流
     *
     * @param searchType 搜索类别
     * @param keyword 搜索关键词
     * @return 搜索结果的数据流
     *
     * @since Api 2
     */
    fun search(searchType: SearchType, keyword: String): Flow<SearchResult>

    /**
     * 用于提供搜索建议
     * 会在输入的关键词发生变化时调用
     *
     * @param history 用户的搜索历史记录, 按照从早到晚的顺序排序
     * @param keyword 当前输入的搜索关键词
     * @return 所有搜索建议
     *
     * @since Api 2
     */
    fun getSearchSuggestions(history: List<String>, keyword: String): List<String> =
        history.filter { it.startsWith(keyword) }
}