package io.nightfish.lightnovelreader.api.doc.sample

import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 更新书本用户阅读数据的示例函数
 *
 * @param bookRepositoryApi 书本仓库 API 实例
 *
 * @since Api 4
 */
suspend fun updateUserReadingData(bookRepositoryApi: BookRepositoryApi) {
    bookRepositoryApi.updateUserReadingData(Identifier.Book("example.source", "ciallo")) {
        it.copy(
            readingProgress = 1f
        )
    }
}
