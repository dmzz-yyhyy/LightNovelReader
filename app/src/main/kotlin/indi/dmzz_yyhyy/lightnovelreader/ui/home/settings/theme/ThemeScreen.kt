package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.rememberAsyncImagePainter
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalDarkColorScheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalLightColorScheme
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSliderEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingsCategory
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import indi.dmzz_yyhyy.lightnovelreader.utils.readerBackgroundColor
import indi.dmzz_yyhyy.lightnovelreader.utils.readerTextColor
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderBackgroundPainter
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderFontFamily
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry
import io.nightfish.lightnovelreader.api.ui.components.SettingsSwitchEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

@Composable
fun ThemeScreen(
    themeSettingState: SettingState,
    onClickBack: () -> Unit,
    onClickChangeTextColor: () -> Unit,
    onClickChangeBackgroundColor: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(onClickBack)
        LazyColumn {
            item {
                DarkModeSettings(themeSettingState)
            }
            item {
                ThemeSettingsList(themeSettingState)
            }
            item {
                ReaderThemeSettingsList(themeSettingState, onClickChangeBackgroundColor)
            }
            item {
                BackgroundSettings(themeSettingState, context)
            }
            item {
                ReaderTextSettings(themeSettingState, context, onClickChangeTextColor)
            }
            navigationBarSpacer()
        }
    }
}

