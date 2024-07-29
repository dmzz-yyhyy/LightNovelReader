package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ExplorationRepository
import indi.dmzz_yyhyy.lightnovelreader.data.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class ExplorationSearchViewModel @Inject constructor(
    private val explorationRepository: ExplorationRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val _uiState = MutableExplorationSearchUiState()
    private val searchHistoryUserData = userDataRepository.stringListUserData(UserDataPath.Search.History.path)
    private var searchTypeMap = emptyMap<String, String>()
    private var searchTypeTipMap = emptyMap<String, String>()
    val uiState: ExplorationSearchUiState = _uiState

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            searchTypeMap = explorationRepository.getSearchTypeMap()
            searchTypeTipMap = explorationRepository.getSearchTipMap()
            _uiState.searchTypeNameList = explorationRepository.getSearchTypeNameList().toMutableList()
            _uiState.searchType = explorationRepository.getSearchTypeNameList().getOrNull(0)
                ?.let {
                    searchTypeMap.getOrDefault(it, "")
                } ?: ""
            _uiState.searchTip = searchTypeTipMap.getOrDefault(_uiState.searchType, "")
            searchHistoryUserData.getFlow().collect {
                it?.let {
                    _uiState.historyList = it.reversed().toMutableList()
                }
            }
        }
    }

    fun changeSearchType(searchTypeName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.searchType = searchTypeMap.getOrDefault(searchTypeName, "")
            _uiState.searchTip = searchTypeTipMap.getOrDefault(_uiState.searchType, "")
        }
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

    fun search(keyword: String) {
        _uiState.isLoading = true
        _uiState.isLoadingComplete = false
        _uiState.searchResult = mutableListOf()
        explorationRepository.stopAllSearch()
        viewModelScope.launch(Dispatchers.IO) {
            explorationRepository.search(_uiState.searchType, keyword).collect {
                _uiState.isLoading = false
                if (it.isNotEmpty() && it.last().isEmpty()) {
                    _uiState.isLoadingComplete = true
                    _uiState.searchResult = it.dropLast(1).toMutableList()
                    return@collect
                }
                _uiState.searchResult = it.toMutableList()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryUserData.update {
                val newList = it.toMutableList()
                if (it.contains(keyword))
                    newList.remove(keyword)
                newList.add(keyword)
                return@update newList
            }
        }
    }
}
