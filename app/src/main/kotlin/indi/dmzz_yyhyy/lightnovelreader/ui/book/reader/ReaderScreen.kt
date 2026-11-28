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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.isVertical
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
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.ui.LocalReaderStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalTime
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    readingScreenUiState: ReaderScreenUiState,
    settingState: SettingState,
    onClickBackButton: () -> Unit,
    accumulateReadTime: (bookId: Identifier.Book, Int) -> Unit,
    updateTotalReadingTime: (bookId: Identifier.Book, Int) -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onChangeChapter: (chapterId: Identifier.Chapter) -> Unit,
    onReadingProgress: (Identifier.Chapter, Float, Boolean) -> Unit,
    flushReadingProgress: () -> Unit,
    onClickReaderStyleSettings: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var isImmersive by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHost.current
    val backBlockMode = settingState.backBlockMode
    var lastBackPressTime: Long by remember { mutableLongStateOf(0) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var showChapterSelectionBottomSheet by remember { mutableStateOf(false) }
    var selectedVolumeId by remember { mutableStateOf<Identifier.Volume?>(null) }
    var isNavigatingToReaderStyle by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

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
                    title = readingScreenUiState.currentChapterContent
                        ?.map { it.title }
                        ?.getOrElse { "Unknowing" }
                        ?: "Unknowing",
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
                    hasNextChapter = readingScreenUiState.currentChapterContent
                        ?.get()
                        ?.hasNextChapter(readingScreenUiState.bookVolumes?.get()) == true,
                    hasPrevChapter = readingScreenUiState.currentChapterContent
                        ?.get()
                        ?.hasPrevChapter(readingScreenUiState.bookVolumes?.get()) == true,
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
        Content(
            isImmersive = isImmersive,
            readingScreenUiState = readingScreenUiState,
            settingState = settingState,
            accumulateReadingTime = accumulateReadTime,
            updateTotalReadingTime = updateTotalReadingTime,
            onClickPrevChapter = onClickPrevChapter,
            onClickNextChapter = onClickNextChapter,
            onChangeIsImmersive = { isImmersive = !isImmersive },
            onReadingProgress = onReadingProgress,
            flushReadingProgress = flushReadingProgress,
        )
    }
    // 注意：ModalBottomSheet 在 Android 上是一个独立的、全屏且模态的 Dialog 窗口
    // (cancel() 被重写为空操作，面板隐藏后其返回键处理也会被禁用)。
    // 只要这个窗口没有被及时移除，它就会吞掉整个屏幕的点击与返回手势，表现为"界面卡死"。
    // 因此这里：
    // 1. 不再用 AnimatedVisibility 包裹 —— 它无法给另一个窗口做动画，只会推迟 Dialog 的移除时机；
    // 2. 每次打开都使用全新的 SheetState，避免沿用上一次残留(被打断/超时)的状态导致面板不可见但窗口仍在；
    // 3. 跳转到"阅读器样式"前先收起并移除弹窗，等窗口真正销毁后再导航，
    //    而不是让弹窗窗口悬挂在新页面之上，依赖页面切换动画结束才被动销毁
    //    (realme UI 5.0 等机型上会因此卡死：无法返回、无法点击)。
    if (showSettingsBottomSheet) {
        val settingsBottomSheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
        SettingsBottomSheet(
            sheetState = settingsBottomSheetState,
            onDismissRequest = {
                // ModalBottomSheet 在回调 onDismissRequest 之前已经完成了隐藏动画，直接移除即可
                showSettingsBottomSheet = false
            },
            settingState = settingState,
            onClickReaderStyleSettings = {
                if (!isNavigatingToReaderStyle) {
                    isNavigatingToReaderStyle = true
                    // 使用阅读页自身的 coroutineScope，而不是弹窗内部的 scope：
                    // 弹窗被移除时其内部 scope 会被取消，导致后续导航永远不会执行。
                    coroutineScope.launch {
                        try {
                            settingsBottomSheetState.hideSafely()
                            showSettingsBottomSheet = false
                            // 等待两帧：第一帧的重组会把弹窗移出组合(onDispose 中 dismiss Dialog)，
                            // 确保 Dialog 窗口已从 WindowManager 中移除、焦点回到 Activity 后再导航
                            withFrameNanos { }
                            withFrameNanos { }
                            Log.i("ReaderScreen", "Settings sheet closed, navigating to reader style")
                            onClickReaderStyleSettings()
                        } finally {
                            showSettingsBottomSheet = false
                            isNavigatingToReaderStyle = false
                        }
                    }
                }
            }
        )
    }

    if (showChapterSelectionBottomSheet) {
        val chaptersBottomSheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
        // 使用 chapterId 而不是 currentChapterContent：
        // 原先章节正文加载失败(Err)或切换章节加载中(null)时目录弹窗会直接消失/无法打开，
        // 用户无法通过目录换到其它章节；且标记位残留，正文加载完成后弹窗又会突然弹出。
        val readingChapterId = readingScreenUiState.chapterId
        if (readingChapterId == null) {
            LaunchedEffect(Unit) { showChapterSelectionBottomSheet = false }
        } else {
            ChapterSelectionBottomSheet(
                sheetState = chaptersBottomSheetState,
                selectedVolumeId = selectedVolumeId,
                bookVolumes = readingScreenUiState.bookVolumes?.get(),
                error = readingScreenUiState.bookVolumes?.getError(),
                readingChapterId = readingChapterId,
                onDismissRequest = {
                    showChapterSelectionBottomSheet = false
                    selectedVolumeId = readingScreenUiState.bookVolumes?.get()?.volumes
                        ?.firstOrNull { volume ->
                            volume.chapters.any {
                                it.id == readingChapterId
                            }
                        }?.volumeId
                },
                onClickChapter = { chapterId ->
                    onChangeChapter(chapterId)
                    coroutineScope.launch {
                        try {
                            chaptersBottomSheetState.hideSafely()
                        } finally {
                            // 无论隐藏动画是否被打断，都要移除弹窗窗口
                            showChapterSelectionBottomSheet = false
                        }
                    }
                },
                onChangeSelectedVolumeId = {
                    selectedVolumeId = it
                }
            )
        }
        LaunchedEffect(readingScreenUiState.bookVolumes) {
            val readingChapterId = readingScreenUiState.chapterId ?: return@LaunchedEffect
            readingScreenUiState.bookVolumes?.onOk { bookVolumes ->
                selectedVolumeId = bookVolumes.volumes.firstOrNull { volume ->
                    volume.chapters.any {
                        it.id == readingChapterId
                    }
                }?.volumeId
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Content(
    isImmersive: Boolean,
    readingScreenUiState: ReaderScreenUiState,
    settingState: SettingState,
    updateTotalReadingTime: (bookId: Identifier.Book, Int) -> Unit,
    accumulateReadingTime: (bookId: Identifier.Book, Int) -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onChangeIsImmersive: () -> Unit,
    onReadingProgress: (Identifier.Chapter, Float, Boolean) -> Unit,
    flushReadingProgress: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val window = activity.window
    val density = LocalDensity.current

    val stableInsets = WindowInsets.systemBarsIgnoringVisibility.union(WindowInsets.displayCutout)
    val stableSafeTopDp = with(density) { stableInsets.getTop(density).toDp() }
    val stableSafeBottomDp = with(density) { stableInsets.getBottom(density).toDp() }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { flushReadingProgress() }

    val originalSystemBarsBehavior = remember(window) {
        WindowCompat.getInsetsController(window, window.decorView).systemBarsBehavior
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

    DisposableEffect(window) {
        onDispose {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = originalSystemBarsBehavior
        }
    }

    LifecycleResumeEffect(Unit) {
        isRunning = true
        onPauseOrDispose {
            isRunning = false
            if (totalReadingTime <= 60) {
                readingScreenUiState.bookId?.let {
                    updateTotalReadingTime(it, totalReadingTime)
                }
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
                readingScreenUiState.bookId?.let {
                    updateTotalReadingTime(it, totalReadingTime)
                }
                totalReadingTime = 0
            }
            delay(1.seconds)
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            readingScreenUiState.bookId?.let {
                accumulateReadingTime(it, 1)
            }
            delay(1.seconds)
        }
    }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {
            readingScreenUiState.bookId?.let {
                accumulateReadingTime(it, -1)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (totalReadingTime <= 60) {
                readingScreenUiState.bookId?.let {
                    updateTotalReadingTime(it, totalReadingTime)
                }
            } else {
                Log.e("ReaderScreen", "time counter error, time now is $totalReadingTime over 60s")
            }
        }
    }

    val isVertical = LocalReaderStyle.current.writeMode.isVertical
    val loopBackground = settingState.enableBackgroundImage && !settingState.isUsingFlipPage &&
        settingState.backgroundImageDisplayMode == MenuOptions.ReaderBgImageDisplayModeOptions.Loop
    var backgroundLength by remember { mutableIntStateOf(0) }
    var phase by remember(loopBackground, isVertical) { mutableFloatStateOf(0f) }
    val backgroundScroll = remember(loopBackground, isVertical) {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val distance = if (isVertical) consumed.x else consumed.y
                if (loopBackground && backgroundLength > 0 && distance != 0f) {
                    phase = ((phase + distance) % backgroundLength + backgroundLength) % backgroundLength
                }
                return Offset.Zero
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .nestedScroll(backgroundScroll)
            .onSizeChanged { backgroundLength = if (isVertical) it.width else it.height }
    ) {
        if (settingState.enableBackgroundImage) {
            val painter = rememberReaderBackgroundPainter(settingState)
            val copies = if (loopBackground) 2 else 1
            repeat(copies) { copy ->
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val offset = if (loopBackground) phase - copy * backgroundLength else 0f
                            if (isVertical) translationX = offset else translationY = offset
                        },
                )
            }
        }
        val isEnableIndicator =
            settingState.batteryIndicatorDisplayMode == "classic" ||
                    settingState.enableTimeIndicator ||
                    settingState.enableReadingChapterProgressIndicator ||
                    settingState.enableChapterTitleIndicator

        Box(Modifier.fillMaxSize()) {
            ChapterContent(
                uiState = readingScreenUiState,
                settingState = settingState,
                paddingValues =
                    if (settingState.autoPadding)
                        PaddingValues(
                            top = stableSafeTopDp,
                            bottom = stableSafeBottomDp + if (isEnableIndicator) 40.dp else 0.dp,
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
                onClickNextChapter = onClickNextChapter,
                onReadingProgress = onReadingProgress,
            )

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = isEnableIndicator &&
                    readingScreenUiState.currentChapterContent?.isOk == true &&
                    readingScreenUiState.readingProgress != null,
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
                    chapterTitle = readingScreenUiState.currentChapterContent
                        ?.map { it.title }
                        ?.getOrElse { "Unknowing" }
                        ?: "Unknowing",
                    enableReadingChapterProgressIndicator = settingState.enableReadingChapterProgressIndicator,
                    readingChapterProgress = readingScreenUiState.readingProgress ?: 0f,
                )
            }
        }
    }
}

/**
 * 安全地收起底部弹窗：
 * - 最多等待 [timeoutMillis]，动画因机型/系统动画设置异常而迟迟不结束时也不会一直挂起；
 * - 被其它拖拽或动画打断(MutationInterruptedException)时不向外抛出，
 *   保证调用方后续"移除弹窗 / 执行导航"的逻辑一定会执行。
 * 若外层协程本身已被取消，则照常抛出取消异常。
 */
@OptIn(ExperimentalMaterial3Api::class)
internal suspend fun SheetState.hideSafely(timeoutMillis: Long = 400L) {
    try {
        withTimeoutOrNull(timeoutMillis) { hide() }
    } catch (e: CancellationException) {
        currentCoroutineContext().ensureActive()
        Log.w("ReaderScreen", "Bottom sheet hide animation was interrupted", e)
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
                onClick = onClickBackButton
            ) {
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
    hasPrevChapter: Boolean,
    hasNextChapter: Boolean,
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
                enabled = hasPrevChapter
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
                enabled = hasNextChapter
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
    // 原先时间与电量只在 Indicator 重组时读取一次，停留在同一页(尤其是翻页模式)时
    // 时钟会停在旧时间。这里每到整分钟刷新一次。
    val now by produceState(initialValue = LocalTime.now(), enableTimeIndicator, enableBatteryIndicator) {
        if (!enableTimeIndicator && !enableBatteryIndicator) return@produceState
        while (true) {
            val current = LocalTime.now()
            value = current
            val untilNextMinute = (60 - current.second) * 1000L - current.nano / 1_000_000
            delay(untilNextMinute.coerceAtLeast(200L))
        }
    }
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
                val context = LocalContext.current
                // getIntProperty 在不支持的设备上返回 Integer.MIN_VALUE，
                // 直接交给 RollingNumber 会因 abs(Int.MIN_VALUE) 溢出而崩溃，这里只接受 0..100。
                val batLevel: Int = remember(now) {
                    val batteryManager = context.getSystemService(BATTERY_SERVICE) as? BatteryManager
                    batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                        ?.takeIf { it in 0..100 } ?: -1
                }
                if (batLevel >= 0) {
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
                }
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .semantics { contentDescription = "battery indicator" },
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
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .semantics { contentDescription = "time indicator" },
                    text = String.format(
                        Locale.US,
                        "%d:%02d",
                        now.hour,
                        now.minute
                    ),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "chapter indicator" },
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
                    modifier = Modifier.semantics { contentDescription = "progress indicator" },
                    text = "%",
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.W500,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
