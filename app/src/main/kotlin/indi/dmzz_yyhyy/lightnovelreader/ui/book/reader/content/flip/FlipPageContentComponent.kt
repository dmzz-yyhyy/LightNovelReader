package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentError
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentLoading
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderBackgroundPainter
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponent
import io.nightfish.lightnovelreader.api.content.component.AbstractDivisibleContentComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FlipPageContentComponent(
    modifier: Modifier,
    uiState: FlipPageContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
) {
    uiState.readingChapterContent?.onOk {
        SimpleFlipPageTextComponent(
            modifier = modifier,
            paddingValues = paddingValues,
            uiState = uiState,
            chapterContent = it,
            settingState = settingState,
            changeIsImmersive = changeIsImmersive,
            onClickNextChapter = onClickNextChapter,
            onClickPrevChapter = onClickPrevChapter,
        )
    }?.onErr {
        ChapterContentError(it)
    } ?: ChapterContentLoading()
}

@Composable
private fun SimpleFlipPageTextComponent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    uiState: FlipPageContentUiState,
    chapterContent: ChapterContentUiState,
    settingState: SettingState,
    changeIsImmersive: () -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    var contentKey by remember { mutableIntStateOf(0) }
    var slippedContentComponentList by remember { mutableStateOf(emptyList<AbstractContentComponent<*>>()) }
    LaunchedEffect(chapterContent.content, resources, density) {
        scope.launch(Dispatchers.IO) {
            val width = resources.displayMetrics
                .widthPixels
                .minus(
                    with(density) {
                        (paddingValues.calculateStartPadding(layoutDirection) + paddingValues.calculateEndPadding(layoutDirection)).toPx()
                    }.toInt()
                )
            val height = resources.displayMetrics
                .heightPixels
                .minus(
                    with(density) {
                        (paddingValues.calculateTopPadding() + paddingValues.calculateBottomPadding()).toPx()
                    }.toInt()
                )
            val key = chapterContent.hashCode() + width + height
            if (key == contentKey) return@launch
            val result = mutableListOf<AbstractContentComponent<*>>()
            chapterContent.content.forEach {
                if (it is AbstractDivisibleContentComponent<*, *>) {
                    result.addAll(it.split(height, width))
                } else {
                    result.add(it)
                }
            }
            slippedContentComponentList = result
            uiState.updatePageState(PagerState { result.size })
        }
    }
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = LocalSnackbarHost.current

    val painter = rememberReaderBackgroundPainter(settingState)
    val bgPainter = remember(settingState.enableBackgroundImage, settingState.backgroundImageDisplayMode) {
        if (settingState.enableBackgroundImage &&
            settingState.backgroundImageDisplayMode == MenuOptions.ReaderBgImageDisplayModeOptions.Loop
        ) painter else null
    }

    val windowInfo = LocalWindowInfo.current
    val screenWidthPx = windowInfo.containerSize.width.toFloat()
    val readerFirstPageText = stringResource(R.string.reader_first_page)
    val previousChapterText = stringResource(R.string.previous_chapter)
    fun lastPage(pagerState: PagerState) {
        if (pagerState.currentPage != 0) {
            scope.launch {
                if (settingState.flipAnime != MenuOptions.FlipAnimationOptions.None) {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                } else {
                    pagerState.scrollToPage(pagerState.currentPage - 1)
                }
            }
        } else if (settingState.fastChapterChange && slippedContentComponentList.isNotEmpty()) {
            uiState.loadPrevChapter.invoke()
        } else {
            showSnackbar(
                coroutineScope = scope,
                hostState = snackbarHostState,
                duration = SnackbarDuration.Short,
                message = readerFirstPageText,
                actionLabel = previousChapterText
            ) {
                if (it == SnackbarResult.ActionPerformed) {
                    onClickPrevChapter()
                }
            }

        }
    }

    val readerLastPageText = stringResource(R.string.reader_last_page)
    val nextPageText = stringResource(R.string.next_chapter)

    fun nextPage(pagerState: PagerState) {
        if (pagerState.currentPage + 1 < pagerState.pageCount) {
            scope.launch {
                if (settingState.flipAnime != MenuOptions.FlipAnimationOptions.None) {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                } else {
                    pagerState.scrollToPage(pagerState.currentPage + 1)
                }
            }
        } else if (settingState.fastChapterChange && slippedContentComponentList.isNotEmpty()) {
            uiState.loadNextChapter.invoke()
        } else {
            showSnackbar(
                coroutineScope = scope,
                hostState = snackbarHostState,
                duration = SnackbarDuration.Short,
                message = readerLastPageText,
                actionLabel = nextPageText
            ) {
                if (it == SnackbarResult.ActionPerformed) {
                    onClickNextChapter()
                }
            }
        }
    }
    var volumeJob by remember { mutableStateOf<Job?>(null) }
    val intervalMs = (settingState.volumeKeyContinuousFlipInterval * 1000).toLong()

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (bgPainter != null)
                    Modifier.paint(
                        painter = bgPainter,
                        contentScale = ContentScale.Crop
                    )
                else Modifier
            )
    ) {
        HorizontalPager(
            state = uiState.pagerState,
            key = { it },
            modifier = modifier
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { event ->
                    if (!settingState.isUsingVolumeKeyFlip) {
                        false
                    } else if (event.key == Key.VolumeUp || event.key == Key.VolumeDown) {
                        when (event.type) {
                            KeyEventType.KeyDown -> {
                                if (event.nativeKeyEvent.repeatCount == 0) {
                                    if (event.key == Key.VolumeUp) lastPage(uiState.pagerState)
                                    else nextPage(uiState.pagerState)

                                    if (intervalMs > 0) {
                                        volumeJob?.cancel()
                                        volumeJob = scope.launch {
                                            while (isActive) {
                                                delay(intervalMs.milliseconds)
                                                if (event.key == Key.VolumeUp) lastPage(uiState.pagerState)
                                                else nextPage(uiState.pagerState)
                                            }
                                        }
                                    }
                                }
                                true
                            }
                            KeyEventType.KeyUp -> {
                                volumeJob?.cancel()
                                volumeJob = null
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
                .draggable(
                    enabled = settingState.isUsingFlipPage,
                    interactionSource = remember { MutableInteractionSource() },
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState {},
                    onDragStopped = {
                        if (it.absoluteValue > 60) changeIsImmersive.invoke()
                    }
                )
                .pointerInput(
                    settingState.isUsingClickFlipPage,
                    settingState.isUsingFlipPage,
                    settingState.flipAnime,
                    settingState.fastChapterChange
                ) {
                    detectTapGestures(
                        onTap = {
                            if (settingState.isUsingFlipPage && settingState.isUsingClickFlipPage)
                                when {
                                    it.x < screenWidthPx / 3f -> lastPage(uiState.pagerState)
                                    it.x > screenWidthPx * 2f / 3f -> nextPage(uiState.pagerState)
                                    else -> changeIsImmersive.invoke()
                                }
                            else changeIsImmersive.invoke()
                        }
                    )
                },
        ) {
            Box(Modifier.fillMaxSize()) {
                if (settingState.enableBackgroundImage && settingState.backgroundImageDisplayMode == MenuOptions.ReaderBgImageDisplayModeOptions.Loop) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = rememberReaderBackgroundPainter(settingState),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                slippedContentComponentList.getOrNull(it)?.Content(
                    modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

