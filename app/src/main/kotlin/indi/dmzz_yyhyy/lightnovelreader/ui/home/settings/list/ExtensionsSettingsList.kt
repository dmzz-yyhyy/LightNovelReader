package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry

@Composable
fun ExtensionsSettingsList(
    onClickChangeSource: () -> Unit,
    onClickPluginManager: () -> Unit
) {
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.public_24px),
        title = stringResource(R.string.settings_sources_and_plugins),
        description = stringResource(R.string.settings_sources_and_plugins_desc),
        onClick = onClickPluginManager
    )
}