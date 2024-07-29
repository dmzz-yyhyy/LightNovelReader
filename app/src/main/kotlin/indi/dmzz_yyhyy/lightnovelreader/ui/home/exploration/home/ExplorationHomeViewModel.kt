package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ExplorationRepository
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ExplorationHomeViewModel @Inject constructor(
    private val explorationRepository: ExplorationRepository
) : ViewModel() {
    private val _uiState = MutableExplorationHomeUiState()
    val uiState: ExplorationHomeUiState = _uiState

    fun init() {
        changePage(0)
    }

    fun changePage(page: Int) {
        _uiState.isLoading = true
        _uiState.selectedPage = page
        viewModelScope.launch {
            val explorationPageMap = explorationRepository.getExplorationPageMap()
            _uiState.pageTitles = explorationRepository.getExplorationPageTitleList().toMutableList()
            viewModelScope.launch {
                explorationPageMap[_uiState.pageTitles[page]]?.getExplorationPage()?.collect {
                    _uiState.explorationPage = it
                    _uiState.isLoading = it.rows.isEmpty()
                }
            }
        }
    }
}
