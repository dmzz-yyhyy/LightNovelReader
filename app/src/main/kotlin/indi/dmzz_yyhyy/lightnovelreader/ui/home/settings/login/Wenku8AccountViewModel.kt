package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login.Wenku8SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Wenku8AccountState(
    val loggedIn: Boolean = false,
    val userName: String? = null,
)

@HiltViewModel
class Wenku8AccountViewModel @Inject constructor(
    private val sessionManager: Wenku8SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow(Wenku8AccountState())
    val state: StateFlow<Wenku8AccountState> = _state

    init {
        viewModelScope.launch {
            // Ensure session is loaded from storage first.
            sessionManager.refreshFromStorage()

            // Combine the session ID and username flows to create a single UI state.
            combine(sessionManager.sessionId, sessionManager.userName) { id, name ->
                Wenku8AccountState(
                    loggedIn = !id.isNullOrBlank(),
                    userName = name
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
            // The state will automatically update via the collector in the init block.
        }
    }
}
