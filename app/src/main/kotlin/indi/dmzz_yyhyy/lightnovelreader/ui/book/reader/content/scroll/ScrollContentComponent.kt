@file:Suppress("AssignedValueIsNeverRead")

package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.github.michaelbull.result.get
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentError
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.readerTextColor
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderBackgroundPainter
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderFontFamily
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import io.nightfish.lightnovelreader.api.ui.LocalComponentRender
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ScrollContentComponent(
    modifier: Modifier,
    uiState: ScrollContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit
) {
    ScrollContentTextComponent(
        modifier = modifier,
        uiState = uiState,
        settingState = settingState,
        paddingValues = paddingValues,
        changeIsImmersive = changeIsImmersive,
        onClickPrevChapter = onClickPrevChapter,
        onClickNextChapter = onClickNextChapter
    )
}

@Composable
fun ScrollContentTextComponent(
    modifier: Modifier,
    uiState: ScrollContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit
) {
    val snackbarHostState = LocalSnackbarHost.current
    val density = LocalDensity.current
    val listState = uiState.lazyListState
    val scope = rememberCoroutineScope()
    val loopBackgroundEnabled = settingState.enableBackgroundImage &&
        settingState.backgroundImageDisplayMode == MenuOptions.ReaderBgImageDisplayModeOptions.Loop
    var lazyColumnSize by remember { mutableStateOf(IntSize(0, 0)) }
    var backgroundViewportHeightPx by remember { mutableIntStateOf(0) }
    var backgroundPhasePx by remember { mutableFloatStateOf(0f) }
    val backgroundScrollConnection = remember(loopBackgroundEnabled) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val height = backgroundViewportHeightPx
                if (loopBackgroundEnabled && height > 0 && consumed.y != 0f) {
                    backgroundPhasePx = positiveModulo(
                        backgroundPhasePx + consumed.y,
                        height.toFloat(),
                    )
                }
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(loopBackgroundEnabled) {
        backgroundPhasePx = 0f
    }

    val reachedTopMsg = stringResource(R.string.reader_reached_top)
    val prevChapterLabel = stringResource(R.string.previous_chapter)
    val reachedBottomMsg = stringResource(R.string.reader_reached_bottom)
    val nextChapterLabel = stringResource(R.string.next_chapter)
    val confirmLabel = stringResource(R.string.confirm)
    val reachedStartMsg = stringResource(R.string.reader_reached_start)
    val reachedEndMsg = stringResource(R.string.reader_reached_end)

    LaunchedEffect(listState, uiState.readingChapterId) {
        if (!uiState.isRestoringProgress) return@LaunchedEffect
        val chapterId = uiState.readingChapterId ?: return@LaunchedEffect
        listState.scrollToItem(1)
        snapshotFlow { lazyColumnSize }.first { lazyColumnSize.height > 0 }
        val item = snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == chapterId }
        }.first { it != null } ?: return@LaunchedEffect
        val offset = if (uiState.readingProgress <= 0f) {
            0
        } else {
            // Keep this inverse of ScrollContentViewModel.calculateReadingProgress().
            // A chapter remains active until its end reaches the viewport center.
            ((item.size - lazyColumnSize.height / 2).coerceAtLeast(0) * uiState.readingProgress).toInt()
        }
        listState.scrollToItem(1, offset)
        withFrameNanos { }
        uiState.finishProgressRestore()
    }
    LaunchedEffect(listState) {
        var atTop = false
        var atBottom = false

        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling) {
                    val layoutInfo = listState.layoutInfo
                    val totalCount = layoutInfo.totalItemsCount
                    val firstIndex = listState.firstVisibleItemIndex
                    val firstOffset = listState.firstVisibleItemScrollOffset
                    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()

                    val isAtTop = firstIndex == 0 && firstOffset == 0
                    val isAtBottom = lastVisible != null &&
                            lastVisible.index == totalCount - 1 &&
                            (lastVisible.offset + lastVisible.size) <= layoutInfo.viewportEndOffset

                    when {
                        isAtTop -> {
                            if (atTop) {
                                if (uiState.readingChapterContent?.map { it.hasPrevChapter() }
                                        ?.get() == true)
                                    launch {
                                        showSnackbar(
                                            coroutineScope = this,
                                            hostState = snackbarHostState,
                                            message = reachedTopMsg,
                                            actionLabel = prevChapterLabel
                                        ) { if (it == SnackbarResult.ActionPerformed) onClickPrevChapter() }
                                    }
                                else
                                    launch {
                                        showSnackbar(
                                            coroutineScope = this,
                                            hostState = snackbarHostState,
                                            message = reachedStartMsg,
                                            actionLabel = confirmLabel
                                        )
                                    }
                            }
                            atTop = true; atBottom = false
                        }

                        isAtBottom -> {
                            if (atBottom) {
                                if (uiState.readingChapterContent?.map { it.hasNextChapter() }
                                        ?.get() == true)
                                    launch {
                                        showSnackbar(
                                            coroutineScope = this,
                                            hostState = snackbarHostState,
                                            message = reachedBottomMsg,
                                            actionLabel = nextChapterLabel
                                        ) { if (it == SnackbarResult.ActionPerformed) onClickNextChapter() }
                                    }
                                else
                                    launch {
                                        showSnackbar(
                                            coroutineScope = this,
                                            hostState = snackbarHostState,
                                            message = reachedEndMsg,
                                            actionLabel = confirmLabel
                                        )
                                    }
                            }
                            atBottom = true; atTop = false
                        }

                        else -> {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            atTop = false; atBottom = false
                        }
                    }
                }
            }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        uiState.writeProgressRightNow()
    }
    val loopBackgroundPainter = if (loopBackgroundEnabled) {
        rememberReaderBackgroundPainter(settingState)
    } else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onGloballyPositioned { backgroundViewportHeightPx = it.size.height }
    ) {
        if (loopBackgroundPainter != null && backgroundViewportHeightPx > 0) {
            val backgroundHeight = with(density) { backgroundViewportHeightPx.toDp() }
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(backgroundHeight)
                    .graphicsLayer {
                        translationY = backgroundPhasePx - backgroundViewportHeightPx
                    },
                painter = loopBackgroundPainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(backgroundHeight)
                    .graphicsLayer { translationY = backgroundPhasePx },
                painter = loopBackgroundPainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }

        AnimatedVisibility(
            uiState.contentList.getOrNull(1) == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Loading()
        }
        AnimatedVisibility(
            uiState.contentList.getOrNull(1) != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = modifier
                    .nestedScroll(backgroundScrollConnection)
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                changeIsImmersive.invoke()
                            }
                        )
                    }
                    .onGloballyPositioned {
                        scope.launch {
                            withFrameNanos { }
                            uiState.setLazyColumnSize(it.size)
                            lazyColumnSize = it.size
                        }
                    },
                state = listState,
            ) {
                itemsIndexed(
                    items = uiState.contentList,
                    key = { index, pair -> pair?.first ?: "placeholder-$index" }
                ) { index, pair ->
                    // The center chapter has its own full-screen loading state above. Empty adjacent
                    // slots must stay zero-height; rendering Loading() here creates a full viewport
                    // item that looks like a previous/next chapter which never finishes loading.
                    val result = pair?.second ?: return@itemsIndexed
                    uiState.contentList.getOrNull(index + 1)?.second?.get()?.let {
                        if (!it.hasPrevChapter()) return@itemsIndexed
                    }
                    uiState.contentList.getOrNull(index - 1)?.second?.get()?.let {
                        if (!it.hasNextChapter()) return@itemsIndexed
                    }
                    result.onOk {
                        TextContent(
                            modifier = modifier,
                            settingState = settingState,
                            content = it
                        )
                    }.onErr {
                        ChapterContentError(it)
                    }
                }
            }
        }
    }
}

