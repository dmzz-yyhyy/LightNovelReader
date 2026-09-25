package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import io.nightfish.lightnovelreader.api.book.RelatedBooksRequest
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class ExpandedPageViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val textProcessingRepository: TextProcessingRepository,
    private val bookRepository: BookRepository
) : ViewModel() {
    private var dataSource: ExploreExpandedPageDataSource? = null
    private var resultJob: Job? = null
    private val collectionMutex = Mutex()
    private var initialized = false
    private var sourceId: String? = null
    private var pageId: String? = null
    private var relatedRequest: RelatedBooksRequest? = null
    private val _uiState = MutableExpandedPageUiState()
    val uiState: ExpandedPageUiState = _uiState

    init {
        viewModelScope.launch {
            bookshelfRepository.getAllBookshelfBookIdsFlow().collect {
                _uiState.allBookshelfBookIds = it.toList()
            }
        }
    }

    fun init(expandedPageDataSourceId: String) {
        if (initialized) return
        initialized = true
        sourceId = bookRepository.sourceId
        pageId = expandedPageDataSourceId
        loadBookResult()
    }

    fun initRelated(sourceId: String, request: RelatedBooksRequest) {
        if (initialized) return
        initialized = true
        this.sourceId = sourceId
        relatedRequest = request
        _uiState.pageTitle = textProcessingRepository.processText { request.value }
        loadBookResult()
    }

    private fun checkSource() {
        check(sourceId != null && sourceId == bookRepository.sourceId) {
            "The book source is unavailable."
        }
        relatedRequest?.let {
            check(it.kind in bookRepository.supportedRelatedBookKinds && it.value.isNotBlank()) {
                "This source does not support the requested list."
            }
        }
    }

    fun loadBookResult() {
        resultJob?.cancel()
        resultJob = viewModelScope.launch {
            collectionMutex.withLock {
                _uiState.bookList.clear()
                _uiState.isLoading = true
                _uiState.isComplete = false
                _uiState.errorMessage = null
                try {
                    checkSource()
                    val current = dataSource ?: run {
                        val request = relatedRequest
                        val created = if (request != null) {
                            bookRepository.createRelatedBooksPage(checkNotNull(sourceId), request)
                        } else {
                            val provider = exploreRepository.explorePageProvider as? ExplorePageProvider.DefaultExplorePageProvider
                            checkNotNull(provider?.exploreExpandedPageDataSourceMap?.get(pageId)) {
                                "The book list is unavailable."
                            }
                        }
                        val title = withContext(Dispatchers.IO) {
                            textProcessingRepository.processText { created.title }
                        }
                        _uiState.pageTitle = title
                        _uiState.filters.clear()
                        _uiState.filters.addAll(created.filters)
                        dataSource = created
                        created
                    }
                    current.getResultFlow().flowOn(Dispatchers.IO).takeWhile { result ->
                        _uiState.acceptResult(result) { bookRepository.getBookInformationFlow(it) }
                    }.collect()
                    _uiState.isLoading = false
                    _uiState.isComplete = true
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    showError(error)
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.isLoading || _uiState.isComplete) return
        try {
            checkSource()
            dataSource?.loadMore()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            resultJob?.cancel()
            showError(error)
        }
    }

    private fun showError(error: Exception) {
        _uiState.errorMessage = error.message.orEmpty()
        _uiState.isLoading = false
        _uiState.isComplete = true
    }
}
