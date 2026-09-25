//FIXME: TEST 停止书籍信息与目录流发射，以测试所有依赖 BookRepository 的加载 UI。
package indi.dmzz_yyhyy.lightnovelreader.data.book

import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.EmptyWebDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.work.CacheBookWork
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.RelatedBookKind
import io.nightfish.lightnovelreader.api.book.RelatedBooksRequest
import io.nightfish.lightnovelreader.api.book.UserReadingData
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

internal data class RawBookInformation(
    val sourceId: String?,
    val supportedRelatedBookKinds: Set<RelatedBookKind>,
    val information: BookInformation,
)

internal fun WebBookDataSourceProvider.relatedBooksSource(): ProxyWebBookDataSource? =
    if (isWebDataSourceFounded()) value.takeUnless { it.origin === EmptyWebDataSource } else null

internal fun WebBookDataSourceProvider.createRelatedBooksPage(
    sourceId: String,
    request: RelatedBooksRequest,
): ExploreExpandedPageDataSource {
    val source = checkNotNull(relatedBooksSource()) { "Book source is unavailable" }
    check(source.id.toString() == sourceId) { "Book source has changed" }
    require(request.kind in source.supportedRelatedBookKinds) { "Related books are unsupported" }
    require(request.value.isNotBlank()) { "Related book value is blank" }
    return source.createRelatedBooksPage(request)
}

@Singleton
class BookRepository @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localBookDataSource: LocalBookDataSource,
    private val bookshelfRepository: BookshelfRepository,
    private val textProcessingRepository: TextProcessingRepository,
    private val workManager: WorkManager
) : BookRepositoryApi {
    companion object {
        private const val TAG = "BookRepository"
    }

    private val webBookDataSource get() = webBookDataSourceProvider.value

    val sourceId: String? get() = webBookDataSourceProvider.relatedBooksSource()?.id?.toString()
    val supportedRelatedBookKinds: Set<RelatedBookKind>
        get() = webBookDataSourceProvider.relatedBooksSource()?.supportedRelatedBookKinds.orEmpty()

    fun createRelatedBooksPage(sourceId: String, request: RelatedBooksRequest): ExploreExpandedPageDataSource =
        webBookDataSourceProvider.createRelatedBooksPage(sourceId, request)

    override fun getBookInformationFlow(
        id: String,
        priority: WebDataSourcePriority
    ): Flow<Result<BookInformation, WebRequestError>> = getRawBookInformationFlow(id, priority).map { result ->
        result.map { textProcessingRepository.processBookInformation { it.information } }
    }

    internal fun getRawBookInformationFlow(
        id: String,
        priority: WebDataSourcePriority,
    ): Flow<Result<RawBookInformation, WebRequestError>> = flow {
        val availableSource = webBookDataSourceProvider.relatedBooksSource()
        val source = availableSource ?: webBookDataSource
        val sourceId = availableSource?.id?.toString()
        val supportedKinds = availableSource?.supportedRelatedBookKinds.orEmpty()
        localBookDataSource.getBookInformation(id)?.also {
            emit(Ok(RawBookInformation(sourceId, supportedKinds, it)))
            if (BuildConfig.BENCHMARK) return@flow
        }
        source.getBookInformation(id, priority)
            .onOk { remote ->
                localBookDataSource.updateBookInformation(remote)
                val bookshelfBookMetadata =
                    bookshelfRepository.getBookshelfBookMetadata(remote.id) ?: return@onOk
                if (bookshelfBookMetadata.lastUpdate.isBefore(remote.lastUpdated))
                    bookshelfBookMetadata.bookShelfIds.forEach {
                        bookshelfRepository.updateBookshelfBookMetadataLastUpdateTime(
                            remote.id,
                            remote.lastUpdated
                        )
                        bookshelfRepository.addUpdatedBooksIntoBookShelf(it, id)
                    }
            }.onErr {
                Log.e(TAG, "Failed to request web data (title=${it.title}, message=${it.message})")
                it.throwable?.printStackTrace()
            }
            .also {
                emit(it.map { information -> RawBookInformation(sourceId, supportedKinds, information) })
            }
    }

    override fun getBookVolumesFlow(
        id: String,
        priority: WebDataSourcePriority
    ): Flow<Result<BookVolumes, WebRequestError>> = flow {
        localBookDataSource.getBookVolumes(id)?.also {
            emit(Ok(it))
            if (BuildConfig.BENCHMARK) return@flow
        }
        webBookDataSource.getBookVolumes(id, priority)
            .onOk { remote ->
                localBookDataSource.updateBookVolumes(remote)
            }.onErr {
                Log.e(TAG, "Failed to request web data (title=${it.title}, message=${it.message})")
                it.throwable?.printStackTrace()
            }
            .also {
                emit(it)
            }
    }.map { result ->
        result.map {
            textProcessingRepository.processBookVolumes { it }
        }
    }

    override fun getChapterContentFlow(
        chapterId: String,
        bookId: String,
        priority: WebDataSourcePriority
    ): Flow<Result<ChapterContent, WebRequestError>> = flow {
        localBookDataSource.getChapterContent(chapterId)?.also {
            emit(Ok(it))
            if (BuildConfig.BENCHMARK) return@flow
        }
        webBookDataSource.getChapterContent(chapterId, bookId, priority)
            .onOk { remote ->
                localBookDataSource.updateChapterContent(remote)
            }.onErr {
                Log.e(TAG, "Failed to request web data (title=${it.title}, message=${it.message})")
                it.throwable?.printStackTrace()
            }
            .also {
                emit(it)
            }
    }.map { result ->
        result.map {
            textProcessingRepository.processChapterContent(bookId) { it }
        }
    }

    override suspend fun preloadChapterContent(
        chapterId: String,
        bookId: String,
        priority: WebDataSourcePriority
    ) {
        webBookDataSource.getChapterContent(chapterId, bookId, priority)
            .onOk { remote ->
                localBookDataSource.updateChapterContent(remote)
            }.onErr {
                Log.e(TAG, "Failed to request web data (title=${it.title}, message=${it.message})")
                it.throwable?.printStackTrace()
            }
    }

    override suspend fun getUserReadingData(bookId: String): UserReadingData =
        localBookDataSource.getUserReadingData(bookId)

    override fun getUserReadingDataFlow(bookId: String): Flow<UserReadingData> =
        localBookDataSource.getUserReadingDataFlow(bookId)

    override suspend fun getAllUserReadingData(): List<UserReadingData> =
        localBookDataSource.getAllUserReadingData()

    override suspend fun updateUserReadingData(
        id: String,
        update: (UserReadingData) -> UserReadingData
    ) {
        localBookDataSource.updateUserReadingData(id, update)
    }

    fun isCacheBookWorkFlow(workId: UUID) = workManager.getWorkInfoByIdFlow(workId)

    fun cacheBook(bookId: String): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<CacheBookWork>()
            .setInputData(
                workDataOf(
                    "bookId" to bookId
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            CacheBookWork.ofId(bookId),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest
    }

    override suspend fun getIsBookCached(bookId: String): Boolean {
        localBookDataSource.getBookVolumes(bookId)?.let { bookVolumes ->
            if (bookVolumes.volumes.isEmpty())
                return false
            bookVolumes.volumes.forEach { bookVolume ->
                bookVolume.chapters.forEach {
                    if (!localBookDataSource.isChapterContentExists(it.id))
                        return false
                }
            }
        } ?: return false
        return true
    }
}
