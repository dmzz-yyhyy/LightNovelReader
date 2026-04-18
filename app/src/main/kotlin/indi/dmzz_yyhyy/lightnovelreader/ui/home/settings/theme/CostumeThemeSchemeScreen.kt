package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry
import io.nightfish.lightnovelreader.api.userdata.UserDataPath

@Composable
fun CostumeThemeSchemeScreen(
    settingState: SettingState,
    onClickBack: () -> Unit,
    onClickOpenColorPicker: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CostumeThemeSchemeTopBar(onClickBack)
        LazyColumn {
            item {
                SectionHeader(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    text = "Primary：主要配色"
                )
            }
            item {
                ColorPickerEntry(
                    title = "Primary",
                    description = "主要顏色",
                    colorUserData = settingState.costumeThemeSchemePrimaryUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemePrimary.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "On Primary",
                    description = "主要顏色上的文字",
                    colorUserData = settingState.costumeThemeSchemeOnPrimaryUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeOnPrimary.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "Primary Container",
                    description = "主要容器顏色",
                    colorUserData = settingState.costumeThemeSchemePrimaryContainerUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemePrimaryContainer.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "On Primary Container",
                    description = "主要容器上的文字",
                    colorUserData = settingState.costumeThemeSchemeOnPrimaryContainerUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeOnPrimaryContainer.path) }
                )
            }
            item {
                SectionHeader(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    text = "Secondary：次要配色"
                )
            }
            item {
                ColorPickerEntry(
                    title = "Secondary",
                    description = "次要顏色",
                    colorUserData = settingState.costumeThemeSchemeSecondaryUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeSecondary.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "On Secondary",
                    description = "次要顏色上的文字",
                    colorUserData = settingState.costumeThemeSchemeOnSecondaryUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeOnSecondary.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "Secondary Container",
                    description = "次要容器顏色",
                    colorUserData = settingState.costumeThemeSchemeSecondaryContainerUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeSecondaryContainer.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "On Secondary Container",
                    description = "次要容器上的文字",
                    colorUserData = settingState.costumeThemeSchemeOnSecondaryContainerUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeOnSecondaryContainer.path) }
                )
            }
            item {
                SectionHeader(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    text = "Tertiary：3rd配色"
                )
            }
            item {
                ColorPickerEntry(
                    title = "Tertiary",
                    description = "第三顏色",
                    colorUserData = settingState.costumeThemeSchemeTertiaryUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeTertiary.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "On Tertiary",
                    description = "第三顏色上的文字",
                    colorUserData = settingState.costumeThemeSchemeOnTertiaryUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeOnTertiary.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "Tertiary Container",
                    description = "第三容器顏色",
                    colorUserData = settingState.costumeThemeSchemeTertiaryContainerUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeTertiaryContainer.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "On Tertiary Container",
                    description = "第三容器上的文字",
                    colorUserData = settingState.costumeThemeSchemeOnTertiaryContainerUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeOnTertiaryContainer.path) }
                )
            }
            item {
                SectionHeader(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    text = "Background & Surface：背景與導航列配色"
                )
            }
            item {
                ColorPickerEntry(
                    title = "Background",
                    description = "背景顏色",
                    colorUserData = settingState.costumeThemeSchemeBackgroundUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeBackground.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "On Background",
                    description = "背景上的文字顏色",
                    colorUserData = settingState.costumeThemeSchemeOnBackgroundUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeOnBackground.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "Surface",
                    description = "導航列顏色",
                    colorUserData = settingState.costumeThemeSchemeSurfaceUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeSurface.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "On Surface",
                    description = "導航列上的文字顏色",
                    colorUserData = settingState.costumeThemeSchemeOnSurfaceUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeOnSurface.path) }
                )
            }
            item {
                SectionHeader(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    text = "Error：錯誤提示配色"
                )
            }
            item {
                ColorPickerEntry(
                    title = "錯誤顏色",
                    description = "color: Error",
                    colorUserData = settingState.costumeThemeSchemeErrorUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeError.path) }
                )
            }
            item {
                ColorPickerEntry(
                    title = "On Error",
                    description = "錯誤顏色上的文字",
                    colorUserData = settingState.costumeThemeSchemeOnErrorUserData,
                    onClickOpenColorPicker = { onClickOpenColorPicker(UserDataPath.Settings.Display.CostumeThemeSchemeOnError.path) }
                )
            }
            navigationBarSpacer()
        }
    }
}

@Composable
private fun ColorPickerEntry(
    title: String,
    description: String,
    colorUserData: io.nightfish.lightnovelreader.api.userdata.ColorUserData,
    onClickOpenColorPicker: () -> Unit
) {
    val colorFlow = remember(colorUserData) {
        colorUserData.getFlowWithDefault(Color.Unspecified)
    }
    val currentColor = colorFlow.collectAsState(initial = Color.Unspecified).value
    val onSecondaryContainer = colorScheme.onSecondaryContainer
    val background = colorScheme.background

    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.palette_24px),
        title = title,
        description = description,
        onClick = onClickOpenColorPicker,
        trailingContent = {
            Canvas(
                modifier = Modifier.size(44.dp)
            ) {
                drawCircle(
                    color = onSecondaryContainer,
                    radius = 20.dp.toPx(),
                )
                drawCircle(
                    color = background,
                    radius = 17.5.dp.toPx(),
                )
                drawCircle(
                    color = currentColor,
                    radius = 17.5.dp.toPx(),
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CostumeThemeSchemeTopBar(
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "覆蓋主題配色",
                    style = typography.displayLarge,
                    color = colorScheme.onSurface
                )
            }
        },
        navigationIcon = {
            IconButton(onClickBack) {
                Icon(
                    painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        }
    )
}
