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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.readerTextColor
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderBackgroundPainter
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderFontFamily
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import kotlinx.coroutines.flow.filter
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
    val screenHeight = LocalResources.current.displayMetrics.heightPixels
    val textColor = readerTextColor(settingState)
    val fontFamily = rememberReaderFontFamily(settingState.fontFamilyUriUserData)
    val listState = uiState.lazyListState
    val scope = rememberCoroutineScope()
    var lazyColumnSize by remember { mutableStateOf(IntSize(0, 0)) }

    val reachedTopMsg = stringResource(R.string.reader_reached_top)
    val prevChapterLabel = stringResource(R.string.previous_chapter)
    val reachedBottomMsg = stringResource(R.string.reader_reached_bottom)
    val nextChapterLabel = stringResource(R.string.next_chapter)
    val confirmLabel = stringResource(R.string.confirm)
    val reachedStartMsg = stringResource(R.string.reader_reached_start)
    val reachedEndMsg = stringResource(R.string.reader_reached_end)

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .filter { it.isNotEmpty() }
            .first()
        withFrameNanos {  }
        listState.scrollToItem(1)
        val item = uiState.lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == uiState.readingContentId } ?: return@LaunchedEffect
        snapshotFlow { lazyColumnSize }
            .filter { lazyColumnSize.height > 0 }
            .first()
        val offset = (item.size * uiState.readingProgress).toInt() - lazyColumnSize.height
        listState.scrollToItem(1, offset)
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
                                if (uiState.readingChapterContent.hasPrevChapter())
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
                                if (uiState.readingChapterContent.hasNextChapter())
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

    if (settingState.enableBackgroundImage && settingState.backgroundImageDisplayMode == MenuOptions.ReaderBgImageDisplayModeOptions.Loop) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) {
                    screenHeight.toDp()
                })
                .offset(y = with(density) {
                    ((uiState.lazyListState.layoutInfo.visibleItemsInfo.getOrNull(0)?.offset
                        ?: 0) % screenHeight + screenHeight).toDp()
                }),
            painter = rememberReaderBackgroundPainter(settingState),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) {
                    screenHeight.toDp()
                })
                .offset(y = with(density) {
                    ((uiState.lazyListState.layoutInfo.visibleItemsInfo.getOrNull(0)?.offset
                        ?: 0) % screenHeight).toDp()
                }),
            painter = rememberReaderBackgroundPainter(settingState),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        uiState.writeProgressRightNow()
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
        var index = remember { 0 }
        LazyColumn(
            modifier = modifier
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
            items(
                items = uiState.contentList,
                key = { it?.id ?: index++ }
            ) { chapterContent ->
                chapterContent?.let { content ->
                    Column(
                        Modifier.defaultMinSize(
                            minHeight = with(density) {
                                screenHeight.toDp()
                            }
                        )
                    ) {
                        if (settingState.isUsingContinuousScrolling) {
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
                                        lineHeight = (settingState.fontSize + settingState.fontLineHeight + 6).sp,
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
                                        lineHeight = (settingState.fontSize + settingState.fontLineHeight + 6).sp,
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
                        }
                        val components = remember { uiState.contentComponentsMap[content.id] }
                        components?.let {
                            for (component in it) {
                                component.Content(modifier)
                            }
                        }
                    }
                }
            }
        }
    }
}