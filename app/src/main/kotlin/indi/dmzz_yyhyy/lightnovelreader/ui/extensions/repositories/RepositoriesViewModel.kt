package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.repositories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryInitializer
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepositoryEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.repositories.model.RepositoriesUI
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class RepositoriesViewModel @Inject constructor(
    private val repositoryService: RepositoryService,
    private val repositoryInitializer: RepositoryInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepositoriesUI())
    val uiState: StateFlow<RepositoriesUI> = _uiState.asStateFlow()

    init {
        loadRepositories()
    }

    private fun loadRepositories() {
        viewModelScope.launch {
            repositoryService.getAllRepositories()
                .map { repositories ->
                    RepositoriesUI(
                        repositories = repositories,
                        isLoading = false,
                        error = null
                    )
                }
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Unknown error",
                        isLoading = false
                    )
                }
                .collect { uiState ->
                    _uiState.value = uiState
                }
        }
    }

    fun addRepository(name: String, url: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repositoryService.addRepository(name, url)
                // Repository will be automatically loaded via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add repository",
                    isLoading = false
                )
            }
        }
    }

    fun refreshRepository(repository: RepositoryEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repositoryService.refreshRepository(repository)
                // Repository will be automatically loaded via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to refresh repository",
                    isLoading = false
                )
            }
        }
    }

    fun deleteRepository(repository: RepositoryEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repositoryService.deleteRepository(repository)
                // Repository will be automatically loaded via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete repository",
                    isLoading = false
                )
            }
        }
    }

    fun toggleRepositoryEnabled(repository: RepositoryEntity) {
        viewModelScope.launch {
            try {
                val updatedRepository = repository.copy(isEnabled = !repository.isEnabled)
                repositoryService.updateRepository(updatedRepository)
                // Repository will be automatically loaded via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update repository",
                    isLoading = false
                )
            }
        }
    }

    fun initializeDefaultRepositories() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repositoryInitializer.initializeDefaultRepositories()
                // Repositories will be automatically loaded via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to initialize default repositories",
                    isLoading = false
                )
            }
        }
    }

    fun refreshAllRepositories() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val repositories = repositoryService.getAllRepositories().first()
                repositories.forEach { repository ->
                    if (repository.isEnabled) {
                        try {
                            repositoryService.refreshRepository(repository)
                        } catch (e: Exception) {
                            // Log error but continue with other repositories
                            e.printStackTrace()
                        }
                    }
                }
                // Repositories will be automatically loaded via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to refresh repositories",
                    isLoading = false
                )
            }
        }
    }
}
