package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login

import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Wenku8SessionManager @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    val sessionId = MutableStateFlow<String?>(null)
    val jieqiUserInfo = MutableStateFlow<String?>(null)
    val jieqiVisitInfo = MutableStateFlow<String?>(null)

    init {
        scope.launch {
            sessionId.value = userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.SessionId.path).getOrDefault("").ifBlank { null }
            jieqiUserInfo.value = userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.JieqiUserInfo.path).getOrDefault("").ifBlank { null }
            jieqiVisitInfo.value = userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.JieqiVisitInfo.path).getOrDefault("").ifBlank { null }
        }
    }

    suspend fun refreshFromStorage() {
        sessionId.value = userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.SessionId.path).getOrDefault("").ifBlank { null }
        jieqiUserInfo.value = userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.JieqiUserInfo.path).getOrDefault("").ifBlank { null }
        jieqiVisitInfo.value = userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.JieqiVisitInfo.path).getOrDefault("").ifBlank { null }
    }

    suspend fun logout() {
        userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.SessionId.path).set("")
        userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.JieqiUserInfo.path).set("")
        userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.JieqiVisitInfo.path).set("")
        userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.UserId.path).set("")
        userDataRepository.stringUserData(UserDataPath.Settings.Wenku8.UserName.path).set("")
        refreshFromStorage()
    }

    fun applyCookies(map: MutableMap<String, String>) {
        sessionId.value?.let { map["PHPSESSID"] = it }
        jieqiUserInfo.value?.let { map["jieqiUserInfo"] = it }
        jieqiVisitInfo.value?.let { map["jieqiVisitInfo"] = it }
    }
}
