package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.qualifyBookId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreHomeViewModel @Inject constructor(
    private val textProcessingRepository: TextProcessingRepository,
    private val exploreRepository: ExploreRepository
) : ViewModel() {
    private val _uiState = MutableExploreHomeUiState()
    private var workingExplorePageJob: Job? = null
    private var workingExploreBooksRowsJob: Job? = null
    val uiState: ExploreHomeUiState = _uiState

    private var explorePageProvider: ExplorePageProvider? = null
    var customExplorePageProvider by mutableStateOf<ExplorePageProvider.CustomExplorePageProvider<*>?>(null)
        private set

    init {
        val enabledIds = exploreRepository.getEnabledSources().map { it.id }.toSet()
        val items = exploreRepository.sourceItems
            .filter { enabledIds.isEmpty() || enabledIds.contains(it.id) }
            .map { it.id to it.name }
        _uiState.sourceItems = items
        val initialSourceId = when {
            items.isEmpty() -> null
            items.any { it.first == exploreRepository.primarySourceId } ->
                exploreRepository.primarySourceId

            else -> items.first().first
        }
        selectSource(initialSourceId, reset = false)
    }

    fun selectSource(sourceId: Identifier?, reset: Boolean = true) {
        if (sourceId == null) {
            explorePageProvider = null
            customExplorePageProvider = null
            _uiState.selectedSourceId = null
            return
        }
        if (reset && _uiState.selectedSourceId == sourceId) return
        _uiState.selectedSourceId = sourceId
        explorePageProvider = exploreRepository.getExplorePageProvider(sourceId)
        customExplorePageProvider =
            explorePageProvider as? ExplorePageProvider.CustomExplorePageProvider<*>
        if (reset) {
            workingExplorePageJob?.cancel()
            workingExploreBooksRowsJob?.cancel()
            _uiState.pageTitles = emptyList()
            _uiState.explorePageBooksRawList = emptyList()
            _uiState.selectedPage = 0
            init()
        }
    }

    fun init() {
        when (val provider = explorePageProvider) {
            is ExplorePageProvider.DefaultExplorePageProvider -> {
                if (_uiState.pageTitles.isEmpty()) {
                    _uiState.pageTitles =
                        provider.exploreTapPageDataSourceMap.map { it.value.title }
                }
                if (_uiState.explorePageBooksRawList.isEmpty()) {
                    loadPage(_uiState.selectedPage, forceRefresh = true)
                }
            }

            is ExplorePageProvider.CustomExplorePageProvider<*> -> provider.init(viewModelScope)
            null -> Unit
        }
    }

    fun changeSource(sourceId: Identifier) {
        selectSource(sourceId, reset = true)
    }

    fun changePage(page: Int) {
        loadPage(page)
    }

    private fun loadPage(page: Int, forceRefresh: Boolean = false) {
        val provider = explorePageProvider
        if (provider !is ExplorePageProvider.DefaultExplorePageProvider) return
        if (provider.explorePageIdList.isEmpty()) return
        val pageChanged = _uiState.selectedPage != page
        if (!forceRefresh && !pageChanged && _uiState.explorePageBooksRawList.isNotEmpty()) return
        workingExplorePageJob?.cancel()
        workingExploreBooksRowsJob?.cancel()
        _uiState.selectedPage = page
        _uiState.explorePageBooksRawList = emptyList()
        val sourceId = _uiState.selectedSourceId ?: return
        workingExplorePageJob = viewModelScope.launch {
            val selectedId = provider.explorePageIdList.getOrNull(page) ?: return@launch
            val explorePageMap = provider.exploreTapPageDataSourceMap
            _uiState.pageTitles = explorePageMap.map { it.value.title }
            workingExploreBooksRowsJob = viewModelScope.launch(Dispatchers.IO) {
                explorePageMap[selectedId]?.getRowsFlow()?.collect { exploreBooksRows ->
                    _uiState.explorePageBooksRawList = exploreBooksRows
                        .map { exploreBooksRow ->
                            exploreBooksRow.copy(
                                bookList = exploreBooksRow.bookList.map {
                                    textProcessingRepository.processExploreBooksRow(it).let { book ->
                                        book.copy(
                                            id = qualifyBookId(sourceId.toString(), book.id)
                                        )
                                    }
                                }
                            )
                        }
                }
            }
        }
    }

    fun refresh() {
        _uiState.explorePageBooksRawList = emptyList()
        loadPage(_uiState.selectedPage, forceRefresh = true)
    }
}
