package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.LayoutDirection
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterLayoutWindow
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.MeasuredComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.chapterOverscroll
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.isVertical
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class FlipPage(
    val chapterId: String,
    val index: Int,
    val count: Int,
    val components: List<MeasuredComponent>,
) {
    val key = "${chapterId.length}:$chapterId:$index"
    val progress = if (count == 1) 1f else index.toFloat() / (count - 1)
}

@Composable
internal fun FlipContent(
    window: ChapterLayoutWindow,
    mode: WriteMode,
    settingState: SettingState,
    entryProgress: Float,
    onAdjacent: (Int) -> Unit,
    onToggleImmersive: () -> Unit,
    onReadingProgress: (String, Float, Boolean) -> Unit,
) {
    val pages = remember(window) {
        window.chapters.flatMap { chapter ->
            chapter.pages.mapIndexed { index, components ->
                FlipPage(chapter.chapterId, index, chapter.pages.size, components)
            }
        }
    }
    val pagesByKey = remember(pages) { pages.associateBy(FlipPage::key) }
    val active = window.current
    val firstPage = window.previous?.pages?.size ?: 0
    val target = firstPage + (entryProgress * active.pages.lastIndex).roundToInt()
    val pager = rememberPagerState(initialPage = target) { pages.size }
    var currentChapterId by remember { mutableStateOf(active.chapterId) }
    var awaitingChapter by remember { mutableStateOf<String?>(active.chapterId) }
    var requestedChapter by remember { mutableStateOf<String?>(null) }
    var previousLayout by remember { mutableStateOf(window.previous) }
    val animated = settingState.flipAnime != MenuOptions.FlipAnimationOptions.None
    val localRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val rtl = if (mode.isVertical) mode == WriteMode.VerticalRTL else localRtl

    SideEffect {
        val previousChanged = previousLayout !== window.previous
        previousLayout = window.previous
        val changed = currentChapterId != active.chapterId
        if (changed) {
            currentChapterId = active.chapterId
            awaitingChapter = active.chapterId.takeUnless { it == requestedChapter }
            requestedChapter = null
        }
        if (awaitingChapter == active.chapterId && (changed || previousChanged)) {
            pager.requestScrollToPage(target)
        }
    }

    val latestWindow by rememberUpdatedState(window)
    val latestPages by rememberUpdatedState(pagesByKey)
    val adjacent by rememberUpdatedState(onAdjacent)
    val report by rememberUpdatedState(onReadingProgress)
    val toggleImmersive by rememberUpdatedState(onToggleImmersive)
    val currentPage = pager.layoutInfo.visiblePagesInfo.firstOrNull { it.index == pager.currentPage }
        ?.let { pagesByKey[it.key] }

    LaunchedEffect(pager) {
        snapshotFlow {
            if (pager.isScrollInProgress) return@snapshotFlow null
            val visible = pager.layoutInfo.visiblePagesInfo.firstOrNull { it.index == pager.settledPage }
                ?: return@snapshotFlow null
            val page = latestPages[visible.key] ?: return@snapshotFlow null
            val activeId = latestWindow.current.chapterId
            page.takeIf {
                it.chapterId == activeId || (awaitingChapter == null &&
                    requestedChapter == null && currentChapterId == activeId)
            }
        }.collect { page ->
            if (page == null) return@collect
            val current = latestWindow.current.chapterId
            if (page.chapterId == current) {
                awaitingChapter = null
                report(current, page.progress, true)
            } else {
                val direction = when (page.chapterId) {
                    latestWindow.previousId -> -1
                    latestWindow.nextId -> 1
                    else -> return@collect
                }
                requestedChapter = page.chapterId
                adjacent(direction)
            }
        }
    }

    val requests = remember { Channel<Int>(Channel.CONFLATED) }
    LaunchedEffect(pager, animated) {
        for (direction in requests) {
            if (awaitingChapter != null || requestedChapter != null || pager.isScrollInProgress) continue
            val destination = pager.settledPage + direction
            if (destination !in 0 until pager.pageCount) {
                adjacent(direction)
            } else if (animated) {
                pager.animateScrollToPage(destination)
            } else {
                pager.scrollToPage(destination)
            }
        }
    }

    val volumeKeys = settingState.isUsingVolumeKeyFlip
    val hasWindowFocus = LocalWindowInfo.current.isWindowFocused
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    var volumeRepeat by remember { mutableStateOf<Job?>(null) }
    val interval = (settingState.volumeKeyContinuousFlipInterval * 1000).toLong()
    LaunchedEffect(volumeKeys, hasWindowFocus, interval) {
        volumeRepeat?.cancel()
    }
    LaunchedEffect(volumeKeys, hasWindowFocus, active.chapterId) {
        if (volumeKeys && hasWindowFocus) focusRequester.requestFocus()
    }

    val tapToTurn = settingState.isUsingClickFlipPage
    val readingDirection = if (rtl) -1 else 1
    val pageInput = Modifier
        .chapterOverscroll(mode, horizontal = true, active.chapterId, onAdjacent)
        .onPreviewKeyEvent { event ->
            if (!volumeKeys || (event.key != Key.VolumeUp && event.key != Key.VolumeDown)) {
                return@onPreviewKeyEvent false
            }
            val direction = if (event.key == Key.VolumeUp) -1 else 1
            when (event.type) {
                KeyEventType.KeyDown -> {
                    if (event.nativeKeyEvent.repeatCount == 0) {
                        volumeRepeat?.cancel()
                        requests.trySend(direction)
                        if (interval > 0) {
                            volumeRepeat = scope.launch {
                                while (true) {
                                    delay(interval)
                                    requests.trySend(direction)
                                }
                            }
                        }
                    }
                    true
                }
                KeyEventType.KeyUp -> {
                    volumeRepeat?.cancel()
                    true
                }
                else -> false
            }
        }
        .focusRequester(focusRequester)
        .focusable()
        .pointerInput(tapToTurn, readingDirection, volumeKeys) {
            detectTapGestures(onPress = { if (volumeKeys) focusRequester.requestFocus() }) { point ->
                when {
                    tapToTurn && point.x < size.width / 3f -> requests.trySend(-readingDirection)
                    tapToTurn && point.x > size.width * 2f / 3f -> requests.trySend(readingDirection)
                    else -> toggleImmersive()
                }
            }
        }
        .then(
            if (animated) Modifier else Modifier.pointerInput(readingDirection) {
                var distance = 0f
                detectHorizontalDragGestures(
                    onDragStart = { distance = 0f },
                    onDragEnd = {
                        if (distance != 0f) requests.trySend(
                            if (distance < 0f) readingDirection else -readingDirection
                        )
                    },
                    onHorizontalDrag = { _, delta -> distance += delta },
                )
            }
        )

    HorizontalPager(
        state = pager,
        modifier = pageInput
            .fillMaxSize()
            .testTag("flip-chapter-${active.chapterId}")
            .semantics {
                testTagsAsResourceId = true
                contentDescription = "flip-state-page=${currentPage?.index ?: 0};" +
                    "count=${active.pages.size};end=true;" +
                    "animating=${pager.isScrollInProgress};direction=0"
            },
        reverseLayout = rtl != localRtl,
        userScrollEnabled = animated,
        key = { pages[it].key },
    ) { index ->
        val page = pages[index]
        val modifier = Modifier.fillMaxSize().then(
            if (currentPage?.key == page.key) {
                Modifier.testTag("flip-page-${page.index}-${page.chapterId.hashCode()}")
            } else Modifier
        )
        if (page.components.isEmpty()) {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.nothing_here),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            return@HorizontalPager
        }
        SelectionContainer {
            if (mode.isVertical) {
                Row(
                    modifier = modifier,
                    horizontalArrangement = if (rtl != localRtl) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val components = if (rtl != localRtl) page.components.asReversed() else page.components
                    components.forEach { ChapterComponent(it, mode) }
                }
            } else {
                Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                    page.components.forEach { ChapterComponent(it, mode) }
                }
            }
        }
    }
}
