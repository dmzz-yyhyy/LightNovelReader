package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login.Wenku8LoginRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login.Wenku8LoginResult
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login.Wenku8SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Wenku8LoginUiState(
    val isLoading: Boolean = false,
    val error: Wenku8LoginResult.Failure? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class Wenku8LoginViewModel @Inject constructor(
    private val repository: Wenku8LoginRepository,
    private val sessionManager: Wenku8SessionManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(Wenku8LoginUiState())
    val uiState: StateFlow<Wenku8LoginUiState> = _uiState

    fun login(account: String, password: String) {
        _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
        viewModelScope.launch {
            val result = repository.login(account, password)
            _uiState.update {
                when (result) {
                    is Wenku8LoginResult.Success -> {
                        viewModelScope.launch {
                            sessionManager.onLoginSuccess(
                                cookieMap = result.cookieMap,
                                userName = result.userName
                            )
                        }
                        it.copy(isLoading = false, isSuccess = true)
                    }
                    is Wenku8LoginResult.Failure -> it.copy(isLoading = false, error = result)
                }
            }
        }
    }

    fun reset() { _uiState.value = Wenku8LoginUiState() }
}
