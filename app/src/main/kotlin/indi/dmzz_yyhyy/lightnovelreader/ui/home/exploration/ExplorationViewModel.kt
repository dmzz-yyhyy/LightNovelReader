package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ExplorationRepository
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ExplorationViewModel @Inject constructor(
    private val explorationRepository: ExplorationRepository
) : ViewModel() {
    private val _uiState = MutableExplorationUiState()
    val uiState: ExplorationUiState = _uiState

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
