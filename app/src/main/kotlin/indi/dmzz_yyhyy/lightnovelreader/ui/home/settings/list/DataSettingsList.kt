package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import kotlinx.coroutines.launch

@Composable
fun DataSettingsList(
    onClickChangeSource: () -> Unit,
    onClickExportUserData: () -> Unit,
    onClickLogcat: () -> Unit,
    settingState: SettingState,
    importData: (Uri) -> OneTimeWorkRequest,
) {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val scope = rememberCoroutineScope()
    val importDataLauncher = uriLauncher {
        scope.launch {
            workManager.getWorkInfoByIdFlow(importData(it).id).collect {
                when (it?.state) {
                    WorkInfo.State.FAILED -> {
                        Toast.makeText(context, "导入失败，请检查文件格式或文件已损坏。", Toast.LENGTH_SHORT).show()
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
    /*
    var displayExportDialog by remember { mutableStateOf(false) }
    var displaySourceChangeDialog by remember { mutableStateOf(false) }
    dialog {
        var selectedWebDataSourceId by remember { mutableStateOf(webDataSourceId) }
        if (displaySourceChangeDialog) {
            SourceChangeDialog(
                onDismissRequest = {
                    displaySourceChangeDialog = false
                    selectedWebDataSourceId = webDataSourceId
                },
                onConfirmation = {
                    displaySourceChangeDialog = false
                    changeWebDataSource(selectedWebDataSourceId, context)
                },
                webDataSourceItems = listOf(wenku8ApiWebDataSourceItem, zaiComicWebDataSourceItem),
                selectedWebDataSourceId = selectedWebDataSourceId,
                onClickItem = {
                    selectedWebDataSourceId = it
                }
            )
        }
        if (displayExportDialog) {
            ExportUserDataDialog(
                onDismissRequest = { displayExportDialog = false },
                onClickSaveAndSend = {
                    displayExportDialog = false
                    exportAndSendToFile(exportContext, context)
                },
                onClickSaveToFile = {
                    displayExportDialog = false
                    exportContext = it
                    createDataFile("LightNovelReaderData", saveDataToFileLauncher)
                }
            )
        }
    }*/
    SettingsClickableEntry(
        iconRes = R.drawable.output_24px,
        title = stringResource(R.string.settings_snap_data),
        description = stringResource(R.string.settings_snap_data_desc),
        onClick = onClickExportUserData
    )
    SettingsClickableEntry(
        iconRes = R.drawable.input_24px,
        title = stringResource(R.string.settings_import_data),
        description = stringResource(R.string.settings_import_data_desc),
        onClick = { selectDataFile(importDataLauncher) }
    )
    SettingsClickableEntry(
        iconRes = R.drawable.public_24px,
        title = stringResource(R.string.settings_select_data_source),
        description = stringResource(R.string.settings_select_data_source_desc),
        onClick = onClickChangeSource
    )
    SettingsSwitchEntry(
        iconRes = R.drawable.wifi_proxy_24px,
        title = "自动代理",
        description = "自动从网络上获取代理ip, **但这将导致您所浏览的书本数据可能会被截获**",
        checked = settingState.isUseProxy,
        booleanUserData = settingState.isUseProxyUserData
    )
    SettingsClickableEntry(
        iconRes = R.drawable.bug_report_24px,
        title = stringResource(R.string.settings_app_logs),
        description = stringResource(R.string.settings_app_logs_desc),
        onClick = onClickLogcat
    )
    SettingsMenuEntry(
        iconRes = R.drawable.bug_report_24px,
        title = stringResource(R.string.settings_app_log_level),
        description = stringResource(R.string.settings_app_log_level_desc),
        options = MenuOptions.LogLevelOptions,
        selectedOptionKey = settingState.logLevelKey,
        onOptionChange = settingState.logLevelKeyUserData::asynchronousSet
    )
}

fun selectDataFile(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
    }
    launcher.launch(Intent.createChooser(intent, "选择数据文件"))
}