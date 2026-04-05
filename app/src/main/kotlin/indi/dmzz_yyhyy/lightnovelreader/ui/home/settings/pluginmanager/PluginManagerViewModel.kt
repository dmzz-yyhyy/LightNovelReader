package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginAppInfo
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginMetadata
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureInfo
import indi.dmzz_yyhyy.lightnovelreader.utils.getApkSignatures
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PluginManagerViewModel @Inject constructor(
    val pluginManager: PluginManager,
    val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val enabledPluginUserData = userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)
    val enabledPluginFlow = enabledPluginUserData.getFlowWithDefault(emptyList())

    val pluginList: List<PluginMetadata> = pluginManager.allPluginList
    val errorMessageMap: Map<String, String> = pluginManager.errorPluginMap
    val scannedPluginApps: List<PluginAppInfo> = pluginManager.appPluginInfos

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    fun onClickEnabledSwitch(pluginInfo: PluginMetadata) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = enabledPluginUserData.getOrDefault(emptyList())
            if (currentList.contains(pluginInfo.packageName)) {
                enabledPluginUserData.set(currentList - pluginInfo.packageName)
                pluginManager.loadedPluginMap[pluginInfo.packageName]?.onUnload()
            } else {
                enabledPluginUserData.set(currentList + pluginInfo.packageName)
                pluginManager.loadPlugin(pluginInfo.packageName)
            }
        }
    }

    fun deletePlugin(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            pluginManager.deletePlugin(packageName)
        }
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch(Dispatchers.Main) { _snackbarFlow.emit(message) }
    }

    fun getPluginSignatures(packageName: String): List<ApkSignatureInfo>? {
        val pluginDir = pluginManager.getPluginDir(packageName)
        val pluginFile = pluginManager.getPluginFile(pluginDir)
        if (!pluginFile.exists()) return null
        return getApkSignatures(pluginFile)
    }

    @Composable
    fun PluginContent(id: String, paddingValues: PaddingValues) {
        pluginManager.PluginContent(id, paddingValues)
    }
}
