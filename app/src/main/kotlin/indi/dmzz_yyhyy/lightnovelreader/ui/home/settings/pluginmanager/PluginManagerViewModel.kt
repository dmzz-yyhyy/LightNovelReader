package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginAppInfo
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginMetadata
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.store.PluginUpdateCheckRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureInfo
import indi.dmzz_yyhyy.lightnovelreader.utils.getApkSignatures
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PluginManagerViewModel @Inject constructor(
    val pluginManager: PluginManager,
    val userDataRepository: UserDataRepository,
    val pluginUpdateCheckRepository: PluginUpdateCheckRepository,
) : ViewModel() {

    private val enabledPluginUserData = userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)
    private val disenabledPlugin = mutableSetOf<String>()
    val enabledPluginFlow = enabledPluginUserData.getFlowWithDefault(emptyList())

    val pluginList: List<PluginMetadata> = pluginManager.allPluginList
    val errorMessageMap: Map<String, String> = pluginManager.errorPluginMap
    val scannedPluginApps: List<PluginAppInfo> = pluginManager.appPluginInfos

    val pluginUpdates get() = pluginUpdateCheckRepository.updates

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    fun onClickEnabledSwitch(pluginInfo: PluginMetadata) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = enabledPluginUserData.getOrDefault(emptyList())
            if (currentList.contains(pluginInfo.packageName)) {
                enabledPluginUserData.set(currentList - pluginInfo.packageName)
                disenabledPlugin.add(pluginInfo.packageName)
            } else {
                enabledPluginUserData.set(currentList + pluginInfo.packageName)
                disenabledPlugin.remove(pluginInfo.packageName)
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

    fun getPluginFile(packageName: String): File =
        pluginManager.getPluginFile(pluginManager.getPluginDir(packageName))

    fun unloadAllDisenablePlugin() {
        for (packageName in disenabledPlugin) {
            pluginManager.loadedPluginMap[packageName]?.onUnload()
        }
    }

    @Composable
    fun PluginContent(id: String, paddingValues: PaddingValues) {
        pluginManager.PluginContent(id, paddingValues)
    }
}
