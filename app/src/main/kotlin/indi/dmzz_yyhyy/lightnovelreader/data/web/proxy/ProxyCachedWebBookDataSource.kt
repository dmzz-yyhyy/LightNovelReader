package indi.dmzz_yyhyy.lightnovelreader.data.web.proxy

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onOk
import io.nightfish.lightnovelreader.api.error.WebRequestError
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
                origin.cache?.cache(id.hashCode(), it)
            }
        return Ok(value)
    }

    override suspend fun getBookInformation(id: String, priority: WebDataSourcePriority) = getOrCache(id) {
        proxiedWebBookDataSource.getBookInformation(id, priority)
    }

    override suspend fun getBookVolumes(id: String, priority: WebDataSourcePriority) = getOrCache(id) {
        proxiedWebBookDataSource.getBookVolumes(id, priority)
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String, priority: WebDataSourcePriority) = getOrCache(chapterId + bookId) {
        proxiedWebBookDataSource.getChapterContent(chapterId, bookId, priority)
    }
}
