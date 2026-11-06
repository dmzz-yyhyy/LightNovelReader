package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.utils.convertOldId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 多数据源模式下按书籍解析它所属的数据源。
 *
 * 书籍的归属记录在本地书籍信息中; 旧数据没有归属信息时回退到当前主数据源。
 */
@Singleton
class BookSourceResolver @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localBookDataSource: LocalBookDataSource
) {
    suspend fun resolve(bookId: String?): ProxyWebBookDataSource {
        if (!bookId.isNullOrBlank()) {
            val storedSourceId = localBookDataSource.getBookDataSourceId(bookId)
            if (!storedSourceId.isNullOrBlank()) {
                webBookDataSourceProvider.get(storedSourceId.convertOldId())?.let { return it }
            }
        }
        return webBookDataSourceProvider.value
    }
}
