package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.utils.convertOldId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 一本书在本地 id 与远端 id 之间的映射结果。
 *
 * @property localBookId 本地数据库/界面使用的书 id
 * @property remoteBookId 数据源认识的书 id
 * @property sourceId 所属数据源
 * @property source 已包装的数据源
 */
data class ResolvedBook(
    val localBookId: String,
    val remoteBookId: String,
    val sourceId: Identifier,
    val source: ProxyWebBookDataSource
)

/**
 * 多数据源模式下按书籍解析它所属的数据源。
 *
 * 新数据的本地 id 形如 `数据源id|原始书id`; 旧数据没有前缀,
 * 通过书籍信息里记录的数据源归属兜底, 最终回退到当前主数据源。
 */
@Singleton
class BookSourceResolver @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localBookDataSource: LocalBookDataSource
) {
    suspend fun resolve(bookId: String?): ProxyWebBookDataSource = resolveBook(bookId ?: "").source

    suspend fun resolveBook(bookId: String): ResolvedBook {
        parseQualifiedBookId(bookId)?.let { parsed ->
            val source = webBookDataSourceProvider.get(parsed.sourceId.convertOldId())
            if (source != null) {
                return ResolvedBook(
                    localBookId = bookId,
                    remoteBookId = parsed.remoteBookId,
                    sourceId = source.id,
                    source = source
                )
            }
        }
        val fallback = if (bookId.isNotBlank()) {
            localBookDataSource.getBookDataSourceId(bookId)
                ?.takeIf { it.isNotBlank() }
                ?.convertOldId()
                ?.let { webBookDataSourceProvider.get(it) }
        } else null
        val source = fallback ?: webBookDataSourceProvider.value
        return ResolvedBook(
            localBookId = bookId,
            remoteBookId = bookId,
            sourceId = source.id,
            source = source
        )
    }
}
