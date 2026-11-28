package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import io.nightfish.lightnovelreader.api.web.explore.CustomExploreTapPage
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreHomeViewModel @Inject constructor(
    private val textProcessingRepository: TextProcessingRepository,
    exploreRepository: ExploreRepository
) : ViewModel() {
    private val provider = exploreRepository.explorePageProvider
    private val pages =
        (provider as? ExplorePageProvider.DefaultExplorePageProvider)?.exploreTapPages
            ?.values.orEmpty().toList()
    private val mutableUiState = MutableExploreHomeUiState().apply {
        pageTitles = pages.map { it.title }
    }
    val uiState: ExploreHomeUiState = mutableUiState
    val customExplorePageProvider = provider as? ExplorePageProvider.CustomExplorePageProvider<*>
    private var rowsJob: Job? = null
    private var started = false

    fun init() {
        if (started) return
        started = true
        customExplorePageProvider?.init(viewModelScope)
        loadPage(uiState.selectedPage)
    }

    fun changePage(page: Int) {
        if (page != uiState.selectedPage) loadPage(page)
    }

    fun refresh() {
        loadPage(uiState.selectedPage)
    }

    private fun loadPage(index: Int) {
        val page = pages.getOrNull(index) ?: return
        rowsJob?.cancel()
        mutableUiState.selectedPage = index
        mutableUiState.customPage = page as? CustomExploreTapPage
        mutableUiState.explorePageBooksRawList = emptyList()
        mutableUiState.isLoading = page is ExploreTapPageDataSource
        if (page is ExploreTapPageDataSource) {
            rowsJob = viewModelScope.launch {
                try {
                    page.getRowsFlow()
                        .map { rows ->
                            rows.map { row ->
                                row.copy(bookList = row.bookList.map(textProcessingRepository::processExploreBooksRow))
                            }
                        }
                        .flowOn(Dispatchers.IO)
                        .collect {
                            mutableUiState.explorePageBooksRawList = it
                            mutableUiState.isLoading = false
                        }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Log.w("ExploreHome", "探索标签页加载失败", error)
                } finally {
                    if (isActive) mutableUiState.isLoading = false
                }
            }
        }
    }
}
