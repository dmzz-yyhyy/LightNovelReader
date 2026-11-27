package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.BookPageResult
import io.nightfish.lightnovelreader.api.web.PageBook
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class BookPageState(
    val generation: Long = 0,
    val allBookIds: List<Identifier.Book> = emptyList(),
    val books: List<PageBook> = emptyList(),
    val loading: Boolean = false,
    val canAppend: Boolean = false,
    val error: WebRequestError? = null,
    val openBook: Identifier.Book? = null
)

internal class BookPager<Q : Any>(
    private val scope: CoroutineScope,
    private val load: suspend (Q, String?) -> Result<BookPageResult, WebRequestError>
) {
    private val mutableState = MutableStateFlow(BookPageState())
    val state: StateFlow<BookPageState> = mutableState
    private val books = linkedMapOf<Identifier.Book, PageBook>()
    private val loadedCursors = mutableSetOf<String?>()
    private var predicates: List<(PageBook) -> Boolean> = emptyList()
    private var query: Q? = null
    private var job: Job? = null
    private var cursorToRetry: String? = null
    private var nextCursor: String? = null

    fun start(query: Q) {
        clear()
        this.query = query
        request(query, null)
    }

    fun clear() {
        job?.cancel()
        job = null
        query = null
        books.clear()
        loadedCursors.clear()
        nextCursor = null
        mutableState.value = BookPageState(generation = mutableState.value.generation + 1)
    }

    fun setLocalFilters(predicates: List<(PageBook) -> Boolean>) {
        this.predicates = predicates
        publishBooks()
    }

    fun append() {
        val query = query ?: return
        val cursor = nextCursor ?: return
        val current = mutableState.value
        if (current.loading || current.error != null) return
        request(query, cursor)
    }

    fun retry() {
        val query = query ?: return
        val current = mutableState.value
        if (current.loading || current.error == null) return
        request(query, cursorToRetry)
    }

    private fun request(query: Q, cursor: String?) {
        val current = mutableState.value
        val generation = current.generation
        cursorToRetry = cursor
        mutableState.value = current.copy(loading = true, error = null)
        job = scope.launch {
            val result = try {
                withContext(Dispatchers.IO) { load(query, cursor) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Err(WebRequestError("", error.localizedMessage.orEmpty(), error))
            }
            if (mutableState.value.generation != generation) return@launch

            result.fold(
                success = { page ->
                    when (page) {
                        is BookPageResult.Page -> {
                            page.books.forEach { books.putIfAbsent(it.id, it) }
                            loadedCursors.add(cursor)
                            nextCursor = page.nextCursor?.takeUnless { it in loadedCursors }
                            publishBooks(loading = false)
                        }

                        is BookPageResult.OpenBook -> {
                            nextCursor = null
                            mutableState.value = mutableState.value.copy(
                                loading = false,
                                canAppend = false,
                                openBook = page.bookId.takeIf { cursor == null }
                            )
                        }
                    }
                },
                failure = { error ->
                    mutableState.value = mutableState.value.copy(loading = false, error = error)
                }
            )
        }
    }

    private fun publishBooks(loading: Boolean = mutableState.value.loading) {
        mutableState.value = mutableState.value.copy(
            allBookIds = books.keys.toList(),
            books = books.values.filter { book -> predicates.all { it(book) } },
            loading = loading,
            canAppend = nextCursor != null
        )
    }
}
