package io.nightfish.lightnovelreader.api.web

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 分页结果中的书目数据类
 * 宿主使用该数据展示书目并执行本地过滤
 *
 * @property id 书本的唯一标识
 * @property information 书本信息或获取该书本信息时发生的错误
 *
 * @since Api 4
 */
data class PageBook(
    val id: Identifier.Book,
    val information: Result<BookInformation, WebRequestError>
)

/**
 * 探索展开页和搜索的分页结果接口
 * 搜索首页也可以返回[OpenBook]以直接打开书本详情
 *
 * @since Api 4
 */
sealed interface BookPageResult {
    /**
     * 表示一次分页请求返回的书目
     *
     * @property books 按数据源顺序排列的书目
     * @property nextCursor 下一页的游标，为 `null` 时表示没有后续页
     *
     * @since Api 4
     */
    data class Page(val books: List<PageBook>, val nextCursor: String?) : BookPageResult

    /**
     * 表示搜索结果应直接打开书本详情
     * 仅可在请求搜索首页时返回该结果
     *
     * @property bookId 要打开的书本标识
     *
     * @since Api 4
     */
    data class OpenBook(val bookId: Identifier.Book) : BookPageResult
}
