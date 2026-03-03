package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar

@Composable
fun AppSettingsList(
    settingState: SettingState,
    onClickLogcat: () -> Unit,
    onClickPluginManager: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHost.current

    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.extension_24px),
        title = stringResource(id = R.string.settings_plugins),
        description = stringResource(id = R.string.settings_plugins_desc),
        onClick = onClickPluginManager,
        option = stringResource(R.string.item_view_details)
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.bug_report_24px),
        title = stringResource(R.string.settings_app_logs),
        description = stringResource(R.string.settings_app_logs_desc),
        onClick = onClickLogcat
    )
    val restartToApplyText = stringResource(R.string.restart_to_apply_changes)
    SettingsMenuEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.bug_report_24px),
        title = stringResource(R.string.settings_app_log_level),
        description = stringResource(R.string.settings_app_log_level_desc),
        options = MenuOptions.LogLevelOptions,
        selectedOptionKey = settingState.logLevelKey,
        onOptionChange = { option ->
            settingState.logLevelKeyUserData.asynchronousSet(option)
            showSnackbar(
                coroutineScope = coroutineScope,
                hostState = snackbarHostState,
                message = restartToApplyText
            ) { }
        }
    )
}