package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.installed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.installed.model.InstalledExtensionItem
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.installed.model.InstalledUI
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstalledViewModel @Inject constructor(
    private val repositoryService: RepositoryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstalledUI())
    val uiState: StateFlow<InstalledUI> = _uiState.asStateFlow()

    private val _uninstallingExtensions = MutableStateFlow<Set<Int>>(emptySet())

    init {
        loadInstalledExtensions()
    }

    private fun loadInstalledExtensions() {
        viewModelScope.launch {
            combine(
                repositoryService.getAllInstalledExtensions(),
                _uninstallingExtensions
            ) { installedExtensions, uninstallingExtensions ->
                val installedItems = installedExtensions.map { extension ->
                    InstalledExtensionItem(
                        extension = extension,
                        isUninstalling = uninstallingExtensions.contains(extension.id)
                    )
                }

                InstalledUI(
                    extensions = installedItems,
                    isLoading = false,
                    error = null
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

    fun uninstallExtension(extension: InstalledExtensionEntity) {
        viewModelScope.launch {
            try {
                _uninstallingExtensions.value = _uninstallingExtensions.value + extension.id
                repositoryService.uninstallExtension(extension)
                // Extension will be automatically removed via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to uninstall extension"
                )
            } finally {
                _uninstallingExtensions.value = _uninstallingExtensions.value - extension.id
            }
        }
    }

    fun toggleExtensionEnabled(extension: InstalledExtensionEntity) {
        viewModelScope.launch {
            try {
                val updatedExtension = extension.copy(isEnabled = !extension.isEnabled)
                repositoryService.updateInstalledExtension(updatedExtension)
                // Extension will be automatically updated via the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update extension"
                )
            }
        }
    }
}