private fun positiveModulo(value: Float, modulus: Float): Float =
    if (modulus <= 0f) 0f else ((value % modulus) + modulus) % modulus

@Composable
private fun TextContent(
    modifier: Modifier,
    settingState: SettingState,
    content: ChapterContentUiState
) {
    val componentRender = LocalComponentRender.current
    val density = LocalDensity.current
    val screenHeight = LocalResources.current.displayMetrics.heightPixels
    val textColor = readerTextColor(settingState)
    val fontFamily = rememberReaderFontFamily(settingState.fontUriUserData)
    Column(
        Modifier.defaultMinSize(
            minHeight = with(density) {
                screenHeight.toDp()
            }
        )
    ) {
        val titleRegex = Regex("^(第[一二三四五六七八九十]+卷)\\s+(.*)")
        val matchResult = titleRegex.find(content.title)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (matchResult != null) {
                val (volumeTitle, chapterTitle) = matchResult.destructured
                Text(
                    text = volumeTitle,
                    textAlign = TextAlign.Center,
                    fontSize = (settingState.fontSize + 2).sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = fontFamily,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    text = chapterTitle,
                    textAlign = TextAlign.Center,
                    fontSize = (settingState.fontSize + 6).sp,
                    lineHeight = (settingState.fontSize + settingState.lineHeight + 6).sp,
                    fontWeight = FontWeight((settingState.fontWeigh.toInt() + 100)),
                    fontFamily = fontFamily,
                    color = textColor
                )
            } else {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    text = content.title,
                    textAlign = TextAlign.Center,
                    fontSize = (settingState.fontSize + 6).sp,
                    lineHeight = (settingState.fontSize + settingState.lineHeight + 6).sp,
                    fontWeight = FontWeight((settingState.fontWeigh.toInt() + 100)),
                    fontFamily = fontFamily,
                    color = textColor
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.width(48.dp),
                    color = textColor
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        for (data in content.content) {
            componentRender.Component(
                modifier = Modifier.fillMaxWidth(),
                componentData = data,
            )
        }
    }
}
