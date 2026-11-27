package io.nightfish.lightnovelreader.api.web

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 可直接渲染和筛选的书目。
 *
 * @property id 稳定的书本标识
 * @property information 列表所需的书目信息或单条信息错误
 */
data class PageBook(val id: Identifier.Book, val information: Result<BookInformation, WebRequestError>)

/** 一次加载的成功结果。 */
sealed interface BookPageResult {
    /**
     * 一页书目。
     *
     * @property books 按数据源顺序排列的书目
     * @property nextCursor 下一页游标，末页为 null
     */
    data class Page(val books: List<PageBook>, val nextCursor: String?) : BookPageResult

    /**
     * 搜索数据源在首页明确返回的书籍直达。
     *
     * @property bookId 宿主应直接打开的书本
     */
    data class OpenBook(val bookId: Identifier.Book) : BookPageResult
}
