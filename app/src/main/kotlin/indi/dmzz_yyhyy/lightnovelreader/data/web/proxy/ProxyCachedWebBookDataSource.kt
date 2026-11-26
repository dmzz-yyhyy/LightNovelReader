package indi.dmzz_yyhyy.lightnovelreader.data.web.proxy

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onOk
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority

class ProxyCachedWebBookDataSource(
    override val proxiedWebBookDataSource: ProxyWebBookDataSource
) : ProxyWebBookDataSource {
    private inline fun <reified T : Any> getOrCache(
        key: String,
        block: () -> Result<T, WebRequestError>
    ): Result<T, WebRequestError> {
        val value = origin.cache?.getCache<T>(key.hashCode()) ?: return block.invoke()
            .onOk {
                origin.cache?.cache(key.hashCode(), it)
            }
        return Ok(value)
    }

    override suspend fun getBookInformation(id: Identifier.Book, priority: WebDataSourcePriority) =
        getOrCache("book-information:$id") {
            proxiedWebBookDataSource.getBookInformation(id, priority)
        }

    override suspend fun getBookVolumes(id: Identifier.Book, priority: WebDataSourcePriority) =
        getOrCache("book-volumes:$id") {
            proxiedWebBookDataSource.getBookVolumes(id, priority)
        }

    override suspend fun getChapterContent(
        chapterId: Identifier.Chapter,
        bookId: Identifier.Book,
        priority: WebDataSourcePriority
    ) = getOrCache("$chapterId:$bookId") {
        proxiedWebBookDataSource.getChapterContent(chapterId, bookId, priority)
    }
}