@Composable
fun DarkModeSettings(
    settingState: SettingState
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
                        selected = settingState.darkModeKey == "Disabled",
                        onClick = { settingState.darkModeKeyUserData.asynchronousSet("Disabled") }
                    )
                    Text(stringResource(R.string.key_dark_mode_disabled), style = typography.labelLarge)
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
                        selected = settingState.darkModeKey == "Enabled",
                        onClick = { settingState.darkModeKeyUserData.asynchronousSet("Enabled") }
                    )
                    Text(stringResource(R.string.key_dark_mode_enabled), style = typography.labelLarge)
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
                        selected = settingState.darkModeKey == "FollowSystem",
                        onClick = { settingState.darkModeKeyUserData.asynchronousSet("FollowSystem") }
                    )
                    Text(stringResource(R.string.key_dark_mode_follow_system), style = typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun ThemeSettingsList(
    settingState: SettingState,
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
            checked = settingState.dynamicColorsKey,
            booleanUserData = settingState.dynamicColorsKeyUserData,
            disabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
        )
        SettingsSwitchEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.experiment_24px),
            title = stringResource(R.string.settings_theme_m3e),
            description = stringResource(R.string.settings_theme_m3e_description),
            checked = settingState.enableM3E,
            booleanUserData = settingState.enableM3EUserData
        )
        if (!settingState.dynamicColorsKey) {
            SettingsMenuEntry(
                modifier = Modifier.background(colorScheme.surfaceContainer),
                painter = painterResource(R.drawable.light_mode_24px),
                title = stringResource(R.string.settings_theme_light_theme),
                description = stringResource(R.string.settings_theme_light_theme_desc),
                options = MenuOptions.LightThemeNameOptions,
                selectedOptionKey = settingState.lightThemeName,
                onOptionChange = settingState.lightThemeNameUserData::asynchronousSet
            )
            SettingsMenuEntry(
                modifier = Modifier.background(colorScheme.surfaceContainer),
                painter = painterResource(R.drawable.dark_mode_24px),
                title = stringResource(R.string.settings_theme_dark_theme),
                description = stringResource(R.string.settings_theme_dark_theme_desc),
                options = MenuOptions.DarkThemeNameOptions,
                selectedOptionKey = settingState.darkThemeName,
                onOptionChange = settingState.darkThemeNameUserData::asynchronousSet
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ReaderThemeSettingsList(
    settingState: SettingState,
    onClickChangeBackgroundColor: () -> Unit
) {
    SettingsCategory(
        title = stringResource(R.string.paper_settings),
    ) {
        val context = LocalContext.current
        val snackbarHostState = LocalSnackbarHost.current

        var lastEnabled by remember { mutableStateOf(settingState.enableBackgroundImage) }

        LaunchedEffect(settingState.enableBackgroundImage) {
            val now = settingState.enableBackgroundImage
            if (!lastEnabled && now) {
                val loader = context.imageLoader

                val key = "default_kraft_paper"

                val memHit = loader.memoryCache?.get(MemoryCache.Key(key)) != null
                val diskHit = loader.diskCache?.openSnapshot(key)?.use { true } ?: false

                if (!memHit && !diskHit) {
                    snackbarHostState.showSnackbar("正在下载纸张背景…")

                    loader.enqueue(
                        ImageRequest.Builder(context)
                            .data(key)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .memoryCacheKey(key)
                            .build()
                    )
                }
            }
            lastEnabled = now
        }


        SettingsSwitchEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.imagesearch_roller_24px),
            title = stringResource(R.string.settings_theme_bg_image),
            description = stringResource(R.string.settings_theme_bg_image_desc),
            checked = settingState.enableBackgroundImage,
            booleanUserData = settingState.enableBackgroundImageUserData
        )
        if (settingState.enableBackgroundImage) {
            SettingsMenuEntry(
                modifier = Modifier.background(colorScheme.surfaceContainer),
                title = stringResource(R.string.settings_theme_bg_display_mode),
                painter = painterResource(R.drawable.insert_page_break_24px),
                description = stringResource(R.string.settings_theme_bg_display_mode_desc),
                options = MenuOptions.ReaderBgImageDisplayModeOptions,
                selectedOptionKey = settingState.backgroundImageDisplayMode,
                stringUserData = settingState.backgroundImageDisplayModeUserData
            )
        } else {
            val onSecondaryContainer = colorScheme.onSecondaryContainer
            val background = colorScheme.background
            val currentBgColor = readerBackgroundColor(settingState)
            SettingsClickableEntry(
                modifier = Modifier.background(colorScheme.surfaceContainer),
                painter = painterResource(R.drawable.colorize_24px),
                title = stringResource(R.string.settings_theme_bg_color),
                description = stringResource(R.string.settings_theme_bg_color_desc),
                onClick = onClickChangeBackgroundColor,
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
                            color = currentBgColor,
                            radius = 17.5.dp.toPx(),
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ReaderTextSettings(settingState: SettingState, context: Context, onClickChangeTextColor: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val onSecondaryContainer = colorScheme.onSecondaryContainer
    val background = colorScheme.background
    val currentColor = readerTextColor(settingState)

    SettingsCategory(
        title = stringResource(R.string.text_settings),
    ) {
        SettingsClickableEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.palette_24px),
            title = stringResource(R.string.settings_theme_text_color),
            description = stringResource(R.string.settings_theme_text_color_desc),
            onClick = onClickChangeTextColor,
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

        val fontPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            coroutineScope.launch(Dispatchers.IO) {
                val fontFile = saveFontToLocal(context, uri) ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.font_file_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                try {
                    textMeasurer.measure(
                        text = "",
                        style = TextStyle(fontFamily = FontFamily(Font(fontFile)))
                    )
                    settingState.fontFamilyUriUserData.set(fontFile.toUri())
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.font_file_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        SettingsMenuEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.text_fields_24px),
            title = stringResource(R.string.settings_theme_text_font),
            description = stringResource(R.string.settings_theme_text_font_desc),
            options = MenuOptions.SelectText,
            selectedOptionKey = if (settingState.fontFamilyUri.toString().isEmpty())
                MenuOptions.SelectText.Default else MenuOptions.SelectText.Customize,
            onOptionChange = {
                when (it) {
                    MenuOptions.SelectText.Default -> settingState.fontFamilyUriUserData.asynchronousSet(Uri.EMPTY)
                    MenuOptions.SelectText.Customize -> fontPicker.launch("*/*")
                }
            }
        )
    }
    BasePageItem(
        Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 0.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (settingState.enableBackgroundImage) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = rememberReaderBackgroundPainter(settingState),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(readerBackgroundColor(settingState)))
            }

            Text(
                modifier = Modifier.padding(horizontal = 18.dp),
                text = stringResource(R.string.settings_about_oss),
                fontSize = settingState.fontSize.sp,
                lineHeight = (settingState.fontLineHeight + settingState.fontSize).sp,
                fontWeight = FontWeight(settingState.fontWeigh.toInt()),
                textAlign = TextAlign.Center,
                fontFamily = rememberReaderFontFamily(settingState.fontFamilyUriUserData),
                color = readerTextColor(settingState)
            )
        }
    }

    SettingsCategory {
        SettingsSliderEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.format_bold_24px),
            title = stringResource(R.string.settings_theme_text_font_weight),
            unit = "", valueRange = 100f..900f,
            value = settingState.fontWeigh,
            valueFormat = { (it / 100).toInt() * 100f },
            floatUserData = settingState.fontWeighUserData
        )

        SettingsSliderEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.format_size_24px),
            title = stringResource(R.string.settings_reader_font_size),
            unit = "sp",
            valueRange = 8f..64f,
            value = settingState.fontSize,
            floatUserData = settingState.fontSizeUserData
        )

        SettingsSliderEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.format_line_spacing_24px),
            title = stringResource(R.string.settings_reader_line_spacing),
            unit = "sp",
            valueRange = 0f..32f,
            value = settingState.fontLineHeight,
            floatUserData = settingState.fontLineHeightUserData
        )
    }
}

