package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.BookPager
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.lightnovelreader.api.userdata.update
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.explore.filter.FilterValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExploreSearchViewModel @Inject constructor(
    exploreRepository: ExploreRepository,
    bookshelfRepository: BookshelfRepository,
    private val textProcessingRepository: TextProcessingRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val provider = exploreRepository.searchProvider
    private val historyData = userDataRepository.userData(UserDataPath.Search.History)
    private val pager = BookPager<Request>(viewModelScope) { request, cursor ->
        provider.loadPage(request.keyword, request.settings, request.filters, cursor)
    }
    private var suggestionsJob: Job? = null
    internal val pageState = pager.state
    internal val settingDefinitions = provider.settings
    internal val filterDefinitions = provider.filters
    internal var settings by mutableStateOf(OptionValues.Empty)
        private set
    internal var filters by mutableStateOf(FilterValues())
        private set
    internal var keyword by mutableStateOf("")
        private set
    internal var submittedKeyword by mutableStateOf<String?>(null)
        private set
    internal var history by mutableStateOf(emptyList<String>())
        private set
    internal var suggestions by mutableStateOf(emptyList<String>())
        private set
    internal var bookshelfIds by mutableStateOf(emptySet<Identifier.Book>())
        private set

    init {
        pager.setLocalFilters(filterDefinitions.map { it.predicate(filters.local) })
        viewModelScope.launch {
            historyData.getFlow().collect { history = it.orEmpty().asReversed().distinct() }
        }
        viewModelScope.launch {
            bookshelfRepository.getAllBookshelfBookIdsFlow().collect { bookshelfIds = it.toSet() }
        }
    }

    @Composable
    internal fun getSearchTip() = provider.getSearchTip(settings)

    internal fun updateInput(value: String) {
        keyword = value
        suggestionsJob?.cancel()
        suggestions = emptyList()
        if (value.isBlank()) return
        val recent = history
        suggestionsJob = viewModelScope.launch {
            suggestions = withContext(Dispatchers.IO) {
                provider.getSearchSuggestions(
                    recent,
                    value
                )
            }.distinct()
        }
    }

    internal fun search(input: String) {
        val value = input.trim()
        if (value.isEmpty()) return
        keyword = value
        submittedKeyword = value
        reload()
        viewModelScope.launch {
            historyData.update { it.filterNot { entry -> entry == value } + value }
        }
    }

    internal fun editSearch() {
        submittedKeyword = null
        pager.clear()
        updateInput(keyword)
    }

    internal fun clearInput() {
        editSearch()
        updateInput("")
    }

    internal fun changeSettings(values: OptionValues) {
        if (settings == values) return
        settings = values
        reload()
    }

    internal fun updateFilters(values: FilterValues) {
        val previous = filters
        filters = values
        pager.setLocalFilters(filterDefinitions.map { it.predicate(values.local) })
        if (values.dataSource != previous.dataSource) reload()
    }

    internal fun append() = pager.append()
    internal fun retry() = pager.retry()
    internal fun processBook(book: BookInformation) =
        textProcessingRepository.processBookInformation { book }

    internal fun deleteHistory(entry: String) {
        viewModelScope.launch { historyData.update { it - entry } }
    }

    internal fun clearHistory() {
        viewModelScope.launch { historyData.update { emptyList() } }
    }

    private fun reload() {
        val query = submittedKeyword ?: return
        pager.start(Request(query, settings, filters.dataSource))
    }

    private data class Request(
        val keyword: String,
        val settings: OptionValues,
        val filters: OptionValues
    )
}
