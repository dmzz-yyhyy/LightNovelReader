package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginStoreInstall
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.applist.navigateToSettingsPluginAppListDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.applist.settingsPluginAppListDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.navigateToSettingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.settingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.restart
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsPluginManagerNavigation() {
    settingsPluginManagerHomeDestination()
    settingsPluginAppListDestination()
    settingsPluginManagerDetailDestination()
}

fun NavEntryScope.settingsPluginManagerHomeDestination() {
    entry<Route.Main.Settings.PluginManager.Home> {
        val navigator = LocalNavigator.current
        val context = LocalContext.current
        val viewModel = activityHiltViewModel<PluginManagerViewModel>()
        val enabledPluginList by viewModel.enabledPluginFlow.collectAsStateWithLifecycle(emptyList())
        val errorMessageMap = viewModel.errorMessageMap
        val pluginUpdates by viewModel.pluginUpdates.collectAsStateWithLifecycle()
        val updateVersionNames = pluginUpdates.mapValues { it.value.versionName }
        var showPluginNoSignatureDialog by remember { mutableStateOf(false) }
        var showPluginErrorDialog by remember { mutableStateOf(false) }
        var showPluginSignatureDialog: String? by remember { mutableStateOf(null) }
        var pendingInstallUri by remember { mutableStateOf<Uri?>(null) }
        var pendingUninstallId by remember { mutableStateOf<String?>(null) }
        val uninstallLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val id = pendingUninstallId ?: return@rememberLauncherForActivityResult
            pendingUninstallId = null
            val isUninstalled = runCatching {
                context.packageManager.getPackageInfo(id, 0)
                false
            }.getOrDefault(true)
            if (isUninstalled) {
                viewModel.deletePlugin(id)
            }
        }
        val launcher = uriLauncher { uri ->
            pendingInstallUri = uri
        }
        val snackbarHostState = LocalSnackbarHost.current
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(viewModel.snackbarFlow) {
            viewModel.snackbarFlow.collect { message ->
                snackbarHostState.showSnackbar(message, withDismissAction = true)
            }
        }
        val checkUpdateViewActionLabel = "查看"
        val errorString = stringResource(R.string.plugin_snackbar_disabled_load_error)
        val notSignedString = stringResource(R.string.plugin_snackbar_not_signed)
        val learnMoreString = stringResource(R.string.plugin_snackbar_learn_more)
        val selectPluginString = stringResource(R.string.plugin_picker_title)
        val incompatibleString = stringResource(R.string.plugin_api_incompatible)
        val restartToApply = stringResource(R.string.restart_to_apply_changes)
        val restartString = stringResource(R.string.restart)

        PluginManagerScreen(
            enabledPluginList = enabledPluginList,
            errorMessageMap = errorMessageMap,
            updateVersionNames = updateVersionNames,
            getPluginFile = viewModel::getPluginFile,
            onClickBack = navigator::popBackStack,
            onClickPluginApps = navigator::navigateToSettingsPluginAppListDestination,
            onClickDetail = navigator::navigateToSettingsPluginManagerDetailDestination,
            onClickSwitch = { pluginMetadata ->
                viewModel.onClickEnabledSwitch(pluginMetadata)
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = restartToApply,
                    actionLabel = restartString
                ) {
                    if (it == SnackbarResult.ActionPerformed) {
                        viewModel.unloadAllDisenablePlugin()
                        restart(context)
                    }
                }
            },
            onClickDelete = { id, uninstall ->
                if (uninstall) {
                    pendingUninstallId = id
                    val intent = Intent(Intent.ACTION_DELETE, "package:$id".toUri())
                    uninstallLauncher.launch(intent)
                } else {
                    navigator.navigateToPluginInstallerDialog("uninstall:$id")
                }
            },
            pluginInfoList = viewModel.pluginList,
            onClickCheckUpdate = { packageName ->
                val updateInfo = pluginUpdates[packageName] ?: return@PluginManagerScreen
                val pluginName =
                    viewModel.pluginList.firstOrNull { it.packageName == packageName }?.name
                        ?: packageName
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = "「$pluginName」有新版本可用：${updateInfo.versionName}",
                    actionLabel = checkUpdateViewActionLabel
                ) {
                    if (it == SnackbarResult.ActionPerformed) {
                        navigator.navigateToPluginStoreInstall(updateInfo.pluginId)
                    }
                }
            },
            onClickKeyAlert = {
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = notSignedString,
                    actionLabel = learnMoreString
                ) {
                    when (it) {
                        SnackbarResult.Dismissed -> {}
                        SnackbarResult.ActionPerformed -> {
                            showPluginNoSignatureDialog = true
                        }
                    }
                }
            },
            onClickErrorAlert = {
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = errorString,
                    actionLabel = learnMoreString
                ) {
                    when (it) {
                        SnackbarResult.Dismissed -> {}
                        SnackbarResult.ActionPerformed -> {
                            showPluginErrorDialog = true
                        }
                    }
                }
            },
            onClickIncompatibleAlert = {
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = incompatibleString
                )
            },
            onClickInstall = {
                val initUri = DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "primary:Documents"
                )
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
                }
                launcher.launch(Intent.createChooser(intent, selectPluginString))
            },
            onClickShowSignatures = { id ->
                showPluginSignatureDialog = id
            }
        )

        if (pendingInstallUri != null) {
            val uri = pendingInstallUri ?: return@entry
            navigator.navigateToPluginInstallerDialog(uri.toString())
            pendingInstallUri = null
        }

        if (showPluginNoSignatureDialog) {
            PluginNoSignatureDialog(onClose = { showPluginNoSignatureDialog = false })
        }

        if (showPluginErrorDialog) {
            PluginErrorDialog(onClose = { showPluginErrorDialog = false })
        }

        showPluginSignatureDialog?.let { pluginIdToShow ->
            if (pluginIdToShow.isNotEmpty()) {
                PluginSignatureDialog(
                    onClose = { showPluginSignatureDialog = null },
                    signatureInfo = viewModel.getPluginSignatures(pluginIdToShow)
                )
            }
        }
    }
}

fun Navigator.navigateToSettingsPluginManagerHomeDestination() {
    navigate(Route.Main.Settings.PluginManager.Home)
}
