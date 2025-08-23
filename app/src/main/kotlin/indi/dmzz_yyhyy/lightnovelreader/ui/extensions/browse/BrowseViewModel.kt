package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.browse.model.BrowseExtensionItem
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.browse.model.BrowseUI
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repositoryService: RepositoryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUI())
    val uiState: StateFlow<BrowseUI> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _installingExtensions = MutableStateFlow<Set<Int>>(emptySet())

    init {
        loadExtensions()
        observeSearchQuery()
    }

    private fun loadExtensions() {
        viewModelScope.launch {
            combine(
                repositoryService.getAllExtensions(),
                repositoryService.getAllInstalledExtensions(),
                _searchQuery,
                _installingExtensions
            ) { extensions, installedExtensions, searchQuery, installingExtensions ->
                val filteredExtensions = if (searchQuery.isBlank()) {
                    extensions
                } else {
                    extensions.filter { extension ->
                        extension.name.contains(searchQuery, ignoreCase = true) ||
                        extension.description.contains(searchQuery, ignoreCase = true) ||
                        extension.lang.contains(searchQuery, ignoreCase = true)
                    }
                }

                val browseItems = filteredExtensions.map { extension ->
                    val installedExtension = installedExtensions.find { it.id == extension.id }
                    val isInstalled = installedExtension != null
                    val isUpdateAvailable = if (isInstalled && installedExtension != null) {
                        compareVersions(extension.version, installedExtension.version) > 0
                    } else false

                    BrowseExtensionItem(
                        extension = extension,
                        isInstalled = isInstalled,
                        installedVersion = installedExtension?.version,
                        isUpdateAvailable = isUpdateAvailable,
                        updateVersion = if (isUpdateAvailable) extension.version else null,
                        isInstalling = installingExtensions.contains(extension.id)
                    )
                }

                BrowseUI(
                    extensions = browseItems,
                    isLoading = false,
                    error = null,
                    searchQuery = searchQuery
                )
            }.catch { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "Unknown error",
                    isLoading = false
                )
            }.collect { uiState ->
                _uiState.value = uiState
            }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery.collect {
                // Search query changes will trigger the combine flow above
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun installExtension(extension: ExtensionEntity) {
        viewModelScope.launch {
            try {
                _installingExtensions.value = _installingExtensions.value + extension.id
                repositoryService.installExtension(extension)
                // Extension will be automatically loaded via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to install extension"
                )
            } finally {
                _installingExtensions.value = _installingExtensions.value - extension.id
            }
        }
    }

    fun updateExtension(extension: ExtensionEntity) {
        // For now, treat update as install
        installExtension(extension)
    }

    private fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(parts1.size, parts2.size)
        
        for (i in 0 until maxLength) {
            val part1 = if (i < parts1.size) parts1[i] else 0
            val part2 = if (i < parts2.size) parts2[i] else 0
            
            when {
                part1 > part2 -> return 1
                part1 < part2 -> return -1
            }
        }
        
        return 0
    }
}
