package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.settings.ExtensionSettingsManager
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionSettingEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExtensionSettingsViewModel @Inject constructor(
    private val settingsManager: ExtensionSettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExtensionSettingsUI())
    val uiState: StateFlow<ExtensionSettingsUI> = _uiState.asStateFlow()

    private var currentExtensionId: Int = -1

    fun loadSettings(extensionId: Int) {
        currentExtensionId = extensionId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                settingsManager.getExtensionSettings(extensionId)
                    .collect { settings ->
                        _uiState.value = _uiState.value.copy(
                            settings = settings.filter { it.isVisible },
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load settings",
                    isLoading = false
                )
            }
        }
    }

    fun updateSetting(key: String, value: String) {
        if (currentExtensionId == -1) return
        
        viewModelScope.launch {
            try {
                settingsManager.updateSetting(currentExtensionId, key, value)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update setting"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ExtensionSettingsUI(
    val settings: List<ExtensionSettingEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
