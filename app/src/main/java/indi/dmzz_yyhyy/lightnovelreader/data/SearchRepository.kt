package indi.dmzz_yyhyy.lightnovelreader.data

import dagger.hilt.android.scopes.ActivityRetainedScoped
import indi.dmzz_yyhyy.lightnovelreader.api.LightNovelReaderAPI
import indi.dmzz_yyhyy.lightnovelreader.data.book.Search
import indi.dmzz_yyhyy.lightnovelreader.data.book.SearchBook
import indi.dmzz_yyhyy.lightnovelreader.data.local.SearchType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ActivityRetainedScoped
class SearchRepository @Inject constructor(
    private val lightNovelReaderAPI: LightNovelReaderAPI,
    private val ioDispatcher: CoroutineDispatcher
){
    private var _searchType: String = SearchType.articleName
    private var _keyword: String = ""
    private var _page: Int = 1
    private var _search: Search? = null
    private var _bookList: MutableStateFlow<List<SearchBook>> = MutableStateFlow(listOf())
    private var _totalPage: MutableStateFlow<Int> = MutableStateFlow(0)
    private var _localBookListList: MutableMap<Int, Search?> = mutableMapOf()

    val bookList: StateFlow<List<SearchBook>> get() = _bookList
    val totalPage: StateFlow<Int> get() = _totalPage


    private suspend fun searchBook(searchType: String, keyword: String, page: Int): Search? =
        withContext(ioDispatcher) {
            lightNovelReaderAPI.searchBook(searchType, keyword, page)
        }

    suspend fun load(){
        if (_localBookListList[_page] == null) {
            _search = searchBook(_searchType, _keyword, _page)
            _localBookListList[_page] = _search
        } else {
            _search = _localBookListList[_page]
        }
        if (_search != null) {
            _totalPage.value = _search!!.totalPage
            _bookList.value = _search!!.searchBookList
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