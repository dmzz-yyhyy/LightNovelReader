package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalDarkColorScheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalLightColorScheme
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import io.nightfish.lightnovelreader.api.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingsCategory
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import io.nightfish.lightnovelreader.api.ui.components.SettingsSwitchEntry

@Composable
fun ThemeScreen(
    settingState: ThemeSettingState,
    onClickBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(onClickBack)
        LazyColumn {
            item {
                DarkModeSettings(settingState)
            }
            item {
                AppThemeSettingsList(settingState)
            }
            navigationBarSpacer()
        }
    }
}

@Composable
fun DarkModeSettings(
    settingState: ThemeSettingState
) {
    SectionHeader(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
        text = stringResource(R.string.settings_theme_dark_theme)
    )

    val appTheme = AppTheme(
        isDark = LocalAppTheme.current.isDark,
        colorScheme = colorScheme,
    )

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LightThemeSettingsItem()
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        modifier = Modifier.size(32.dp),
                        selected = settingState.darkMode == "Disabled",
                        onClick = { settingState.darkModeUserData.asynchronousSet("Disabled") }
                    )
                    Text(
                        stringResource(R.string.key_dark_mode_disabled),
                        style = typography.labelLarge
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DarkThemeSettingsItem()
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        modifier = Modifier.size(32.dp),
                        selected = settingState.darkMode == "Enabled",
                        onClick = { settingState.darkModeUserData.asynchronousSet("Enabled") }
                    )
                    Text(
                        stringResource(R.string.key_dark_mode_enabled),
                        style = typography.labelLarge
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(170.dp)
                        .width(110.dp)
                ) {
                    val shapeTop = GenericShape { size: Size, _ ->
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, size.height / 2)
                        lineTo(0f, size.height / 2)
                        close()
                    }

                    val shapeBottom = GenericShape { size: Size, _ ->
                        moveTo(0f, size.height / 2)
                        lineTo(size.width, size.height / 2)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }

                    val modifierTop = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            clip = true
                            shape = shapeTop
                        }

                    val modifierBottom = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            clip = true
                            shape = shapeBottom
                        }

                    LightThemeSettingsItem(modifier = modifierTop)
                    DarkThemeSettingsItem(modifier = modifierBottom)
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        modifier = Modifier.size(32.dp),
                        selected = settingState.darkMode == "FollowSystem",
                        onClick = { settingState.darkModeUserData.asynchronousSet("FollowSystem") }
                    )
                    Text(
                        stringResource(R.string.key_dark_mode_follow_system),
                        style = typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun AppThemeSettingsList(
    settingState: ThemeSettingState,
) {
    SettingsCategory(
        title = stringResource(R.string.theme_settings),
    ) {
        SettingsSwitchEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.format_color_fill_24px),
            title = stringResource(R.string.settings_theme_dynamic_colors),
            description = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                stringResource(R.string.settings_theme_dynamic_colors_desc_unavailable)
            else stringResource(R.string.settings_theme_dynamic_colors_desc),
            checked = settingState.dynamicColors,
            booleanUserData = settingState.dynamicColorsUserData,
            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )
        SettingsMenuEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.light_mode_24px),
            title = stringResource(R.string.settings_theme_light_theme),
            description = stringResource(R.string.settings_theme_light_theme_desc),
            options = MenuOptions.LightThemeNameOptions,
            selectedOptionKey = settingState.lightThemeName,
            onOptionChange = settingState.lightThemeNameUserData::asynchronousSet,
            enabled = !settingState.dynamicColors
        )
        SettingsMenuEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.dark_mode_24px),
            title = stringResource(R.string.settings_theme_dark_theme),
            description = stringResource(R.string.settings_theme_dark_theme_desc),
            options = MenuOptions.DarkThemeNameOptions,
            selectedOptionKey = settingState.darkThemeName,
            onOptionChange = settingState.darkThemeNameUserData::asynchronousSet,
            enabled = !settingState.dynamicColors
        )
    }
}

@Composable
private fun LightThemeSettingsItem(
    modifier: Modifier = Modifier
) {
    MaterialTheme(
        LocalLightColorScheme.current
    ) {
        DarkModeSettingItem(modifier)
    }
}

@Composable
private fun DarkThemeSettingsItem(
    modifier: Modifier = Modifier,
) {
    MaterialTheme(
        LocalDarkColorScheme.current
    ) {
        DarkModeSettingItem(modifier)
    }
}

@Composable
private fun BasePageItem(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = colorScheme.background,
                        shape = RoundedCornerShape(9.dp)
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DarkModeSettingItem(
    modifier: Modifier
) {
    BasePageItem(
        modifier = modifier
            .width(110.dp)
            .height(170.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .width(36.dp)
                        .background(
                            color = colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(8.dp)
                            .background(
                                color = colorScheme.primaryContainer,
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(8.dp)
                            .background(
                                color = colorScheme.secondaryContainer,
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(8.dp)
                            .background(
                                color = colorScheme.secondaryContainer,
                                shape = CircleShape
                            )
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(8.dp)
                        .background(
                            color = colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color = colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(8.dp)
                        .background(
                            color = colorScheme.inversePrimary,
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(8.dp)
                        .background(
                            color = colorScheme.tertiaryContainer,
                            shape = CircleShape
                        )
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 8.dp)
                .height(height = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(18.dp)
                    .background(
                        color = colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = typography.displayLarge,
                    fontWeight = FontWeight.W600,
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
