package indi.dmzz_yyhyy.lightnovelreader.data.bookshelf

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookshelfDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfEntity
import indi.dmzz_yyhyy.lightnovelreader.data.work.CacheBookWork
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfBookMetadata
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfRepositoryApi
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookshelfRepository @Inject constructor(
    private val bookshelfDao: BookshelfDao, private val workManager: WorkManager
) : BookshelfRepositoryApi {
    override suspend fun getAllBookshelfIds(): List<Int> = bookshelfDao.getAllBookshelfIds()

    override suspend fun getAllBookshelves(): List<Bookshelf> =
        bookshelfDao.getAllBookshelves().map { bookshelfEntity ->
            Bookshelf(
                id = bookshelfEntity.id,
                name = bookshelfEntity.name,
                sortType = BookshelfSortType.map(bookshelfEntity.sortType),
                sortReversed = bookshelfEntity.sortReversed,
                autoCache = bookshelfEntity.autoCache,
                systemUpdateReminder = bookshelfEntity.systemUpdateReminder,
                allBookIds = bookshelfEntity.allBookIds,
                pinnedBookIds = bookshelfEntity.pinnedBookIds,
                updatedBookIds = bookshelfEntity.updatedBookIds
            )
        }

    override fun getAllBookshelvesFlow(): Flow<List<Bookshelf>> =
        bookshelfDao.getAllBookshelvesFlow().map { bookshelfEntities ->
            bookshelfEntities.map { bookshelfEntity ->
                Bookshelf(
                    id = bookshelfEntity.id,
                    name = bookshelfEntity.name,
                    sortType = BookshelfSortType.map(bookshelfEntity.sortType),
                    sortReversed = bookshelfEntity.sortReversed,
                    autoCache = bookshelfEntity.autoCache,
                    systemUpdateReminder = bookshelfEntity.systemUpdateReminder,
                    allBookIds = bookshelfEntity.allBookIds,
                    pinnedBookIds = bookshelfEntity.pinnedBookIds,
                    updatedBookIds = bookshelfEntity.updatedBookIds
                )
            }
        }

    override suspend fun getBookshelf(id: Int): Bookshelf? {
        val bookshelfEntity = bookshelfDao.getBookshelf(id) ?: return null
        return Bookshelf(
            id = id,
            name = bookshelfEntity.name,
            sortType = BookshelfSortType.map(bookshelfEntity.sortType),
            sortReversed = bookshelfEntity.sortReversed,
            autoCache = bookshelfEntity.autoCache,
            systemUpdateReminder = bookshelfEntity.systemUpdateReminder,
            allBookIds = bookshelfEntity.allBookIds,
            pinnedBookIds = bookshelfEntity.pinnedBookIds,
            updatedBookIds = bookshelfEntity.updatedBookIds
        )
    }

    override fun getBookshelfFlow(id: Int): Flow<Bookshelf?> =
        bookshelfDao.getBookShelfFlow(id).map { bookshelfEntity ->
                bookshelfEntity ?: return@map null
                Bookshelf(
                    id = id,
                    name = bookshelfEntity.name,
                    sortType = BookshelfSortType.map(bookshelfEntity.sortType),
                    sortReversed = bookshelfEntity.sortReversed,
                    autoCache = bookshelfEntity.autoCache,
                    systemUpdateReminder = bookshelfEntity.systemUpdateReminder,
                    allBookIds = bookshelfEntity.allBookIds,
                    pinnedBookIds = bookshelfEntity.pinnedBookIds,
                    updatedBookIds = bookshelfEntity.updatedBookIds
                )
            }

    override suspend fun addBookshelf(bookshelf: Bookshelf) {
        bookshelfDao.insertBookshelf(
            BookshelfEntity(
                bookshelf.id,
                bookshelf.name,
                bookshelf.sortType.key,
                bookshelf.sortReversed,
                bookshelf.autoCache,
                bookshelf.systemUpdateReminder,
                bookshelf.allBookIds,
                bookshelf.pinnedBookIds,
                bookshelf.updatedBookIds,
            )
        )
    }

    override suspend fun deleteBookshelf(bookshelfId: Int) {
        bookshelfDao.getBookshelf(bookshelfId)?.let { bookshelf ->
            bookshelf.allBookIds.forEach { bookId ->
                clearBookshelfIdFromBookshelfBookMetadata(bookshelfId, bookId)
            }
        }
        bookshelfDao.deleteBookshelf(bookshelfId)
    }

    override suspend fun addBookIntoBookShelf(bookshelfId: Int, bookInformation: BookInformation) {
        val bookshelf = bookshelfDao.getBookshelf(bookshelfId) ?: return
        bookshelfDao.addBookshelfMetadata(
            id = bookInformation.id,
            lastUpdate = bookInformation.lastUpdated,
            bookshelfIds = listOf(bookshelfId)
        )
        if (bookshelf.autoCache && bookshelf.allBookIds.contains(bookInformation.id)) {
            val workRequest = OneTimeWorkRequestBuilder<CacheBookWork>().setInputData(
                    workDataOf(
                        "bookId" to bookInformation.id
                    )
                ).build()
            workManager.enqueueUniqueWork(
                CacheBookWork.ofId(bookInformation.id), ExistingWorkPolicy.KEEP, workRequest
            )
        }
        (bookshelf.allBookIds + listOf(bookInformation.id)).let {
            bookshelfDao.insertBookshelf(
                bookshelf.copy(
                    allBookIds = it.distinct(),
                )
            )
        }
    }

    override suspend fun addUpdatedBooksIntoBookShelf(bookShelfId: Int, bookId: String) {
        val bookshelf = bookshelfDao.getBookshelf(bookShelfId) ?: return
        (bookshelf.updatedBookIds + listOf(bookId)).let {
            bookshelfDao.insertBookshelf(
                bookshelf.copy(
                    updatedBookIds = it.distinct(),
                )
            )
        }
    }

    override suspend fun updateBookshelf(bookshelfId: Int, updater: (Bookshelf) -> Bookshelf) {
        this.getBookshelf(bookshelfId)?.let { oldBookshelf ->
            updater(oldBookshelf).let { newBookshelf ->
                bookshelfDao.insertBookshelf(
                    BookshelfEntity(
                        bookshelfId,
                        newBookshelf.name,
                        newBookshelf.sortType.key,
                        newBookshelf.sortReversed,
                        newBookshelf.autoCache,
                        newBookshelf.systemUpdateReminder,
                        newBookshelf.allBookIds,
                        newBookshelf.pinnedBookIds,
                        newBookshelf.updatedBookIds,
                    )
                )
            }
        }
    }

    override suspend fun getAllBookshelfBooksMetadata(): List<BookshelfBookMetadata> =
        bookshelfDao.getAllBookshelfBookEntities().map {
                BookshelfBookMetadata(
                    it.id, it.lastUpdate, it.bookShelfIds
                )
            }

    override fun getAllBookshelfBookIdsFlow(): Flow<List<String>> =
        bookshelfDao.getAllBookshelfBookIdsFlow()

    override suspend fun getBookshelfBookMetadata(id: String): BookshelfBookMetadata? =
        bookshelfDao.getBookshelfBookMetadata(id)

    override fun getBookshelfBookMetadataFlow(id: String): Flow<BookshelfBookMetadata?> =
        bookshelfDao.getBookshelfBookMetadataEntityFlow(id).map {
            it ?: return@map null
            BookshelfBookMetadata(
                it.id, it.lastUpdate, it.bookShelfIds
            )
        }

    private suspend fun clearBookshelfIdFromBookshelfBookMetadata(
        bookshelfId: Int,
        bookId: String
    ) {
        bookshelfDao.getBookshelfBookMetadata(bookId)?.let { bookshelfBookMetadata ->
            bookshelfBookMetadata.bookShelfIds.toMutableList()
                .apply { removeAll { bookshelfId == it } }.let { bookshelfIds ->
                    if (bookshelfIds.isEmpty()) bookshelfDao.deleteBookshelfBookMetadata(bookId)
                    else bookshelfDao.insertBookshelfBookMetadata(
                        bookId,
                        bookshelfBookMetadata.lastUpdate,
                        ListConverter.intListToString(bookshelfIds)
                    )
                }
        }
    }

    override suspend fun deleteBookFromBookshelf(bookshelfId: Int, bookId: String) {
        clearBookshelfIdFromBookshelfBookMetadata(bookshelfId, bookId)
        updateBookshelf(bookshelfId) { oldBookshelf ->
            oldBookshelf.copy(
                allBookIds = oldBookshelf.allBookIds.toMutableList()
                .apply { removeAll { it == bookId } },
                pinnedBookIds = oldBookshelf.pinnedBookIds.toMutableList()
                    .apply { removeAll { it == bookId } },
                updatedBookIds = oldBookshelf.updatedBookIds.toMutableList()
                    .apply { removeAll { it == bookId } })
        }
    }

    override suspend fun deleteBookFromBookshelfUpdatedBookIds(bookshelfId: Int, bookId: String) {
        updateBookshelf(bookshelfId) { oldBookshelf ->
            oldBookshelf.copy(
                updatedBookIds = oldBookshelf.updatedBookIds.toMutableList()
                    .apply { removeAll { it == bookId } })
        }
    }

    override suspend fun updateBookshelfBookMetadataLastUpdateTime(
        bookId: String,
        time: LocalDateTime
    ) {
        bookshelfDao.insertBookshelfBookMetadata(
            bookId,
            time,
            ListConverter.intListToString(
                bookshelfDao.getBookshelfBookMetadata(bookId)?.bookShelfIds ?: emptyList()
            )
        )
    }

    override suspend fun clear() = bookshelfDao.clear()
}
