package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.applist.navigateToSettingsPluginAppListDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.applist.settingsPluginAppListDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.navigateToSettingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.settingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.repository.navigateToSettingsPluginRepositoryDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.repository.settingsPluginRepositoryDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import androidx.core.net.toUri

fun NavGraphBuilder.settingsPluginManagerNavigation() {
    navigation<Route.Main.Settings.PluginManager>(
        startDestination = Route.Main.Settings.PluginManager.Home
    ) {
        settingsPluginManagerHomeDestination()
        settingsPluginAppListDestination()
        settingsPluginManagerDetailDestination()
        settingsPluginRepositoryDestination()
    }
}

fun NavGraphBuilder.settingsPluginManagerHomeDestination() {
    composable<Route.Main.Settings.PluginManager.Home> { navBackStackEntry ->
        val navController = LocalNavController.current
        val context = LocalContext.current
        val parentEntry = remember(navBackStackEntry) {
            navBackStackEntry.destination.parent?.route
                ?.let(navController::getBackStackEntry)
        }
        val viewModel = hiltViewModel<PluginManagerViewModel>(parentEntry ?: navBackStackEntry)
        val enabledPluginList by viewModel.enabledPluginFlow.collectAsState(emptyList())
        val errorMessageMap = viewModel.errorMessageMap
        var showPluginNoSignatureDialog by remember { mutableStateOf(false) }
        var showPluginErrorDialog by remember { mutableStateOf(false) }
        var showPluginSignatureDialog: String? by remember { mutableStateOf(null) }
        var pendingInstallUri by remember { mutableStateOf<Uri?>(null) }
        var pendingInstallName by remember { mutableStateOf("") }
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
            pendingInstallName = getDisplayName(context, uri)
        }
        val snackbarHostState = LocalSnackbarHost.current
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(viewModel.snackbarFlow) {
            viewModel.snackbarFlow.collect { message ->
                snackbarHostState.showSnackbar(message, withDismissAction = true)
            }
        }

        val errorString = stringResource(R.string.plugin_snackbar_disabled_load_error)
        val notSignedString = stringResource(R.string.plugin_snackbar_not_signed)
        val learnMoreString = stringResource(R.string.plugin_snackbar_learn_more)
        val selectPluginString = stringResource(R.string.plugin_picker_title)
        val incompatibleString = stringResource(R.string.plugin_api_incompatible)

        PluginManagerScreen(
            enabledPluginList = enabledPluginList,
            errorMessageMap = errorMessageMap,
            onClickBack = navController::popBackStackIfResumed,
            onClickPluginApps = navController::navigateToSettingsPluginAppListDestination,
            onClickDetail = navController::navigateToSettingsPluginManagerDetailDestination,
            onClickSwitch = viewModel::onClickEnabledSwitch,
            onClickDelete = { id, uninstall ->
                if (uninstall) {
                    pendingUninstallId = id
                    val intent = Intent(Intent.ACTION_DELETE, "package:$id".toUri())
                    uninstallLauncher.launch(intent)
                } else {
                    navController.navigateToPluginInstallerDialog("uninstall:$id")
                }
            },
            pluginInfoList = viewModel.pluginList,
            onClickCheckUpdate = { TODO() },
            onClickKeyAlert = {
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = notSignedString,
                    actionLabel = learnMoreString
                ) {
                    when (it) {
                        SnackbarResult.Dismissed -> { }
                        SnackbarResult.ActionPerformed -> { showPluginNoSignatureDialog = true }
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
                        SnackbarResult.Dismissed -> { }
                        SnackbarResult.ActionPerformed -> { showPluginErrorDialog = true }
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
            onClickPluginRepo = navController::navigateToSettingsPluginRepositoryDestination,
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
            AlertDialog(
                onDismissRequest = {
                    pendingInstallUri = null
                    pendingInstallName = ""
                },
                title = {
                    Text(
                        text = stringResource(R.string.plugin_install_dialog_title),
                        style = typography.displayMedium,
                        color = colorScheme.onSurface
                    )
                },
                text = { Text(stringResource(R.string.plugin_install_dialog_body, pendingInstallName)) },
                confirmButton = {
                    Row {
                        /*TextButton(
                            onClick = {
                                val uri = pendingInstallUri ?: return@TextButton
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/vnd.android.package-archive")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                                pendingInstallUri = null
                                pendingInstallName = ""
                            }
                        ) {
                            Text("系统安装")
                        }*/
                        TextButton(
                            onClick = {
                                val uri = pendingInstallUri ?: return@TextButton
                                navController.navigateToPluginInstallerDialog(uri.toString())
                                pendingInstallUri = null
                                pendingInstallName = ""
                            }
                        ) {
                            Text(stringResource(R.string.plugin_install_action_install))
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            pendingInstallUri = null
                            pendingInstallName = ""
                        }
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
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

fun NavController.navigateToSettingsPluginManagerHomeDestination() {
    navigate(Route.Main.Settings.PluginManager.Home)
}

private fun getDisplayName(context: android.content.Context, uri: Uri): String {
    val resolver = context.contentResolver
    val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    return name ?: uri.lastPathSegment?.substringAfterLast('/') ?: context.getString(R.string.plugin_unknown_name)
}
