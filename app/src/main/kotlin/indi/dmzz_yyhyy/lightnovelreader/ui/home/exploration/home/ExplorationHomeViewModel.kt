package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.ExplorationRepository
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class ExplorationHomeViewModel @Inject constructor(
    private val explorationRepository: ExplorationRepository
) : ViewModel() {
    private val _uiState = MutableExplorationHomeUiState()
    private var workingExplorationPageJob: Job? = null
    private var workingExplorationBooksRowsJob: Job? = null
    val uiState: ExplorationHomeUiState = _uiState

    fun init() {
        changePage(_uiState.selectedPage)
    }

    fun changePage(page: Int) {
        workingExplorationPageJob?.cancel()
        workingExplorationBooksRowsJob?.cancel()
        _uiState.selectedPage = page
        workingExplorationPageJob = viewModelScope.launch {
            val explorationPageMap = explorationRepository.getExplorationPageMap()
            _uiState.pageTitles = explorationRepository.getExplorationPageTitleList().toMutableList()
            workingExplorationBooksRowsJob = viewModelScope.launch {
                explorationPageMap[_uiState.pageTitles[page]]?.getExplorationPage()?.let { explorationPage ->
                    _uiState.explorationPageTitle = explorationPage.title
                    explorationPage.rows.collect {
                        _uiState.explorationPageBooksRawList = it.toMutableList()
                    }
                }
            }
        }
    }
}
