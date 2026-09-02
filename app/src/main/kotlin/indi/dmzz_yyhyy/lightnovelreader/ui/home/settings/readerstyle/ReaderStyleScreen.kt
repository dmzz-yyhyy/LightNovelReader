package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.readerstyle

import android.content.Context
import android.net.Uri
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.rememberAsyncImagePainter
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.runCatching
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import io.nightfish.lightnovelreader.api.ui.components.SettingsMenuEntry
import io.nightfish.lightnovelreader.api.ui.components.SettingsSliderEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingsCategory
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.KRAFT_PAPER_CACHE_KEY
import indi.dmzz_yyhyy.lightnovelreader.utils.KRAFT_PAPER_URL
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import indi.dmzz_yyhyy.lightnovelreader.utils.readerBackgroundColor
import indi.dmzz_yyhyy.lightnovelreader.utils.readerTextColor
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderBackgroundPainter
import io.nightfish.lightnovelreader.api.ui.LocalReaderStyle
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry
import io.nightfish.lightnovelreader.api.ui.components.SettingsSwitchEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun ReaderStyleScreen(
    settingState: SettingState,
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
                ReaderThemeSettingsList(settingState, onClickChangeBackgroundColor)
            }
            item {
                BackgroundSettings(settingState, context)
            }
            item {
                ReaderTextSettings(settingState, context, onClickChangeTextColor)
            }
            navigationBarSpacer()
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

                val key = KRAFT_PAPER_CACHE_KEY

                val memHit = loader.memoryCache?.get(MemoryCache.Key(key)) != null
                val diskHit = loader.diskCache?.openSnapshot(key)?.use { true } ?: false

                if (!memHit && !diskHit) {
                    snackbarHostState.showSnackbar("正在下载纸张背景…")

                    loader.enqueue(
                        ImageRequest.Builder(context)
                            .data(KRAFT_PAPER_URL)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .memoryCacheKey(key)
                            .diskCacheKey(key)
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
fun ReaderTextSettings(
    settingState: SettingState,
    context: Context,
    onClickChangeTextColor: () -> Unit
) {
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
                val fontFile = saveFontToLocal(context, uri).get() ?: run {
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
                    settingState.fontUriUserData.set(fontFile.toUri())
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
            selectedOptionKey = if (settingState.fontUri.toString().isEmpty())
                MenuOptions.SelectText.Default else MenuOptions.SelectText.Customize,
            onOptionChange = {
                when (it) {
                    MenuOptions.SelectText.Default -> settingState.fontUriUserData.asynchronousSet(
                        Uri.EMPTY
                    )

                    MenuOptions.SelectText.Customize -> fontPicker.launch("*/*")
                }
            }
        )
    }
    BasePageItem(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 270.dp)
            .padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .fillMaxWidth(),
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(readerBackgroundColor(settingState))
                )
            }
            val readerStyle = LocalReaderStyle.current
            val isDark = LocalAppTheme.current.isDark
            val density = LocalDensity.current
            Column {
                Text(
                    buildAnnotatedString {
                        withStyle(
                            readerStyle.toParagraphStyle()
                        ) {
                            withStyle(
                                readerStyle.toSpanStyle(isDark)
                            ) {
                                append("LNR是一款作者为了能在每个深夜得到安慰所开发出的软件。")
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(
                            bottom = with(density) {
                                readerStyle.spacingAfterParagraph.toDp()
                            },
                            top = with(density) {
                                readerStyle.spacingBeforeParagraph.toDp()
                            }
                        )
                )
                Text(
                    buildAnnotatedString {
                        withStyle(
                            readerStyle.toParagraphStyle()
                        ) {
                            withStyle(
                                readerStyle.toSpanStyle(isDark)
                            ) {
                                append("我们总是在夜晚保持清醒，拼命的抓住每一寸属于自己的时间，寻找每一个能寄托心灵的空间。")
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(
                            bottom = with(density) {
                                readerStyle.spacingAfterParagraph.toDp()
                            },
                            top = with(density) {
                                readerStyle.spacingBeforeParagraph.toDp()
                            }
                        )
                )
                Text(
                    buildAnnotatedString {
                        withStyle(
                            readerStyle.toParagraphStyle()
                        ) {
                            withStyle(
                                readerStyle.toSpanStyle(isDark)
                            ) {
                                append("我们不清楚各自在白昼受到怎样的灼烧，但我们同样享受那首彻夜之歌。")
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(
                            bottom = with(density) {
                                readerStyle.spacingAfterParagraph.toDp()
                            },
                            top = with(density) {
                                readerStyle.spacingBeforeParagraph.toDp()
                            }
                        )
                )
                Text(
                    buildAnnotatedString {
                        withStyle(
                            readerStyle.toParagraphStyle()
                        ) {
                            withStyle(
                                readerStyle.toSpanStyle(isDark)
                            ) {
                                append("LightNovelReader是送给所有小说爱好者的礼物。")
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(
                            bottom = with(density) {
                                readerStyle.spacingAfterParagraph.toDp()
                            },
                            top = with(density) {
                                readerStyle.spacingBeforeParagraph.toDp()
                            }
                        )
                )
            }
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
            decimalFormat = "0.#",
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
            title = stringResource(R.string.settings_line_height),
            unit = "em",
            valueRange = 1f..3f,
            value = settingState.lineHeight,
            valueFormat = { (it * 10).roundToInt().toFloat() / 10 },
            floatUserData = settingState.lineHeightUserData,
        )

        SettingsSliderEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.format_line_spacing_24px),
            title = stringResource(R.string.settings_paragraph_spacing),
            unit = "sp",
            valueRange = 0f..64f,
            value = settingState.spacingAfterParagraph,
            floatUserData = settingState.spacingAfterParagraphUserData,
        )

        SettingsSliderEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.format_line_spacing_24px),
            title = stringResource(R.string.settings_text_indent),
            unit = "em",
            valueRange = 0f..8f,
            value = settingState.firstLineTextIndent,
            floatUserData = settingState.firstLineTextIndentUserData,
        )
    }
}

private suspend fun saveFontToLocal(context: Context, uri: Uri): Result<File, Throwable> =
    withContext(Dispatchers.IO) {
        val fontDir = context.filesDir.resolve("fonts").also {
            it.mkdirs()
        }
        fontDir.listFiles()?.forEach {
            it.delete()
        }
        val fontFile = fontDir.resolve(uri.hashCode().toString()).also {
            it.createNewFile()
        }
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                fontFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            fontFile
        }.onErr {
            Log.e("ReaderTextFont", "Failed to import font", it)
        }
    }

private suspend fun saveBackgroundToLocal(
    context: Context,
    uri: Uri,
    fileName: String,
): File? = withContext(Dispatchers.IO) {
    val target = context.filesDir.resolve("$fileName-${UUID.randomUUID()}")
    val pending = context.filesDir.resolve("${target.name}.pending")

    try {
        val input = context.contentResolver.openInputStream(uri)
            ?: error("Unable to open selected background image")
        input.use { source ->
            pending.outputStream().use { destination -> source.copyTo(destination) }
        }
        require(pending.length() > 0L) { "Selected background image is empty" }

        if (!pending.renameTo(target)) {
            pending.copyTo(target, overwrite = true)
            pending.delete()
        }
        target
    } catch (e: Exception) {
        pending.delete()
        target.delete()
        Log.e("ReaderBackground", "Failed to import background image", e)
        null
    }
}

private fun deleteReplacedBackground(context: Context, uri: Uri, fileName: String) {
    val oldFile = uri.path?.let(::File) ?: return
    val filesDir = context.filesDir.canonicalFile
    val canonicalOldFile = try {
        oldFile.canonicalFile
    } catch (_: Exception) {
        return
    }
    if (canonicalOldFile.parentFile == filesDir && canonicalOldFile.name.startsWith(fileName)) {
        canonicalOldFile.delete()
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
        scope.launch {
            val fileName =
                if (isDarkSelection) "readerDarkBackgroundImage" else "readerBackgroundImage"
            val replacedUri =
                if (isDarkSelection) settingState.backgroundDarkImageUri
                else settingState.backgroundImageUri
            val file = saveBackgroundToLocal(context, uri, fileName) ?: return@launch
            val fileUri = file.toUri()
            withContext(Dispatchers.IO) {
                if (isDarkSelection)
                    settingState.backgroundDarkImageUriUserData.set(fileUri)
                else
                    settingState.backgroundImageUriUserData.set(fileUri)
                deleteReplacedBackground(context, replacedUri, fileName)
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.settings_reader_style),
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
