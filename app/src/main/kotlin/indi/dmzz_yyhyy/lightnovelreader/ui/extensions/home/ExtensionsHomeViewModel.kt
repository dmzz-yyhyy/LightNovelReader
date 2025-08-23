package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.home.model.ExtensionsHomeUI
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExtensionsHomeViewModel @Inject constructor(
    private val repositoryService: RepositoryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExtensionsHomeUI())
    val uiState: StateFlow<ExtensionsHomeUI> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repositoryService.getAllRepositories(),
                repositoryService.getAllExtensions(),
                repositoryService.getAllInstalledExtensions()
            ) { repositories, extensions, installedExtensions ->
                ExtensionsHomeUI(
                    repositoryCount = repositories.size,
                    availableExtensionsCount = extensions.size,
                    installedExtensionsCount = installedExtensions.size,
                    isLoading = false
                )
            }.collect { uiState ->
                _uiState.value = uiState
            }
        }
    }
}
