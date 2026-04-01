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
import io.nightfish.lightnovelreader.api.bookshelf.MutableBookshelf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookshelfRepository @Inject constructor(
    private val bookshelfDao: BookshelfDao,
    private val workManager: WorkManager
): BookshelfRepositoryApi {
    override fun getAllBookshelfIds(): List<Int> = bookshelfDao.getAllBookshelfIds()

    override fun getAllBookshelves(): List<MutableBookshelf> = bookshelfDao.getAllBookshelves().map { bookshelfEntity ->
        MutableBookshelf().apply {
            this.id =   bookshelfEntity.id
            this.name = bookshelfEntity.name
            this.sortType = BookshelfSortType.entries.first { it.key == bookshelfEntity.sortType }
            this.autoCache = bookshelfEntity.autoCache
            this.systemUpdateReminder = bookshelfEntity.systemUpdateReminder
            this.allBookIds = bookshelfEntity.allBookIds
            this.pinnedBookIds = bookshelfEntity.pinnedBookIds
            this.updatedBookIds = bookshelfEntity.updatedBookIds
        }
    }

    override fun getAllBookshelvesFlow(): Flow<List<MutableBookshelf>> = bookshelfDao.getAllBookshelvesFlow().map { bookshelfEntities ->
        bookshelfEntities.map { bookshelfEntity ->
            MutableBookshelf().apply {
                this.id =   bookshelfEntity.id
                this.name = bookshelfEntity.name
                this.sortType = BookshelfSortType.entries.first { it.key == bookshelfEntity.sortType }
                this.autoCache = bookshelfEntity.autoCache
                this.systemUpdateReminder = bookshelfEntity.systemUpdateReminder
                this.allBookIds = bookshelfEntity.allBookIds
                this.pinnedBookIds = bookshelfEntity.pinnedBookIds
                this.updatedBookIds = bookshelfEntity.updatedBookIds
            }
        }
    }

    override fun getBookshelf(id: Int): MutableBookshelf? = MutableBookshelf().apply {
        val bookshelfEntity = bookshelfDao.getBookshelf(id) ?: return null
        this.id = id
        this.name = bookshelfEntity.name
        this.sortType = BookshelfSortType.entries.first { it.key == bookshelfEntity.sortType }
        this.autoCache = bookshelfEntity.autoCache
        this.systemUpdateReminder = bookshelfEntity.systemUpdateReminder
        this.allBookIds = bookshelfEntity.allBookIds
        this.pinnedBookIds = bookshelfEntity.pinnedBookIds
        this.updatedBookIds = bookshelfEntity.updatedBookIds
    }

    override fun getBookshelfFlow(id: Int): Flow<MutableBookshelf?> = bookshelfDao
        .getBookShelfFlow(id)
        .map { bookshelfEntity ->
            bookshelfEntity ?: return@map null
            MutableBookshelf().apply {
                this.id = id
                this.name = bookshelfEntity.name
                this.sortType = BookshelfSortType.entries.first { it.key == bookshelfEntity.sortType }
                this.autoCache = bookshelfEntity.autoCache
                this.systemUpdateReminder = bookshelfEntity.systemUpdateReminder
                this.allBookIds = bookshelfEntity.allBookIds
                this.pinnedBookIds = bookshelfEntity.pinnedBookIds
                this.updatedBookIds = bookshelfEntity.updatedBookIds
            }
        }

    override fun createBookShelf(
        id: Int,
        name: String,
        sortType: BookshelfSortType,
        autoCache: Boolean,
        systemUpdateReminder: Boolean,
    ): Int {
        bookshelfDao.createBookshelf(BookshelfEntity(
            id = id,
            name = name,
            sortType = sortType.key,
            autoCache = autoCache,
            systemUpdateReminder = systemUpdateReminder,
            allBookIds = emptyList(),
            pinnedBookIds = emptyList(),
            updatedBookIds = emptyList(),
        ))
        return Instant.now().epochSecond.hashCode()
    }

    override fun deleteBookshelf(bookshelfId: Int) {
        bookshelfDao.getBookshelf(bookshelfId)?.let { bookshelf ->
            bookshelf.allBookIds.forEach { bookId ->
                clearBookshelfIdFromBookshelfBookMetadata(bookshelfId, bookId)
            }
        }
        bookshelfDao.deleteBookshelf(bookshelfId)
    }

    override fun addBookIntoBookShelf(bookshelfId: Int, bookInformation: BookInformation) {
        val bookshelf = bookshelfDao.getBookshelf(bookshelfId) ?: return
        bookshelfDao.addBookshelfMetadata(
            id = bookInformation.id,
            lastUpdate = bookInformation.lastUpdated,
            bookshelfIds = listOf(bookshelfId)
        )
        if (bookshelf.autoCache && bookshelf.allBookIds.contains(bookInformation.id)) {
            val workRequest = OneTimeWorkRequestBuilder<CacheBookWork>()
                .setInputData(
                    workDataOf(
                    "bookId" to bookInformation.id
                )
                )
                .build()
            workManager.enqueueUniqueWork(
                CacheBookWork.ofId(bookInformation.id),
                ExistingWorkPolicy.KEEP,
                workRequest
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

    override fun addUpdatedBooksIntoBookShelf(bookShelfId: Int, bookId: String) {
        val bookshelf = bookshelfDao.getBookshelf(bookShelfId) ?: return
        (bookshelf.updatedBookIds + listOf(bookId)).let {
            bookshelfDao.insertBookshelf(
                bookshelf.copy(
                    updatedBookIds = it.distinct(),
                )
            )
        }
    }

    override fun updateBookshelf(bookshelfId: Int, updater: (MutableBookshelf) -> Bookshelf) {
        this.getBookshelf(bookshelfId)?.let { oldBookshelf ->
            updater(oldBookshelf).let { newBookshelf ->
                bookshelfDao.insertBookshelf(
                    BookshelfEntity(
                        bookshelfId,
                        newBookshelf.name,
                        newBookshelf.sortType.key,
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

    override fun getAllBookshelfBooksMetadata(): List<BookshelfBookMetadata> = bookshelfDao
        .getAllBookshelfBookEntities()
        .map {
            BookshelfBookMetadata(
                it.id,
                it.lastUpdate,
                it.bookShelfIds
            )
        }

    override fun getAllBookshelfBookIdsFlow(): Flow<List<String>> = bookshelfDao.getAllBookshelfBookIdsFlow()

    override fun getBookshelfBookMetadata(id: String): BookshelfBookMetadata? = bookshelfDao.getBookshelfBookMetadata(id)

    override fun getBookshelfBookMetadataFlow(id: String): Flow<BookshelfBookMetadata?> = bookshelfDao.getBookshelfBookMetadataEntityFlow(id).map {
        it ?: return@map null
        BookshelfBookMetadata(
            it.id,
            it.lastUpdate,
            it.bookShelfIds
        )
    }

    private fun clearBookshelfIdFromBookshelfBookMetadata(bookshelfId: Int, bookId: String) {
        bookshelfDao.getBookshelfBookMetadata(bookId)?.let { bookshelfBookMetadata ->
            bookshelfBookMetadata.bookShelfIds
                .toMutableList()
                .apply { removeAll { bookshelfId == it } }
                .let { bookshelfIds ->
                    if (bookshelfIds.isEmpty()) bookshelfDao.deleteBookshelfBookMetadata(bookId)
                    else
                        bookshelfDao.insertBookshelfBookMetadata(
                            bookId,
                            bookshelfBookMetadata.lastUpdate,
                            ListConverter.intListToString(bookshelfIds)
                        )
                }
        }
    }

    override fun deleteBookFromBookshelf(bookshelfId: Int, bookId: String) {
        clearBookshelfIdFromBookshelfBookMetadata(bookshelfId, bookId)
        updateBookshelf(bookshelfId) { oldBookshelf ->
            oldBookshelf.apply {
                this.allBookIds = allBookIds.toMutableList().apply { removeAll { it == bookId } }
                this.pinnedBookIds = pinnedBookIds.toMutableList().apply { removeAll { it == bookId } }
                this.updatedBookIds = updatedBookIds.toMutableList().apply { removeAll { it == bookId } }
            }
        }
    }

    override fun deleteBookFromBookshelfUpdatedBookIds(bookshelfId: Int, bookId: String) {
        updateBookshelf(bookshelfId) { oldBookshelf ->
            oldBookshelf.apply {
                this.updatedBookIds = updatedBookIds.toMutableList().apply { removeAll { it == bookId } }
            }
        }
    }

    override fun updateBookshelfBookMetadataLastUpdateTime(bookId: String, time: LocalDateTime) {
        bookshelfDao.insertBookshelfBookMetadata(
            bookId,
            time,
            ListConverter.intListToString(bookshelfDao.getBookshelfBookMetadata(bookId)?.bookShelfIds ?: emptyList())
        )
    }

    override fun clear() = bookshelfDao.clear()
}