package indi.dmzz_yyhyy.lightnovelreader.data.web.proxy

import com.santimattius.resilient.composition.ResilientScope
import com.santimattius.resilient.composition.resilient
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.Dispatchers

class ProxyCoalescingWebBookDataSource(
    override val proxiedWebBookDataSource: ProxyWebBookDataSource
) : ProxyWebBookDataSource {
    private val scope = ResilientScope(Dispatchers.IO)

    override suspend fun getBookInformation(id: String, priority: WebDataSourcePriority) = resilient(scope) {
        coalesce {
            key = id
        }
    }.execute {
        proxiedWebBookDataSource.getBookInformation(id, priority)
    }

    override suspend fun getBookVolumes(id: String, priority: WebDataSourcePriority) = resilient(scope) {
        coalesce {
            key = id
        }
    }.execute {
        proxiedWebBookDataSource.getBookVolumes(id, priority)
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String, priority: WebDataSourcePriority) = resilient(scope) {
        coalesce {
            key = chapterId + bookId
        }
    }.execute {
        proxiedWebBookDataSource.getChapterContent(chapterId, bookId, priority)
    }
}
