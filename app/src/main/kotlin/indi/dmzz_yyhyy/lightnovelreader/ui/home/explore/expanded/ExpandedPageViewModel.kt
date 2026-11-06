package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.qualifyBookId
import indi.dmzz_yyhyy.lightnovelreader.utils.convertOldId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExpandedPageViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val textProcessingRepository: TextProcessingRepository,
    private val bookRepository: BookRepository
) : ViewModel() {
    private var expandedPageDataSource: ExploreExpandedPageDataSource? = null
    private var exploreExpandedPageBookListCollectJob: Job? = null
    private var lastExpandedPageDataSourceId: String = ""
    private var sourceId: Identifier? = null
    private val _uiState = MutableExpandedPageUiState()
    val uiState: ExpandedPageUiState = _uiState

    fun init(expandedPageDataSourceId: String, sourceIdString: String = "") {
        val newSourceId = if (sourceIdString.isBlank()) null else sourceIdString.convertOldId()
        sourceId = newSourceId
        val provider = newSourceId
            ?.let { exploreRepository.getExplorePageProvider(it) }
            ?: exploreRepository.explorePageProvider
        if (provider !is ExplorePageProvider.DefaultExplorePageProvider) return
        val explorePageProvider = provider
        val cacheKey = "${sourceIdString}|$expandedPageDataSourceId"
        if (cacheKey == lastExpandedPageDataSourceId) return
        lastExpandedPageDataSourceId = cacheKey

        expandedPageDataSource =
            explorePageProvider.exploreExpandedPageDataSourceMap[expandedPageDataSourceId]

        viewModelScope.launch(Dispatchers.IO) {
            expandedPageDataSource?.let { dataSource ->
                val processedTitle = withContext(Dispatchers.IO) {
                    textProcessingRepository.processText { dataSource.title }
                }
                val filters = withContext(Dispatchers.IO) {
                    dataSource.filters
                }
                _uiState.pageTitle = processedTitle
                _uiState.filters.clear()
                _uiState.filters.addAll(filters)
            }
        }
        viewModelScope.launch {
            bookshelfRepository.getAllBookshelfBookIdsFlow().collect { ids ->
                _uiState.allBookshelfBookIds = ids.toList()
            }
        }
        loadBookResult()
    }

    fun loadBookResult() {
        _uiState.bookList.clear()
        exploreExpandedPageBookListCollectJob?.cancel()
        exploreExpandedPageBookListCollectJob = viewModelScope.launch(Dispatchers.IO) {
            expandedPageDataSource?.let { dataSource ->
                dataSource.getResultFlow().collect { rawResult ->
                    when (rawResult) {
                        is SearchResult.SingleBook -> {
                            val localBookId = bookId(rawResult.bookId)
                            _uiState.bookList.add(
                                localBookId to bookRepository.getBookInformationFlow(localBookId)
                            )
                        }

                        is SearchResult.MultipleBook -> {
                            val localBookId = bookId(rawResult.bookId)
                            _uiState.bookList.add(
                                localBookId to bookRepository.getBookInformationFlow(localBookId)
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun loadMore() {
        expandedPageDataSource?.loadMore()
    }

    fun clear() {
        lastExpandedPageDataSourceId = ""
    }

    fun refresh() {
        init(lastExpandedPageDataSourceId.substringAfter('|'), sourceId?.toString() ?: "")
    }

    private fun bookId(remoteBookId: String): String =
        sourceId?.let { qualifyBookId(it.toString(), remoteBookId) } ?: remoteBookId
}
