package indi.dmzz_yyhyy.lightnovelreader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.SearchRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.RouteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private fun load(){
        viewModelScope.launch {
            searchRepository.load()
        }
        viewModelScope.launch {
            searchRepository.bookList.collect{
                if (searchRepository.bookList.value.isNotEmpty()) {
                    _uiState.update { searchUiState ->
                        searchUiState.copy(
                            isLoading = false,
                            bookList = searchRepository.bookList.value,
                            totalPage = searchRepository.totalPage.value
                        )
                    }
                }
            }
        }
    }
    private fun changePage(page: Int) {
        searchRepository.setPage(page)
        _uiState.update {
            searchUiState -> searchUiState.copy(
                isLoading = true,
                page = page
            )
        }
        load()
    }
    fun onHomePageLoad() {
        _uiState.update { searchUiState ->
            searchUiState.copy(
                isLoading = true,
                route = null,
                page = 1,
                keyword = "",
                bookList = listOf(),
                totalPage = 0
            )
        }
    }
    fun onClickSearch(searchNavController: NavController, keyword: String) {
        searchNavController.navigate(RouteConfig.SEARCH)
        searchRepository.setKeyword(keyword)
        _uiState.update { searchUiState ->
            searchUiState.copy(
                isLoading = true,
                route = RouteConfig.SEARCH,
                keyword = keyword
            )
        }
        load()
    }
}