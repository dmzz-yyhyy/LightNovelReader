package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login

import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class Wenku8SessionManager @Inject constructor(
    private val userDataRepository: UserDataRepository
) {
    private val _cookies = MutableStateFlow<Map<String, String>>(emptyMap())
    val cookies: StateFlow<Map<String, String>> = _cookies.asStateFlow()

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    fun isLoggedIn(): Boolean {
        return !_sessionId.value.isNullOrBlank()
    }

    fun onLoginSuccess(cookieMap: Map<String, String>, userName: String) {
        val sessionId = cookieMap["PHPSESSID"]
        if (sessionId.isNullOrBlank()) return

        _cookies.value = cookieMap
        _sessionId.value = sessionId
        _userName.value = userName

        userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.SessionId.path).set(sessionId)
        userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.UserName.path).set(userName)
    }

    fun refreshFromStorage() {
        val storedSessionId = userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.SessionId.path).get()
        val storedUserName = userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.UserName.path).get()

        if (!storedSessionId.isNullOrBlank()) {
            _sessionId.value = storedSessionId
            _userName.value = storedUserName
            _cookies.value = mapOf("PHPSESSID" to storedSessionId)
        }
    }

    fun logout() {
        _cookies.value = emptyMap()
        _sessionId.value = null
        _userName.value = null

        userDataRepository.remove(UserDataPath.Settings.Wenku8.SessionId.path)
        userDataRepository.remove(UserDataPath.Settings.Wenku8.UserName.path)
    }
}
