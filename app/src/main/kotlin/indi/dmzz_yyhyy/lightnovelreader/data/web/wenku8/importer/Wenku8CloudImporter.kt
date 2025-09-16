package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.importer

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login.Wenku8SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface Wenku8CloudImportState {
    data object Idle: Wenku8CloudImportState
    data class Prepared(
        val targetBookshelfId: Int,
        val needImportIds: List<Int>,
        val alreadyExists: Int
    ): Wenku8CloudImportState
    data class Running(
        val total: Int,
        val current: Int,
        val currentBookId: Int? = null
    ): Wenku8CloudImportState
    data class Success(
        val imported: Int,
        val skipped: Int,
        val successIds: List<Int>,
        val failedIds: List<Int>
    ): Wenku8CloudImportState
    data class Failure(val error: Wenku8CloudImportError): Wenku8CloudImportState
    data class Cancelled(val imported: Int, val successIds: List<Int>, val failedIds: List<Int>): Wenku8CloudImportState
    data object NeedLogin: Wenku8CloudImportState
}

@Singleton
class Wenku8CloudImporter @Inject constructor(
    private val sessionManager: Wenku8SessionManager,
    private val wenku8Api: Wenku8Api,
    private val bookshelfRepository: BookshelfRepository,
    private val bookRepository: BookRepository
) {
    suspend fun prepare(targetBookshelfId: Int): Wenku8CloudImportState = withContext(Dispatchers.IO) {
        val loggedIn = sessionManager.isLoggedIn()
        if (!loggedIn) return@withContext Wenku8CloudImportState.NeedLogin
        val remoteIds = try {
            wenku8Api.getUserBookshelfIds()
        } catch (e: IllegalStateException) {
            val msg = e.message ?: ""
            if (msg.startsWith("WENKU8_CODE_")) {
                val code = msg.removePrefix("WENKU8_CODE_").toIntOrNull()
                if (code == 4) return@withContext Wenku8CloudImportState.NeedLogin
            }
            return@withContext Wenku8CloudImportState.Failure(Wenku8CloudImportError.Unknown(e))
        } catch (e: Exception) {
            return@withContext Wenku8CloudImportState.Failure(Wenku8CloudImportError.Unknown(e))
        }
        val bookshelf = bookshelfRepository.getBookshelf(targetBookshelfId)
            ?: return@withContext Wenku8CloudImportState.Failure(Wenku8CloudImportError.Unknown(IllegalStateException("Bookshelf not found")))
        val localSet = bookshelf.allBookIds.toSet()
        val needImport = remoteIds.filterNot(localSet::contains)
        return@withContext Wenku8CloudImportState.Prepared(
            targetBookshelfId = targetBookshelfId,
            needImportIds = needImport,
            alreadyExists = remoteIds.size - needImport.size
        )
    }

    suspend fun execute(prepared: Wenku8CloudImportState.Prepared,
                        isCancelled: () -> Boolean,
                        onProgress: suspend (Wenku8CloudImportState.Running) -> Unit,
                        concurrency: Int = 4,
                        maxRetry: Int = 3
    ): Wenku8CloudImportState = withContext(Dispatchers.IO) {
        val ids = prepared.needImportIds
        if (ids.isEmpty()) return@withContext Wenku8CloudImportState.Success(
            imported = 0,
            skipped = prepared.alreadyExists,
            successIds = emptyList(),
            failedIds = emptyList()
        )
        val success = mutableListOf<Int>()
        val failed = mutableListOf<Int>()
        val semaphore = kotlinx.coroutines.sync.Semaphore(concurrency)
        var current = 0
        coroutineScope {
            ids.map { bookId ->
                async {
                    semaphore.acquire()
                    try {
                        if (isCancelled()) return@async
                        var attempt = 0
                        while (attempt < maxRetry) {
                            try {
                                val info = wenku8Api.getBookInformation(bookId)
                                if (info.isEmpty()) throw IllegalStateException("EMPTY_INFO")
                                try { wenku8Api.getBookVolumes(bookId) } catch (_: Exception) {}
                                bookshelfRepository.addBookIntoBookShelf(prepared.targetBookshelfId, info)
                                success += bookId
                                break
                            } catch (e: Exception) {
                                attempt++
                                if (attempt >= maxRetry) {
                                    failed += bookId
                                    break
                                }
                                val backoff = 200L * (1 shl (attempt - 1))
                                kotlinx.coroutines.delay(backoff)
                            }
                        }
                    } finally {
                        current++
                        onProgress(
                            Wenku8CloudImportState.Running(
                                total = ids.size,
                                current = current,
                                currentBookId = bookId
                            )
                        )
                        semaphore.release()
                    }
                }
            }.forEach { it.await() }
        }
        if (isCancelled()) return@withContext Wenku8CloudImportState.Cancelled(
            imported = success.size,
            successIds = success,
            failedIds = failed
        )
        return@withContext Wenku8CloudImportState.Success(
            imported = success.size,
            skipped = prepared.alreadyExists + (ids.size - success.size - failed.size),
            successIds = success,
            failedIds = failed
        )
    }
}
