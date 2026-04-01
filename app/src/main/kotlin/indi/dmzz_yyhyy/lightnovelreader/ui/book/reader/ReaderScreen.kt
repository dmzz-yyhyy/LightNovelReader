package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import coil3.compose.AsyncImagePainter
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedTextLine
import indi.dmzz_yyhyy.lightnovelreader.ui.components.LnrSnackbar
import indi.dmzz_yyhyy.lightnovelreader.ui.components.RollingNumber
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalClaimSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.readerBackgroundColor
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderBackgroundPainter
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import io.nightfish.lightnovelreader.api.book.ChapterContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Locale

@Suppress("AssignedValueIsNeverRead")
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    readingScreenUiState: ReaderScreenUiState,
    settingState: SettingState,
    onClickBackButton: () -> Unit,
    accumulateReadTime: (bookId: String, Int) -> Unit,
    updateTotalReadingTime: (bookId: String, Int) -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onChangeChapter: (chapterId: String) -> Unit,
    onClickThemeSettings: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var isImmersive by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHost.current
    val backBlockMode = settingState.backBlockMode
    var lastBackPressTime: Long by remember { mutableLongStateOf(0) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var showChapterSelectionBottomSheet by remember { mutableStateOf(false) }
    var selectedVolumeId by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val settingsBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val chaptersBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val claim = LocalClaimSnackbarHost.current

    DisposableEffect(Unit) {
        claim(true)
        onDispose { claim(false) }
    }

    BackHandler {
        when (backBlockMode) {
            MenuOptions.ReaderBackBlockMode.None -> {
                isImmersive = false
                onClickBackButton()
            }
            MenuOptions.ReaderBackBlockMode.DoublePress -> {
                val now = System.currentTimeMillis()
                if (!isImmersive || now - lastBackPressTime < 1500) {
                    onClickBackButton()
                } else {
                    lastBackPressTime = now
                    showSnackbar(
                        coroutineScope = coroutineScope,
                        hostState = snackbarHostState,
                        message = context.getString(R.string.reader_back_press_again),
                        duration = SnackbarDuration.Short
                    )
                }
            }

            MenuOptions.ReaderBackBlockMode.FullyBlocked -> {}
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            isImmersive = false
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AnimatedVisibility(
                visible = !isImmersive,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                TopBar(
                    onClickBackButton = onClickBackButton,
                    title = readingScreenUiState.contentUiState.readingChapterContent.title,
                    scrollBehavior
                )
            }
        },
        snackbarHost = {
            SnackbarHost(LocalSnackbarHost.current) { data ->
                LnrSnackbar(
                    data,
                    modifier = Modifier
                        .padding(bottom = animateDpAsState(if (isImmersive) 56.dp else 12.dp).value)
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isImmersive,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                BottomBar(
                    chapterContent = readingScreenUiState.contentUiState.readingChapterContent,
                    onClickPrevChapter = onClickPrevChapter,
                    onClickNextChapter = onClickNextChapter,
                    onClickSettings = { showSettingsBottomSheet = true },
                    onClickChapterSelector = { showChapterSelectionBottomSheet = true },
                )
            }
        },
        containerColor = readerBackgroundColor(settingState),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        if (settingState.enableBackgroundImage) {
            val bgPainter = rememberReaderBackgroundPainter(settingState)
            val bgState by remember(bgPainter) {
                (bgPainter as? AsyncImagePainter)?.state
            }?.collectAsState() ?: remember { mutableStateOf(null) }

            key(bgState) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = bgPainter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }

        Content(
            isImmersive = isImmersive,
            readingScreenUiState = readingScreenUiState,
            settingState = settingState,
            accumulateReadingTime = accumulateReadTime,
            updateTotalReadingTime = updateTotalReadingTime,
            onClickPrevChapter = onClickPrevChapter,
            onClickNextChapter = onClickNextChapter,
            onChangeIsImmersive = { isImmersive = !isImmersive }
        )
    }
    AnimatedVisibility(visible = showSettingsBottomSheet) {
        SettingsBottomSheet(
            sheetState = settingsBottomSheetState,
            onDismissRequest = {
                coroutineScope.launch { settingsBottomSheetState.hide() }.invokeOnCompletion {
                    if (!settingsBottomSheetState.isVisible) {
                        showSettingsBottomSheet = false
                    }
                }
                showSettingsBottomSheet = false
            },
            settingState = settingState,
            onClickThemeSettings = onClickThemeSettings
        )
    }

    AnimatedVisibility(visible = showChapterSelectionBottomSheet) {
        ChapterSelectionBottomSheet(
            sheetState = chaptersBottomSheetState,
            selectedVolumeId = selectedVolumeId,
            bookVolumes = readingScreenUiState.bookVolumes,
            readingChapterId = readingScreenUiState.contentUiState.readingChapterContent.id,
            onDismissRequest = {
                coroutineScope.launch { chaptersBottomSheetState.hide() }.invokeOnCompletion {
                    if (!chaptersBottomSheetState.isVisible) {
                        showChapterSelectionBottomSheet = false
                    }
                }
                showChapterSelectionBottomSheet = false
                selectedVolumeId =
                    readingScreenUiState.bookVolumes.volumes.firstOrNull { volume -> volume.chapters.any { it.id == readingScreenUiState.contentUiState.readingChapterContent.id } }?.volumeId
                        ?: ""
            },
            onClickChapter = onChangeChapter,
            onChangeSelectedVolumeId = {
                selectedVolumeId = it
            }
        )
    }
    LaunchedEffect(readingScreenUiState.bookVolumes) {
        selectedVolumeId = readingScreenUiState.bookVolumes.volumes.firstOrNull { volume -> volume.chapters.any { it.id == readingScreenUiState.contentUiState.readingChapterContent.id } }?.volumeId ?: ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(
    isImmersive: Boolean,
    readingScreenUiState: ReaderScreenUiState,
    settingState: SettingState,
    updateTotalReadingTime: (bookId: String, Int) -> Unit,
    accumulateReadingTime: (bookId: String, Int) -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onChangeIsImmersive: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val window = activity.window
    val density = LocalDensity.current

    val stableSafeTopDp by remember {
        mutableStateOf(
            with(density) {
                WindowInsetsCompat
                    .toWindowInsetsCompat(activity.window.decorView.rootWindowInsets)
                    .getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars())
                    .top
                    .toDp()
            }
        )
    }

    val originalUiFlags = remember {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility
    }

    var isRunning by remember { mutableStateOf(false) }
    var totalReadingTime by remember { mutableIntStateOf(0) }

    LaunchedEffect(
        isImmersive,
        settingState.enableHideStatusBar,
        settingState.batteryIndicatorDisplayMode
    ) {
        updateReaderImmersiveMode(
            window = window,
            immersive = isImmersive,
            enableHideStatusBar = settingState.enableHideStatusBar,
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    DisposableEffect(Unit) {
        @Suppress("deprecation")
        onDispose {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.show(WindowInsetsCompat.Type.systemBars())
            window.decorView.systemUiVisibility = originalUiFlags
        }
    }

    LifecycleResumeEffect(Unit) {
        isRunning = true
        onPauseOrDispose {
            isRunning = false
            if (totalReadingTime <= 60) {
                updateTotalReadingTime(readingScreenUiState.bookId, totalReadingTime)
            } else {
                Log.e("ReaderScreen", "time counter error, time now is $totalReadingTime over 60s")
            }
            totalReadingTime = 0
        }
    }
    LaunchedEffect(settingState.keepScreenOn) {
        if (settingState.keepScreenOn)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    LaunchedEffect(isRunning) {
        while (isRunning) {
            totalReadingTime += 1
            if (totalReadingTime > 60) {
                updateTotalReadingTime(readingScreenUiState.bookId, totalReadingTime)
                totalReadingTime = 0
            }
            delay(1000)
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            accumulateReadingTime(readingScreenUiState.bookId, 1)
            delay(1000)
        }
    }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {
            accumulateReadingTime(readingScreenUiState.bookId, -1)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (totalReadingTime <= 60) {
                updateTotalReadingTime(readingScreenUiState.bookId, totalReadingTime)
            } else {
                Log.e("ReaderScreen", "time counter error, time now is $totalReadingTime over 60s")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val isEnableIndicator =
            settingState.enableTimeIndicator ||
                    settingState.enableReadingChapterProgressIndicator ||
                    settingState.enableChapterTitleIndicator

        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                readingScreenUiState.contentUiState,
                label = "ContentAnimate"
            ) { contentUiState ->
                ContentComponent(
                    uiState = contentUiState,
                    settingState = settingState,
                    paddingValues =
                        if (settingState.autoPadding)
                            PaddingValues(
                                top = stableSafeTopDp,
                                bottom = with(density) { WindowInsets.safeContent.getBottom(density).toDp() } + if (isEnableIndicator) 40.dp else 0.dp,
                                start = 16.dp,
                                end = 16.dp
                            )
                        else PaddingValues(
                            top = settingState.topPadding.dp,
                            bottom = if (isEnableIndicator)
                                (settingState.bottomPadding + 40).dp
                            else settingState.bottomPadding.dp,
                            start = settingState.leftPadding.dp,
                            end = settingState.rightPadding.dp
                        ),
                    changeIsImmersive = onChangeIsImmersive,
                    onClickPrevChapter = onClickPrevChapter,
                    onClickNextChapter = onClickNextChapter
                )
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = isEnableIndicator,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Indicator(
                    Modifier
                        .padding(
                            if (settingState.autoPadding)
                                PaddingValues(
                                    bottom = 8.dp,
                                    start = 16.dp,
                                    end = 16.dp
                                )
                            else PaddingValues(
                                bottom = settingState.bottomPadding.dp,
                                start = settingState.leftPadding.dp,
                                end = settingState.rightPadding.dp
                            )
                        ),
                    enableBatteryIndicator = settingState.batteryIndicatorDisplayMode == "classic",
                    enableTimeIndicator = settingState.enableTimeIndicator,
                    enableChapterTitle = settingState.enableChapterTitleIndicator,
                    chapterTitle = readingScreenUiState.contentUiState.readingChapterContent.title,
                    enableReadingChapterProgressIndicator = settingState.enableReadingChapterProgressIndicator,
                    readingChapterProgress = readingScreenUiState.contentUiState.readingProgress,
                )
            }
        }
    }
}

private fun updateReaderImmersiveMode(
    window: Window,
    immersive: Boolean,
    enableHideStatusBar: Boolean,
) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)

    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    if (immersive) {
        if (enableHideStatusBar) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    } else {
        controller.show(WindowInsetsCompat.Type.statusBars())
    }

    if (immersive) {
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    } else {
        controller.show(WindowInsetsCompat.Type.navigationBars())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBackButton: () -> Unit,
    title: String,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onClickBackButton) {
                Icon(painterResource(id = R.drawable.arrow_back_24px), "back")
            }
        },
        title = {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(title, label = "TitleAnimate") { text ->
                    Text(
                        text = text,
                        style = typography.displayLarge,
                        fontWeight = FontWeight.W400,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun BottomBar(
    chapterContent: ChapterContent,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onClickSettings: () -> Unit,
    onClickChapterSelector: () -> Unit
) {
    BottomAppBar {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onClickPrevChapter,
                enabled = chapterContent.hasPrevChapter()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_24px),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.previous_chapter),
                        style = typography.labelSmall
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = false,
                    onClick = {
                        // TODO 添加至书签
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_bookmark_24px),
                        contentDescription = "mark"
                    )
                }

                IconButton(onClick = onClickChapterSelector) {
                    Icon(
                        painter = painterResource(id = R.drawable.menu_24px),
                        contentDescription = "menu"
                    )
                }

                IconButton(onClick = onClickSettings) {
                    Icon(
                        painter = painterResource(R.drawable.outline_settings_24px),
                        contentDescription = "setting"
                    )
                }
            }

            TextButton(
                onClick = onClickNextChapter,
                enabled = chapterContent.hasNextChapter()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward_24px),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.next_chapter),
                        style = typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun Indicator(
    modifier: Modifier = Modifier,
    enableBatteryIndicator: Boolean,
    enableTimeIndicator: Boolean,
    enableChapterTitle: Boolean,
    chapterTitle: String,
    enableReadingChapterProgressIndicator: Boolean,
    readingChapterProgress: Float
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (enableBatteryIndicator) {
                val batteryManager = LocalContext.current.getSystemService(BATTERY_SERVICE) as BatteryManager
                val batLevel: Int = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                RollingNumber(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    number = batLevel,
                    style = typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    length = 3
                )
                Text(
                    text = "%",
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.W500,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter =
                        when {
                            (batLevel in 0..15) -> painterResource(R.drawable.battery_android_alert_24px)
                            (batLevel in 16..35) -> painterResource(R.drawable.battery_android_3_24px)
                            (batLevel in 36..65) -> painterResource(R.drawable.battery_android_4_24px)
                            (batLevel in 66..80) -> painterResource(R.drawable.battery_android_5_24px)
                            (batLevel in 81..95) -> painterResource(R.drawable.battery_android_6_24px)
                            (batLevel in 96..100) -> painterResource(R.drawable.battery_android_full_24px)
                            else -> painterResource(R.drawable.battery_android_question_24px)
                        },
                    tint = colorScheme.onSurfaceVariant,
                    contentDescription = null
                )
                Spacer(Modifier.width(14.dp))
            }
            if (enableTimeIndicator) {
                AnimatedText(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = String.format(Locale.US, "%d:%02d", LocalTime.now().hour, LocalTime.now().minute),
                    style = typography.bodyLarge.copy(
                        letterSpacing = 1.sp
                    ),
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (enableChapterTitle) {
                AnimatedTextLine(
                    modifier = Modifier.fillMaxWidth(),
                    text = chapterTitle,
                    textAlign = TextAlign.End,
                    style = typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (enableReadingChapterProgressIndicator) {
                RollingNumber(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    number = (readingChapterProgress * 100).toInt(),
                    style = typography.bodyLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = colorScheme.onSurfaceVariant,
                    length = 3
                )
                Text(
                    text = "%",
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.W500,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}