package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.exploration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionReadingService
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.exploration.model.ExtensionExplorationHomeUI
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.exploration.model.ExtensionExplorationUI
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.exploration.model.ExtensionInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExtensionExplorationViewModel @Inject constructor(
    private val extensionReadingService: ExtensionReadingService,
    private val repositoryService: RepositoryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExtensionExplorationUI())
    val uiState: StateFlow<ExtensionExplorationUI> = _uiState.asStateFlow()

    private val _homeUIState = MutableStateFlow(ExtensionExplorationHomeUI())
    val homeUIState: StateFlow<ExtensionExplorationHomeUI> = _homeUIState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedExtensionId = MutableStateFlow<String?>(null)

    init {
        loadAvailableExtensions()
        observeSearchAndExtension()
    }

    private fun loadAvailableExtensions() {
        viewModelScope.launch {
            try {
                _homeUIState.value = _homeUIState.value.copy(isLoading = true)
                val enabledExtensions = extensionReadingService.getEnabledExtensions()
                val extensionInfos = enabledExtensions.map { extension ->
                    ExtensionInfo(
                        id = extension.id,
                        name = extension.name,
                        description = extension.description,
                        isEnabled = true
                    )
                }
                
                _homeUIState.value = _homeUIState.value.copy(
                    availableExtensions = extensionInfos,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _homeUIState.value = _homeUIState.value.copy(
                    error = e.message ?: "Failed to load extensions",
                    isLoading = false
                )
            }
        }
    }

    private fun observeSearchAndExtension() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _selectedExtensionId
            ) { searchQuery, selectedExtensionId ->
                if (searchQuery.isNotBlank()) {
                    searchBooks(searchQuery)
                } else {
                    _uiState.value = _uiState.value.copy(
                        books = emptyList(),
                        searchQuery = searchQuery,
                        selectedExtensionId = selectedExtensionId
                    )
                }
            }.collect()
        }
    }

    private suspend fun searchBooks(query: String) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val books = extensionReadingService.searchBooks(query)
            _uiState.value = _uiState.value.copy(
                books = books,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Failed to search books",
                isLoading = false
            )
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedExtension(extensionId: String?) {
        _selectedExtensionId.value = extensionId
    }

    fun refreshExtensions() {
        loadAvailableExtensions()
    }
}
