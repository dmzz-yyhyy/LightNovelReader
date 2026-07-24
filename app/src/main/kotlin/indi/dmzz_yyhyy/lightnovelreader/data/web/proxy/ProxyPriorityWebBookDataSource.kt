package indi.dmzz_yyhyy.lightnovelreader.data.web.proxy

import indi.dmzz_yyhyy.lightnovelreader.coroutine.PriorityDispatcher
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.withContext

class ProxyPriorityWebBookDataSource(
    override val origin: WebBookDataSource
) : ProxyWebBookDataSource {
    override val proxiedWebBookDataSource: ProxyWebBookDataSource = this
    private val dispatcher: PriorityDispatcher = PriorityDispatcher(origin.permits)

    override suspend fun getBookInformation(id: String, priority: WebDataSourcePriority) = withContext(dispatcher + PriorityDispatcher.Priority(priority.priority)) {
        origin.getBookInformation(id)
    }

    override suspend fun getBookVolumes(id: String, priority: WebDataSourcePriority) = withContext(dispatcher + PriorityDispatcher.Priority(priority.priority)) {
        origin.getBookVolumes(id)
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String, priority: WebDataSourcePriority) = withContext(dispatcher + PriorityDispatcher.Priority(priority.priority)) {
        origin.getChapterContent(chapterId, bookId)
    }
}
