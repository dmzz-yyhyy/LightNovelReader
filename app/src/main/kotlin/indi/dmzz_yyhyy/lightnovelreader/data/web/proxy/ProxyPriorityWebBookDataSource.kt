package indi.dmzz_yyhyy.lightnovelreader.data.web.proxy

import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.coroutine.PriorityDispatcher
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.withContext

class ProxyPriorityWebBookDataSource(
    override val origin: WebBookDataSource
) : ProxyWebBookDataSource {
    override val proxiedWebBookDataSource: ProxyWebBookDataSource = this
    private val dispatcher: PriorityDispatcher = PriorityDispatcher(origin.permits)

    private fun checkId(value: Identifier) {
        require(value.namespace == origin.id.sourceNamespace) { "ID does not belong to datasource ${origin.id}: $value" }
    }

    override suspend fun getBookInformation(id: Identifier.Book, priority: WebDataSourcePriority) =
        withContext(dispatcher + PriorityDispatcher.Priority(priority.priority)) {
            checkId(id)
            origin.getBookInformation(id).onOk { checkId(it.id); require(it.id == id) }
        }

    override suspend fun getBookVolumes(id: Identifier.Book, priority: WebDataSourcePriority) =
        withContext(dispatcher + PriorityDispatcher.Priority(priority.priority)) {
            checkId(id)
            origin.getBookVolumes(id).onOk { volumes ->
                require(volumes.bookId == id)
                volumes.volumes.forEach { volume ->
                    checkId(volume.volumeId)
                    volume.chapters.forEach { checkId(it.id) }
                }
            }
        }

    override suspend fun getChapterContent(
        chapterId: Identifier.Chapter,
        bookId: Identifier.Book,
        priority: WebDataSourcePriority
    ) = withContext(dispatcher + PriorityDispatcher.Priority(priority.priority)) {
        checkId(chapterId)
        checkId(bookId)
        origin.getChapterContent(chapterId, bookId).onOk { chapter ->
            require(chapter.id == chapterId)
            chapter.prevChapter?.let { checkId(it) }
            chapter.nextChapter?.let { checkId(it) }
        }
    }
}
