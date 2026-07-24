package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
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
    private val _uiState = MutableExpandedPageUiState()
    val uiState: ExpandedPageUiState = _uiState

    fun init(expandedPageDataSourceId: String) {
        if (exploreRepository.explorePageProvider !is ExplorePageProvider.DefaultExplorePageProvider) return
        val explorePageProvider = exploreRepository.explorePageProvider as ExplorePageProvider.DefaultExplorePageProvider
        if (expandedPageDataSourceId == lastExpandedPageDataSourceId) return
        lastExpandedPageDataSourceId = expandedPageDataSourceId

        expandedPageDataSource = explorePageProvider.exploreExpandedPageDataSourceMap[expandedPageDataSourceId]

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
                    when(rawResult) {
                        is SearchResult.SingleBook -> _uiState.bookList.add(rawResult.bookId to bookRepository.getBookInformationFlow(rawResult.bookId))
                        is SearchResult.MultipleBook -> _uiState.bookList.add(rawResult.bookId to bookRepository.getBookInformationFlow(rawResult.bookId))
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
        init(lastExpandedPageDataSourceId)
    }
}
