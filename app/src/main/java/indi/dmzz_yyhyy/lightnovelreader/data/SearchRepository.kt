package indi.dmzz_yyhyy.lightnovelreader.data

import dagger.hilt.android.scopes.ActivityRetainedScoped
import indi.dmzz_yyhyy.lightnovelreader.data.local.SearchType
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Information
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.SearchBooks
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class SearchRepository @Inject constructor(
    private val webDataSource: WebDataSource
){
    private var _searchType: String = SearchType.articleName
    private var _keyword: String = ""
    private var _page: Int = 1
    private var _webSearchData: SearchBooks? = null
    private var _bookList: MutableStateFlow<List<Information>> = MutableStateFlow(listOf())
    private var _totalPage: MutableStateFlow<Int> = MutableStateFlow(0)
    private var _localBookListList: MutableMap<Int, SearchBooks?> = mutableMapOf()

    val bookList: StateFlow<List<Information>> get() = _bookList
    val totalPage: StateFlow<Int> get() = _totalPage




    suspend fun load(){
        _page = 1
        _bookList.value = listOf()
        _totalPage.value = 0
        _localBookListList = mutableMapOf()
        if (_localBookListList[_page] == null) {
            _webSearchData = webDataSource.searchBook(_searchType, _keyword, _page)
            _localBookListList[_page] = _webSearchData
        } else {
            _webSearchData = _localBookListList[_page]
        }
        if (_webSearchData != null) {
            _totalPage.value = _webSearchData!!.totalPage
            _bookList.value = _webSearchData!!.searchBooks
        }
    }

    fun setSearchType(searchType: String) {
        _searchType = searchType
    }

    fun setKeyword(keyword: String) {
        _keyword = keyword
    }

    fun setPage(page: Int) {
        _page = page
    }


}