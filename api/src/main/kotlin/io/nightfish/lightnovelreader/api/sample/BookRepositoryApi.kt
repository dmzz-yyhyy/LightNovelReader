package io.nightfish.lightnovelreader.api.sample

import io.nightfish.lightnovelreader.api.book.BookRepositoryApi

/**
 * 更新书本用户阅读数据的示例函数
 *
 * @param bookRepositoryApi 书本仓库 API 实例
 *
 * @since Api 4
 */
suspend fun updateUserReadingData(bookRepositoryApi: BookRepositoryApi) {
    bookRepositoryApi.updateUserReadingData("ciallo") {
        it.copy(
            readingProgress = 1f
        )
    }
}