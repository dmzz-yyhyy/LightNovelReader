package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings

import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import androidx.work.WorkInfo
import androidx.work.WorkManager
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ExportContext
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ExportUserDataDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.components.MutableExportContext
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SliderValueDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.SliderValueDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.UpdatesAvailableDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug.navigateToSettingsDebugDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug.settingsDebugDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.licenses.navigateToSettingsLicensesDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.licenses.settingsLicensesDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat.navigateToSettingsLogcatDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat.settingsLogcatDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.navigateToSettingsPluginManagerHomeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.settingsPluginManagerNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange.navigateToSettingsSourceChangeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange.settingsSourceChangeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.editTextFormattingRuleDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.navigateToSettingsTextFormattingManagerDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.settingsTextFormattingNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme.navigateToSettingsThemeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme.settingsThemeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.settingsDestination() {
    composable<Route.Main.Settings.Home> {
        val navController = LocalNavController.current
        val settingsViewModel = hiltViewModel<SettingsViewModel>()
        val updatesAvailableDialogViewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
        val updatePhase by updatesAvailableDialogViewModel.updatePhaseFlow.collectAsState("Not Checked")
        SettingsScreen(
            updatePhase = updatePhase,
            settingState = settingsViewModel.settingState,
            checkUpdate = updatesAvailableDialogViewModel::checkUpdate,
            importData = settingsViewModel::importFromFile,
            onClickDebugMode = navController::navigateToSettingsDebugDestination,
            onClickLicenses = navController::navigateToSettingsLicensesDestination,
            onClickChangeSource = navController::navigateToSettingsSourceChangeDestination,
            onClickExportUserData = navController::navigateToExportUserDataDialog,
            onClickLogcat = navController::navigateToSettingsLogcatDestination,
            onClickTextFormatting = navController::navigateToSettingsTextFormattingManagerDestination,
            onClickPluginManager = navController::navigateToSettingsPluginManagerHomeDestination,
            onClickThemeSettings = navController::navigateToSettingsThemeDestination
        )
    }
    settingsSourceChangeDestination()
    exportUserDataDialog()
    editTextFormattingRuleDialog()
    sliderValueDialog()
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.settingsNavigation() {
    navigation<Route.Main.Settings>(
        startDestination = Route.Main.Settings.Home
    ) {
        settingsDestination()
        settingsDebugDestination()
        settingsLogcatDestination()
        settingsThemeDestination()
        settingsTextFormattingNavigation()
        settingsPluginManagerNavigation()
        settingsLicensesDestination()
    }
}

@Suppress("unused")
fun NavController.navigateToSettingsDestination() {
    navigate(Route.Main.Settings)
}

private fun NavGraphBuilder.sliderValueDialog() {
    dialog<Route.SliderValueDialog> { entry ->
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<SliderValueDialogViewModel>()
        val route = entry.toRoute<Route.SliderValueDialog>()
        val value = route.value
        SliderValueDialog(
            value = value,
            onValueChange = { viewModel.setValue(it) },
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                navController.popBackStack()
            }
        )

    }
}

fun NavController.navigateToSliderValueDialog(path: String, value: Float) {
    if (!this.isResumed()) return
    navigate(Route.SliderValueDialog(value, path))
}


private fun NavGraphBuilder.exportUserDataDialog() {
    dialog<Route.Main.ExportUserDataDialog> {
        val navController = LocalNavController.current
        val context = LocalContext.current
        val workManager = WorkManager.getInstance(context)
        val viewModel = hiltViewModel<ExportUserDataDialogViewModel>()
        var exportContext: ExportContext by remember { mutableStateOf(MutableExportContext()) }
        val saveDataToFileLauncher = uriLauncher { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                workManager.getWorkInfoByIdFlow(viewModel.exportToFile(uri, exportContext).id).collect {
                    when (it?.state) {
                        WorkInfo.State.FAILED -> {
                            Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
            navController.popBackStack()
        }
        ExportUserDataDialog(
            onDismissRequest = { navController.popBackStack() },
            onClickSaveAndSend = {
                viewModel.exportAndSendToFile(exportContext, context)
                navController.popBackStack()
            },
            onClickSaveToFile = {
                exportContext = it
                createDataFile("LightNovelReaderData", saveDataToFileLauncher)
            }
        )
    }
}

private fun NavController.navigateToExportUserDataDialog() {
    navigate(Route.Main.ExportUserDataDialog)
}

@Suppress("DuplicatedCode", "SameParameterValue")
private fun createDataFile(fileName: String, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
        putExtra(Intent.EXTRA_TITLE, "$fileName.lnr")
    }
    launcher.launch(Intent.createChooser(intent, "选择一位置"))
}
