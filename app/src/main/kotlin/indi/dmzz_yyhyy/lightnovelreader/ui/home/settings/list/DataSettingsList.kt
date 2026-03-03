package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import kotlinx.coroutines.launch

@Composable
fun DataSettingsList(
    onClickChangeSource: () -> Unit,
    onClickExportUserData: () -> Unit,
    settingState: SettingState,
    importData: (Uri) -> OneTimeWorkRequest,
) {
    val dataImportFailedText = stringResource(R.string.data_import_failed)
    val dataImportSuccessText = stringResource(R.string.data_import_success)

    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val scope = rememberCoroutineScope()
    val importDataLauncher = uriLauncher { uri ->
        scope.launch {
            workManager.getWorkInfoByIdFlow(importData(uri).id).collect {
                when (it?.state) {
                    WorkInfo.State.FAILED -> {
                        Toast.makeText(context, dataImportFailedText, Toast.LENGTH_SHORT).show()
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        Toast.makeText(context, dataImportSuccessText, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.output_24px),
        title = stringResource(R.string.settings_snap_data),
        description = stringResource(R.string.settings_snap_data_desc),
        onClick = onClickExportUserData
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.input_24px),
        title = stringResource(R.string.settings_import_data),
        description = stringResource(R.string.settings_import_data_desc),
        onClick = { selectDataFile(importDataLauncher) }
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.public_24px),
        title = stringResource(R.string.settings_select_data_source),
        description = stringResource(R.string.settings_select_data_source_desc),
        onClick = onClickChangeSource
    )
    SettingsSwitchEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.wifi_proxy_24px),
        title = stringResource(R.string.settings_auto_proxy),
        description = stringResource(R.string.settings_auto_proxy_desc),
        checked = settingState.isUseProxy,
        booleanUserData = settingState.isUseProxyUserData
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