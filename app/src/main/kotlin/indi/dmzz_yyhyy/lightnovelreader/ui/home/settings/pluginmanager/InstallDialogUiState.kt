package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
interface PluginInstallerDialogUiState {
    var mode: PluginDialogMode

    var installMessage: String
    var installProgress: Float?
    var installInfo: PluginInstallInfo?
    var installStep: InstallStepState
    var installCompletedSuccess: Boolean
    var installCompletedMessage: String
    var installDecision: InstallDecision?

    var uninstallPluginId: String
    var uninstallPluginName: String
    var uninstallStep: DeleteStepState
    var uninstallMessage: String
    var uninstallCompletedSuccess: Boolean
    var uninstallCompletedMessage: String

    var updatePluginId: String
    var updatePluginName: String
    var updateStep: UpdateStepState
    var updateMessage: String
    var updateDownloadProgress: Float?

    var toast: String
    var closeSignal: Int
}

enum class PluginDialogMode {
    Hidden,
    Install,
    Uninstall,
    UpdateCheck
}

enum class InstallStepState {
    Working,
    AwaitingDecision,
    Completed
}

enum class DeleteStepState {
    Confirming,
    Working,
    Completed
}

enum class UpdateStepState {
    Checking,
    Latest,
    Available,
    Error,
    Downloading,
    Installing,
    Completed
}

@Stable
class MutablePluginInstallerDialogUiState : PluginInstallerDialogUiState {
    override var mode by mutableStateOf(PluginDialogMode.Hidden)

    override var installMessage by mutableStateOf("")
    override var installProgress: Float? by mutableStateOf(null)
    override var installInfo: PluginInstallInfo? by mutableStateOf(null)
    override var installStep by mutableStateOf(InstallStepState.Working)
    override var installCompletedSuccess by mutableStateOf(false)
    override var installCompletedMessage by mutableStateOf("")
    override var installDecision: InstallDecision? by mutableStateOf(null)

    override var uninstallPluginId by mutableStateOf("")
    override var uninstallPluginName by mutableStateOf("")
    override var uninstallStep by mutableStateOf(DeleteStepState.Working)
    override var uninstallMessage by mutableStateOf("")
    override var uninstallCompletedSuccess by mutableStateOf(false)
    override var uninstallCompletedMessage by mutableStateOf("")

    override var updatePluginId by mutableStateOf("")
    override var updatePluginName by mutableStateOf("")
    override var updateStep by mutableStateOf(UpdateStepState.Checking)
    override var updateMessage by mutableStateOf("")
    override var updateDownloadProgress: Float? by mutableStateOf(null)

    override var toast by mutableStateOf("")
    override var closeSignal by mutableIntStateOf(0)
}

@Stable
data class PluginInstallInfo(
    val packageName: String,
    val name: String,
    val versionName: String
)

@Stable
data class InstallDecision(
    val type: InstallDecisionType,
    val message: String
)

enum class InstallDecisionType {
    Reinstall,
    Downgrade,
    InvalidSignature
}
