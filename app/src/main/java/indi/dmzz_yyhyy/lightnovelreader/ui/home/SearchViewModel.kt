package indi.dmzz_yyhyy.lightnovelreader.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.SearchRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.RouteConfig
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.ReaderActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private fun load() {
        viewModelScope.launch {
            searchRepository.load()
        }
        viewModelScope.launch {
            searchRepository.bookList.collect {
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
        _uiState.update { searchUiState ->
            searchUiState.copy(
                isLoading = true,
                page = page
            )
        }
        searchRepository.setPage(page)
        load()
    }

    fun onHomePageLoad() {
        _uiState.update { searchUiState ->
            searchUiState.copy(
                isLoading = true,
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
                keyword = keyword,
                page = 1,
                bookList = listOf(),
                totalPage = 0
            )
        }
        load()
    }

    fun onCardClick(bookId: Int, context: Context) {
        val intent = Intent(context, ReaderActivity::class.java)
        intent.putExtra("id", bookId)
        ContextCompat.startActivity(context, intent, Bundle())
    }

    fun onClickFistPageButton() {
        changePage(1)
    }

    fun onClickBeforePageButton() {
        if (_uiState.value.page > 1) {
            changePage(_uiState.value.page - 1)
        }
    }

    fun onClickNextPageButton() {
        if (_uiState.value.page < searchRepository.totalPage.value) {
            changePage(_uiState.value.page + 1)
        }
    }

    fun onClickLastPageButton() {
        changePage(searchRepository.totalPage.value)
    }
}