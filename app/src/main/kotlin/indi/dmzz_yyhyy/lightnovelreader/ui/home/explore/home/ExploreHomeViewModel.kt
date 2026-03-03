package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreHomeViewModel @Inject constructor(
    private val textProcessingRepository: TextProcessingRepository,
    exploreRepository: ExploreRepository
) : ViewModel() {
    private val _uiState = MutableExploreHomeUiState()
    private var workingExplorePageJob: Job? = null
    private var workingExploreBooksRowsJob: Job? = null
    val uiState: ExploreHomeUiState = _uiState
    private val explorePageProvider = exploreRepository.explorePageProvider
    var customExplorePageProvider: ExplorePageProvider.CustomExplorePageProvider<*>? = null

    init {
        if (explorePageProvider is ExplorePageProvider.CustomExplorePageProvider<*>) {
            customExplorePageProvider = explorePageProvider
        }
    }

    fun init() {
        when (explorePageProvider) {
            is ExplorePageProvider.DefaultExplorePageProvider -> changePage(_uiState.selectedPage)
            is ExplorePageProvider.CustomExplorePageProvider<*> -> explorePageProvider.init(viewModelScope)
        }
    }

    fun changePage(page: Int) {
        if (explorePageProvider !is ExplorePageProvider.DefaultExplorePageProvider) return
        if (explorePageProvider.explorePageIdList.isEmpty()) return
        workingExplorePageJob?.cancel()
        workingExploreBooksRowsJob?.cancel()
        _uiState.selectedPage = page
        workingExplorePageJob = viewModelScope.launch {
            val selectedId = explorePageProvider.explorePageIdList[page]
            val explorePageMap = explorePageProvider.exploreTapPageDataSourceMap
            _uiState.pageTitles = explorePageMap.map { it.value.title }
            workingExploreBooksRowsJob = viewModelScope.launch(Dispatchers.IO) {
                explorePageMap[selectedId]?.getRowsFlow()?.collect { exploreBooksRows ->
                    _uiState.explorePageBooksRawList = exploreBooksRows
                        .map { exploreBooksRow ->
                            exploreBooksRow.copy(
                                bookList = exploreBooksRow.bookList.map {
                                    textProcessingRepository.processExploreBooksRow(it)
                                }
                            )
                        }
                }
            }
        }
    }

    fun refresh() {
        _uiState.explorePageBooksRawList = emptyList()
        changePage(_uiState.selectedPage)
    }
}
