package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.install.InstallState
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.install.PluginInstallError
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.DeleteStepState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallDecision
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallDecisionType
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallStepState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.MutablePluginInstallerDialogUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginDialogMode
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginInstallInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginInstallerDialogUiState
import io.nightfish.lightnovelreader.api.ApiMetadata
import jakarta.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@HiltViewModel
class PluginInstallerDialogViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pluginManager: PluginManager
) : ViewModel() {
    companion object {
        private const val TAG = "PluginInstaller"
    }

    private val mutableUiState = MutablePluginInstallerDialogUiState()
    val uiState: PluginInstallerDialogUiState = mutableUiState

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    private var currentJob: Job? = null
    private var userDecisionDeferred: CompletableDeferred<Boolean>? = null

    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is PluginInstallError.PluginNotSupport -> context.getString(
                R.string.plugin_error_unsupported_api,
                throwable.pluginUsedApiVersion,
                ApiMetadata.API_VERSION
            )
            is PluginInstallError.AppPluginExist ->  context.getString(
                R.string.plugin_error_app_plugin_exist
            )

            is PluginInstallError.CurrentPluginVersionTooHighError -> context.getString(
                R.string.plugin_error_version_too_high
            )
            is PluginInstallError.PluginSignatureNotMatchError -> context.getString(
                R.string.plugin_error_signature_mismatch
            )
            else -> throwable.localizedMessage
                ?: throwable.message
                ?: context.getString(R.string.plugin_error_unknown, throwable.toString())
        }
    }

    fun setSource(source: String) {
        when {
            source.startsWith("uninstall:") -> {
                val packageName = source.removePrefix("uninstall:")
                startUninstall(packageName)
            }
            else -> {
                startInstall(source)
            }
        }
    }

    private fun startInstall(uriString: String) {
        mutableUiState.mode = PluginDialogMode.Install
        mutableUiState.installStep = InstallStepState.Working
        mutableUiState.installMessage = context.getString(R.string.plugin_install_preparing)
        mutableUiState.installCompletedSuccess = false
        mutableUiState.installCompletedMessage = ""
        mutableUiState.installInfo = null
        mutableUiState.installProgress = null
        mutableUiState.installDecision = null

        currentJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val uri = uriString.toUri()
                val tempFile = copyUriToTempFile(uri) ?: run {
                    withContext(Dispatchers.Main) {
                        mutableUiState.installStep = InstallStepState.Completed
                        mutableUiState.installCompletedSuccess = false
                        mutableUiState.installCompletedMessage = context.getString(R.string.plugin_install_read_failed)
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    mutableUiState.installMessage = context.getString(R.string.plugin_install_parse_package_info)
                }
                val packageInfo = context.packageManager.getPackageArchiveInfo(tempFile.path, 0)
                if (packageInfo == null) {
                    tempFile.delete()
                    withContext(Dispatchers.Main) {
                        mutableUiState.installStep = InstallStepState.Completed
                        mutableUiState.installCompletedSuccess = false
                        mutableUiState.installCompletedMessage = context.getString(R.string.plugin_install_read_failed)
                    }
                    return@launch
                }
                val appInfo = packageInfo.applicationInfo
                val pluginLabel = appInfo?.loadLabel(context.packageManager)?.toString()
                    ?: packageInfo.packageName
                val pluginIcon = appInfo?.let {
                    it.sourceDir = tempFile.path
                    it.publicSourceDir = tempFile.path
                    it.loadIcon(context.packageManager)
                }
                withContext(Dispatchers.Main) {
                    mutableUiState.installInfo = PluginInstallInfo(
                        packageName = packageInfo.packageName,
                        name = pluginLabel,
                        versionName = packageInfo.versionName ?: "",
                        icon = pluginIcon
                    )
                    mutableUiState.installDecision = InstallDecision(
                        type = InstallDecisionType.ConfirmInstall,
                        message = context.getString(R.string.plugin_install_confirm_body, pluginLabel)
                    )
                    mutableUiState.installStep = InstallStepState.AwaitingDecision
                }

                val deferred = CompletableDeferred<Boolean>()
                userDecisionDeferred = deferred
                val confirmed = deferred.await()
                userDecisionDeferred = null

                if (!confirmed) {
                    tempFile.delete()
                    withContext(Dispatchers.Main) {
                        onCloseDialog()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    mutableUiState.installStep = InstallStepState.Working
                    mutableUiState.installDecision = null
                    mutableUiState.installMessage = context.getString(R.string.plugin_install_preparing)
                }

                pluginManager.installPlugin(tempFile).collect { state ->
                    withContext(Dispatchers.Main) {
                        when (state) {
                            is InstallState.Start -> {
                                mutableUiState.installMessage = context.getString(state.strId)
                            }
                            is InstallState.Info -> {
                                mutableUiState.installInfo = PluginInstallInfo(
                                    packageName = state.packageName,
                                    name = state.name,
                                    versionName = state.versionName
                                )
                            }
                            is InstallState.Completed -> {
                                mutableUiState.installStep = InstallStepState.Completed
                                mutableUiState.installCompletedSuccess = true
                                mutableUiState.installCompletedMessage = context.getString(R.string.plugin_install_completed)
                            }
                            is InstallState.Error -> {
                                Log.e(TAG, "Plugin install failed", state.result)
                                mutableUiState.installStep = InstallStepState.Completed
                                mutableUiState.installCompletedSuccess = false
                                mutableUiState.installCompletedMessage = context.getString(
                                    R.string.plugin_install_failed_with_reason,
                                    getErrorMessage(state.result)
                                )
                            }
                        }
                    }
                }

                tempFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Plugin install unexpected error", e)
                withContext(Dispatchers.Main) {
                    mutableUiState.installStep = InstallStepState.Completed
                    mutableUiState.installCompletedSuccess = false
                    mutableUiState.installCompletedMessage = context.getString(
                        R.string.plugin_install_failed_with_reason,
                        getErrorMessage(e)
                    )
                }
            }
        }
    }

    private fun startUninstall(packageName: String) {
        mutableUiState.mode = PluginDialogMode.Uninstall
        mutableUiState.uninstallPluginId = packageName
        mutableUiState.uninstallPluginName = pluginManager.allPluginList
            .firstOrNull { it.packageName == packageName }?.name ?: packageName
        mutableUiState.uninstallStep = DeleteStepState.Confirming
        mutableUiState.uninstallMessage = context.getString(R.string.plugin_delete_confirm_body)
        mutableUiState.uninstallCompletedSuccess = false
        mutableUiState.uninstallCompletedMessage = ""
    }

    fun confirmDelete() {
        val packageName = mutableUiState.uninstallPluginId
        mutableUiState.uninstallStep = DeleteStepState.Working
        mutableUiState.uninstallMessage = context.getString(R.string.plugin_delete_deleting)

        currentJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                pluginManager.deletePlugin(packageName)
                withContext(Dispatchers.Main) {
                    mutableUiState.uninstallStep = DeleteStepState.Completed
                    mutableUiState.uninstallCompletedSuccess = true
                    mutableUiState.uninstallCompletedMessage = context.getString(R.string.plugin_delete_completed)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Plugin delete failed", e)
                withContext(Dispatchers.Main) {
                    mutableUiState.uninstallStep = DeleteStepState.Completed
                    mutableUiState.uninstallCompletedSuccess = false
                    mutableUiState.uninstallCompletedMessage = context.getString(
                        R.string.plugin_delete_failed_with_reason,
                        getErrorMessage(e)
                    )
                }
            }
        }
    }

    private fun copyUriToTempFile(uri: Uri): File? {
        return try {
            val tempDir = pluginManager.pluginsTempDir.also { it.mkdirs() }
            val tempFile = File(tempDir, "install_${System.currentTimeMillis()}.apk")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (uri.scheme == "file") {
                val sourceFile = File(uri.path ?: "")
                if (sourceFile.canonicalPath.startsWith(pluginManager.pluginsTempDir.canonicalPath)) {
                    sourceFile.delete()
                }
            }
            tempFile.setReadOnly()
            tempFile
        } catch (_: Exception) {
            null
        }
    }

    fun onCancelOperation() {
        currentJob?.cancel()
        onCloseDialog()
    }

    fun respondUserDecision(confirm: Boolean) {
        val deferred = userDecisionDeferred
        if (deferred != null) {
            userDecisionDeferred = null
            deferred.complete(confirm)
        } else if (!confirm) {
            onCancelOperation()
        }
    }

    fun onCloseDialog() {
        mutableUiState.closeSignal++
    }
}