private suspend fun saveFontToLocal(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
    val fontFile = context.filesDir.resolve("readerTextFont").apply {
        if (exists()) delete()
        createNewFile()
    }
    try {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
            FileInputStream(fd.fileDescriptor).use { input ->
                fontFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        fontFile
    } catch (e: Exception) {
        Log.e("ReaderTextFont", "Failed to import font", e)
        null
    }
}

@Composable
fun BackgroundSettings(settingState: SettingState, context: Context) {
    val scope = rememberCoroutineScope()

    val isCustomSelected = settingState.backgroundImageUri.toString().isNotBlank() ||
            settingState.backgroundDarkImageUri.toString().isNotBlank()
    var isDarkSelection by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val fileName = if (isDarkSelection) "readerDarkBackgroundImage" else "readerBackgroundImage"
            val file = context.filesDir.resolve(fileName).apply {
                if (exists()) delete()
                createNewFile()
            }
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                FileInputStream(fd.fileDescriptor).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
            val fileUri = file.toUri()
            if (isDarkSelection)
                settingState.backgroundDarkImageUriUserData.set(fileUri)
            else
                settingState.backgroundImageUriUserData.set(fileUri)
        }
    }

    if (!settingState.enableBackgroundImage) return

    SettingsCategory(title = "自定义纸张") {
        BackgroundCard(
            title = stringResource(R.string.settings_theme_bg_image_built_in),
            desc = stringResource(R.string.settings_theme_bg_image_built_in_desc),
            selected = !isCustomSelected,
            onClick = {
                settingState.enableBackgroundImageUserData.asynchronousSet(true)
                settingState.backgroundImageUriUserData.asynchronousSet(Uri.EMPTY)
                settingState.backgroundDarkImageUriUserData.asynchronousSet(Uri.EMPTY)
            }
        )

        BackgroundCard(
            title = stringResource(R.string.settings_theme_bg_image_custom),
            selected = isCustomSelected,
            enabled = isCustomSelected,
            onClick = { },
            contentBelow = {
                Column {
                    Spacer(Modifier.height(6.dp))
                    BackgroundSelectRow(
                        label = stringResource(R.string.choose_light_bg),
                        uri = settingState.backgroundImageUri,
                        previewSize = 52.dp,
                        onClick = {
                            settingState.backgroundImageUriUserData.asynchronousSet(Uri.EMPTY)
                            isDarkSelection = false
                            launcher.launch("image/*")
                        }
                    )
                    Spacer(Modifier.height(6.dp))
                    BackgroundSelectRow(
                        label = stringResource(R.string.choose_dark_bg),
                        uri = settingState.backgroundDarkImageUri,
                        previewSize = 52.dp,
                        onClick = {
                            settingState.backgroundDarkImageUriUserData.asynchronousSet(Uri.EMPTY)
                            isDarkSelection = true
                            launcher.launch("image/*")
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun BackgroundSelectRow(
    label: String,
    uri: Uri,
    previewSize: Dp = 32.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = typography.headlineSmall)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(previewSize)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (uri.toString().isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.arrow_forward_24px),
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun BackgroundCard(
    title: String,
    desc: String? = null,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    contentBelow: (@Composable ColumnScope.() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(colorScheme.surfaceContainer)
            .clip(RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(title, style = typography.headlineSmall)
                desc?.let { Text(it, style = typography.bodyMedium, color = colorScheme.secondary) }
            }
            Spacer(Modifier.weight(1f))
            RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        }
        contentBelow?.let { it() }
    }
}

@Composable
private fun LightThemeSettingsItem(
    modifier: Modifier = Modifier
) {
    MaterialTheme (
        LocalLightColorScheme.current
    ) {
        DarkModeSettingItem(modifier)
    }
}

@Composable
private fun DarkThemeSettingsItem(
    modifier: Modifier = Modifier,
) {
    MaterialTheme (
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