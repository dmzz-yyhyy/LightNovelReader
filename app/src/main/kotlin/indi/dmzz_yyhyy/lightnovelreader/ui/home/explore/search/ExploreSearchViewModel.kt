package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.lightnovelreader.api.util.LocalString
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreSearchViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val bookRepository: BookRepository,
    savedStateHandle: SavedStateHandle,
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val session = SearchSession(savedStateHandle)
    private val _uiState = MutableExploreSearchUiState()
    private val searchHistoryUserData = userDataRepository.stringListUserData(UserDataPath.Search.History.path)
    private var searchTypeTipMap = mutableMapOf<String, LocalString>()
    private var searchJob: Job? = null
    private var initialized = false
    val uiState: ExploreSearchUiState = _uiState

    override fun onCleared() {
        searchJob?.cancel()
    }

    fun init(author: String? = null, navigateToSingleBook: (String) -> Unit) {
        if (initialized) return
        initialized = true
        for (type in exploreRepository.searchTypes) {
            searchTypeTipMap[type.type] = type.tip
            _uiState.searchTypeNameMap[type.type] = type.name
            _uiState.searchTypeIdList.add(type.type)
        }
        val wasSubmitted = session.submitted != null
        val request = session.initialize(
            author, exploreRepository.authorSearchType?.type, _uiState.searchTypeIdList.firstOrNull()
        )
        _uiState.keyword = session.keyword
        val selectedType = session.selectedType
        if (selectedType != null && selectedType in _uiState.searchTypeIdList) {
            changeSearchType(selectedType)
        } else {
            _uiState.isLoading = false
            _uiState.isLoadingComplete = true
            _uiState.searchBarExpanded = false
            _uiState.errorMessage = "This source does not support the requested search."
        }
        if (request != null) {
            _uiState.searchBarExpanded = false
            executeSearch(request, navigateToSingleBook, restoring = wasSubmitted)
        }
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryUserData.getFlow().collect {
                it?.let { _uiState.historyList = it.reversed() }
            }
        }
        viewModelScope.launch {
            bookshelfRepository.getAllBookshelfBookIdsFlow().collect {
                _uiState.allBookshelfBookIds = it.toMutableList()
            }
        }
    }

    fun changeSearchType(searchTypeId: String) {
        if (searchTypeId !in _uiState.searchTypeIdList) return
        session.selectedType = searchTypeId
        _uiState.searchType = searchTypeId
        _uiState.searchTip = searchTypeTipMap.getOrDefault(searchTypeId, LocalString(""))
    }

    fun deleteHistory(history: String) {
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryUserData.update {
                val newList = it.toMutableList()
                newList.remove(history)
                return@update newList
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryUserData.update { emptyList() }
        }
    }

    fun search(
        keyword: String,
        navigateToSingleBook: (bookId: String) -> Unit
    ) {
        session.keyword = keyword
        _uiState.keyword = keyword
        executeSearch(SearchRequest(_uiState.searchType, keyword), navigateToSingleBook)
    }

    private fun executeSearch(
        request: SearchRequest,
        navigateToSingleBook: (String) -> Unit,
        restoring: Boolean = false
    ) {
        val searchType = exploreRepository.searchTypes.firstOrNull { it.type == request.typeId }
        if (searchType == null) {
            _uiState.isLoading = false
            _uiState.isLoadingComplete = true
            _uiState.errorMessage = "This source does not support the requested search."
            return
        }
        session.submit(request)
        _uiState.resultKeyword = request.keyword
        _uiState.isLoading = true
        _uiState.isLoadingComplete = false
        _uiState.errorMessage = ""
        _uiState.searchResult.clear()
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            val flow = exploreRepository.search(searchType, request.keyword)
            _uiState.isLoading = false
            flow.collect {
                when(it) {
                    is SearchResult.SingleBook -> {
                        if (request.keepSingleResult || restoring) {
                            _uiState.searchResult.add(it.bookId to bookRepository.getBookInformationFlow(it.bookId))
                            _uiState.isLoadingComplete = true
                        } else launch(Dispatchers.Main) {
                            _uiState.searchBarExpanded = true
                            navigateToSingleBook(it.bookId)
                        }
                    }
                    is SearchResult.MultipleBook -> _uiState.searchResult.add(it.bookId to bookRepository.getBookInformationFlow(it.bookId))
                    is SearchResult.Error -> {
                        _uiState.isLoadingComplete = true
                        _uiState.errorMessage = it.error.message.toString()
                    }
                    is SearchResult.End -> _uiState.isLoadingComplete = true
                    is SearchResult.Empty -> {
                        _uiState.isLoadingComplete = true
                    }
                }
            }
        }
        if (!restoring) viewModelScope.launch(Dispatchers.IO) {
            searchHistoryUserData.update {
                val newList = it.toMutableList()
                if (it.contains(request.keyword))
                    newList.remove(request.keyword)
                newList.add(request.keyword)
                return@update newList
            }
        }
    }

    fun updateKeyword(keyword: String) {
        session.keyword = keyword
        _uiState.keyword = keyword
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.suggestions = exploreRepository.getSuggestions(_uiState.historyList, keyword)
        }
    }
}
