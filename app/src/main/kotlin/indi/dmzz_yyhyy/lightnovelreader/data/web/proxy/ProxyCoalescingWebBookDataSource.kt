package indi.dmzz_yyhyy.lightnovelreader.data.web.proxy

import com.santimattius.resilient.composition.ResilientScope
import com.santimattius.resilient.composition.resilient
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.Dispatchers

class ProxyCoalescingWebBookDataSource(
    override val proxiedWebBookDataSource: ProxyWebBookDataSource
) : ProxyWebBookDataSource {
    private val scope = ResilientScope(Dispatchers.IO)

    override suspend fun getBookInformation(id: Identifier.Book, priority: WebDataSourcePriority) =
        resilient(scope) {
            coalesce {
                key = "book-information:$id"
            }
        }.execute {
            proxiedWebBookDataSource.getBookInformation(id, priority)
        }

    override suspend fun getBookVolumes(id: Identifier.Book, priority: WebDataSourcePriority) =
        resilient(scope) {
            coalesce {
                key = "book-volumes:$id"
            }
        }.execute {
            proxiedWebBookDataSource.getBookVolumes(id, priority)
        }

    override suspend fun getChapterContent(
        chapterId: Identifier.Chapter,
        bookId: Identifier.Book,
        priority: WebDataSourcePriority
    ) = resilient(scope) {
        coalesce {
            key = "$chapterId:$bookId"
        }
    }.execute {
        proxiedWebBookDataSource.getChapterContent(chapterId, bookId, priority)
    }
}
