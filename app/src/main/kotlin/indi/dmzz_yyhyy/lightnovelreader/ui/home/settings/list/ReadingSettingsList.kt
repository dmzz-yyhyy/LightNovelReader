package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry
import io.nightfish.lightnovelreader.api.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState

@Composable
fun ReadingSettingsList(
    settingState: SettingState,
    onClickReaderStyle: () -> Unit,
    onClickTextFormatting: () -> Unit
) {
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.chrome_reader_mode_24px),
        title = stringResource(R.string.settings_reader_style),
        description = stringResource(R.string.settings_reader_style_desc),
        onClick = onClickReaderStyle
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.find_replace_24px),
        title = stringResource(R.string.settings_text_formatting),
        description = stringResource(R.string.settings_text_formatting_desc),
        onClick = onClickTextFormatting
    )
    SettingsSwitchEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.translate_24px),
        title = stringResource(R.string.settings_trad_conversion),
        description = stringResource(R.string.settings_trad_conversion_desc),
        checked = settingState.enableSimplifiedTraditionalTransform,
        booleanUserData = settingState.enableSimplifiedTraditionalTransformUserData,
    )
}