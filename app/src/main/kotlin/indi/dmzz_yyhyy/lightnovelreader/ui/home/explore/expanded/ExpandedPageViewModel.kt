package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.BookPager
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPage
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import io.nightfish.lightnovelreader.api.web.explore.filter.FilterValues
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpandedPageViewModel @Inject constructor(
    exploreRepository: ExploreRepository,
    bookshelfRepository: BookshelfRepository,
    private val textProcessingRepository: TextProcessingRepository
) : ViewModel() {
    private val provider =
        exploreRepository.explorePageProvider as? ExplorePageProvider.DefaultExplorePageProvider
    private var dataSource: ExploreExpandedPageDataSource? = null
    private val pager = BookPager<Request>(viewModelScope) { request, cursor ->
        request.dataSource.loadPage(cursor, request.filters)
    }
    internal val pageState = pager.state
    internal var page by mutableStateOf<ExploreExpandedPage?>(null)
        private set
    internal var filterDefinitions by mutableStateOf(emptyList<Filter<*>>())
        private set
    internal var filters by mutableStateOf(FilterValues())
        private set
    internal var bookshelfIds by mutableStateOf(emptySet<Identifier.Book>())
        private set

    init {
        viewModelScope.launch {
            bookshelfRepository.getAllBookshelfBookIdsFlow().collect { bookshelfIds = it.toSet() }
        }
    }

    internal fun open(id: String) {
        val next = provider?.exploreExpandedPages?.get(id)
        if (next == page) return
        page = next
        dataSource = next as? ExploreExpandedPageDataSource
        filterDefinitions = dataSource?.filters.orEmpty()
        filters = FilterValues()
        pager.setLocalFilters(filterDefinitions.map { it.predicate(filters.local) })
        refresh()
    }

    internal fun updateFilters(values: FilterValues) {
        val previous = filters
        filters = values
        pager.setLocalFilters(filterDefinitions.map { it.predicate(values.local) })
        if (values.dataSource != previous.dataSource) refresh()
    }

    internal fun refresh() {
        val source = dataSource
        if (source == null) pager.clear() else pager.start(Request(source, filters.dataSource))
    }

    internal fun append() = pager.append()
    internal fun retry() = pager.retry()
    internal fun processBook(book: BookInformation) =
        textProcessingRepository.processBookInformation { book }

    internal fun processTitle(title: String) = textProcessingRepository.processText { title }

    private data class Request(
        val dataSource: ExploreExpandedPageDataSource,
        val filters: OptionValues
    )
}
