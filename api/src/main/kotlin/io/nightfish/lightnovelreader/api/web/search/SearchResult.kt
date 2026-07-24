package io.nightfish.lightnovelreader.api.web.search

/**
 * 搜索类数据流传输的密封类
 * 根据不同变体软件会有不同响应
 *
 * @since Api 2
 */
sealed class SearchResult {
    /**
     * 表示搜索结果仅有一本书
     * 返回该结果后软件会认为不存在后续结果
     * 这在搜索界面用于对单个结果直接跳转到书本页面
     *
     * @property bookId 唯一结果的书本id
     *
     * @since Api 2
     */
    class SingleBook(
        val bookId: String
    ): SearchResult()

    /**
     * 表示搜索结果仅有多本书
     * 这是其中的一本书
     * 软件会接收该结果
     *
     * @property bookId 唯一结果的书本id
     *
     * @since Api 4
     */
    class MultipleBook(
        val bookId: String
    ): SearchResult()


    /**
     * 表示搜索过程出现了错误
     * 返回该结果后软件会认为不存在后续结果
     *
     * @property error 错误对象
     *
     * @since Api 2
     */
    class Error(
        val error: Throwable
    ): SearchResult() {
        constructor(message: String): this(kotlin.Error(message))
    }

    /**
     * 表示单次搜索轮次已经结束
     * 返回该结果后软件会认为不存在后续结果
     *
     * @since Api 2
     */
    class End: SearchResult()

    /**
     * 表示无搜索结果
     *
     * @since Api 2
     */
    class Empty: SearchResult()
}