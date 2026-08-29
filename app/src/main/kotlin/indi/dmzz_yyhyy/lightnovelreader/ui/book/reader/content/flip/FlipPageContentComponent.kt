package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentError
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentLoading
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.data.Divisible
import io.nightfish.lightnovelreader.api.ui.LocalComponentRender
import io.nightfish.lightnovelreader.api.ui.LocalReaderStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
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
    val focusRequester = remember { FocusRequester() }
    val windowInfo = LocalWindowInfo.current
    val screenWidthPx = windowInfo.containerSize.width.toFloat()
    val activeChapterContent by rememberUpdatedState(chapterContent)
    var volumeJob by remember { mutableStateOf<Job?>(null) }
    // A no-animation turn completes synchronously, so every physical tap must be retained.
    // CONFLATED collapsed a burst of taps into one request and made seamless chapter turning
    // appear to stop. BUFFERED remains bounded while preserving realistic rapid input.
    val pageRequests = remember(uiState.pagerState) { Channel<Int>(capacity = 256) }
    val intervalMs = (settingState.volumeKeyContinuousFlipInterval * 1000).toLong()
    fun enqueuePageRequest(direction: Int) {
        if (
            settingState.flipAnime != MenuOptions.FlipAnimationOptions.None &&
            (uiState.pagerState.isAnimating || uiState.pagerState.pendingChapterDirection != 0)
        ) {
            // Animated turns retain only the newest request so releasing a rapid gesture does
            // not leave seconds of autonomous page turns. No-animation mode intentionally keeps
            // every click because each one is an explicit page advance.
            while (pageRequests.tryReceive().isSuccess) {
                // Drain stale animated requests; the newest direction is enqueued below.
            }
        }
        pageRequests.trySend(direction)
    }
    suspend fun settlePage(
        direction: Int,
        startOffset: Float = uiState.pagerState.pageOffset,
    ): Boolean {
        val pagerState = uiState.pagerState
        if (direction == 0 || pagerState.isAnimating || pagerState.pendingChapterDirection != 0) {
            return false
        }
        var waitingForChapter = false
        var progressed = false
        try {
            pagerState.isAnimating = true
            val animated = settingState.flipAnime != MenuOptions.FlipAnimationOptions.None
            if (animated) {
                val pageWidth = pagerState.viewportWidth
                    .takeIf { it > 0 }
                    ?.toFloat()
                    ?: screenWidthPx
                animate(
                    initialValue = startOffset,
                    targetValue = -direction * pageWidth,
                    animationSpec = tween(220),
                ) { value, _ -> pagerState.pageOffset = value }
            }

            val changed = Snapshot.withMutableSnapshot {
                if (direction > 0) nextPage(pagerState) else lastPage(pagerState)
            }
            progressed = changed
            if (!changed) {
                val adjacentChapterId = if (direction > 0) {
                    activeChapterContent.nextChapter
                } else {
                    activeChapterContent.prevChapter
                }?.takeIf { it.isNotBlank() }
                if (adjacentChapterId != null) {
                    // Keep the completed adjacent frame while the already-prefetched chapter is
                    // installed. A watchdog below releases the lock if loading does not complete;
                    // an I/O failure must never make all future reader input a permanent no-op.
                    pagerState.pendingChapterDirection = direction
                    if (direction > 0) onClickNextChapter() else onClickPrevChapter()
                    waitingForChapter = true
                    progressed = true
                    val sourceChapterId = activeChapterContent.id
                    scope.launch {
                        delay(CHAPTER_TRANSITION_INPUT_TIMEOUT_MS)
                        if (
                            uiState.readingChapterId == sourceChapterId &&
                            pagerState.pendingChapterDirection == direction
                        ) {
                            Snapshot.withMutableSnapshot {
                                pagerState.pageOffset = 0f
                                pagerState.pendingChapterDirection = 0
                                pagerState.isAnimating = false
                            }
                            volumeJob?.cancel()
                            volumeJob = null
                        }
                    }
                }
            }
        } finally {
            if (!waitingForChapter) {
                Snapshot.withMutableSnapshot {
                    pagerState.pageOffset = 0f
                    pagerState.pendingChapterDirection = 0
                    pagerState.isAnimating = false
                }
            }
        }
        return progressed
    }
    suspend fun cancelPageDrag() {
        val pagerState = uiState.pagerState
        if (pagerState.isAnimating || pagerState.pendingChapterDirection != 0) return
        try {
            pagerState.isAnimating = true
            animate(
                initialValue = pagerState.pageOffset,
                targetValue = 0f,
                animationSpec = tween(180),
            ) { value, _ -> pagerState.pageOffset = value }
        } finally {
            pagerState.pageOffset = 0f
            pagerState.isAnimating = false
        }
    }
    fun requestNextPage() {
        enqueuePageRequest(1)
    }
    fun requestPreviousPage() {
        enqueuePageRequest(-1)
    }

    // Keep one consumer alive across seamless chapter replacement. Cancelling this effect on
    // chapter id used to remove an already-received click while it was waiting for the new
    // chapter to unlock, so rapid no-animation taps appeared to freeze at the boundary.
    LaunchedEffect(pageRequests) {
        pageRequests.receiveAsFlow().collect { direction ->
            snapshotFlow {
                !uiState.pagerState.isAnimating &&
                    uiState.pagerState.pendingChapterDirection == 0
            }.first { ready -> ready }
            val progressed = settlePage(direction, 0f)
            if (!progressed) {
                // KeyUp can be delivered to a replacement focus target during a chapter
                // recomposition. Stop an orphaned long-press producer at the real book edge so
                // it cannot continuously overwrite later input in the conflated request queue.
                volumeJob?.cancel()
                volumeJob = null
            }
        }
    }

    LaunchedEffect(
        focusRequester,
        chapterContent.id,
        settingState.isUsingVolumeKeyFlip,
        windowInfo.isWindowFocused,
    ) {
        if (settingState.isUsingVolumeKeyFlip && windowInfo.isWindowFocused) {
            // The chapter Flow and AnimatedContent can replace focus targets in the same frame.
            // Request after the replacement has been attached, otherwise a long press that
            // crosses chapters leaves volume navigation permanently unfocused.
            withFrameNanos { }
            focusRequester.requestFocus()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FlipPageFragment(
            modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (!settingState.isUsingVolumeKeyFlip) {
                    false
                } else if (event.key == Key.VolumeUp || event.key == Key.VolumeDown) {
                    when (event.type) {
                        KeyEventType.KeyDown -> {
                            focusRequester.requestFocus()
                            if (event.nativeKeyEvent.repeatCount == 0) {
                                if (event.key == Key.VolumeUp) requestPreviousPage()
                                else requestNextPage()

                                if (intervalMs > 0) {
                                    volumeJob?.cancel()
                                    volumeJob = scope.launch {
                                        while (isActive) {
                                            delay(intervalMs.milliseconds)
                                            if (event.key == Key.VolumeUp) requestPreviousPage()
                                            else requestNextPage()
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
            .pointerInput(
                settingState.isUsingFlipPage,
                settingState.isUsingClickFlipPage,
                settingState.flipAnime,
                chapterContent.id,
            ) {
                // currentPage/pageCount are intentionally not pointer-input keys. Full-chapter
                // pagination updates pageCount repeatedly in the background; restarting this
                // coroutine for every measured page cancels an in-flight gesture, which made
                // the pager appear unresponsive immediately after restoring progress.
                awaitEachGesture {
                    // Observe the gesture before SelectionContainer. Horizontal page turns win
                    // after touch slop; an unmoved long press still reaches text selection.
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial,
                    )
                    var lastPosition = down.position
                    var movement = Offset.Zero
                    var pressed = true
                    while (pressed) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        movement += change.position - lastPosition
                        lastPosition = change.position
                        pressed = change.pressed
                        if (
                            settingState.flipAnime != MenuOptions.FlipAnimationOptions.None &&
                            !uiState.pagerState.isAnimating &&
                            uiState.pagerState.pendingChapterDirection == 0 &&
                            abs(movement.x) > abs(movement.y)
                        ) {
                            val pageWidth = uiState.pagerState.viewportWidth
                                .takeIf { it > 0 }
                                ?.toFloat()
                                ?: size.width.toFloat()
                            uiState.pagerState.pageOffset = movement.x.coerceIn(
                                -pageWidth,
                                pageWidth,
                            )
                        }
                        if (movement.getDistance() > viewConfiguration.touchSlop) change.consume()
                    }

                    val horizontal = abs(movement.x) > abs(movement.y)
                    if (!horizontal) uiState.pagerState.pageOffset = 0f
                    if (
                        horizontal &&
                        movement.getDistance() > viewConfiguration.touchSlop &&
                        settingState.isUsingVolumeKeyFlip
                    ) {
                        focusRequester.requestFocus()
                    }
                    when {
                        settingState.isUsingFlipPage && horizontal &&
                                movement.x < -size.width / 4f -> enqueuePageRequest(1)
                        settingState.isUsingFlipPage && horizontal &&
                                movement.x > size.width / 4f -> enqueuePageRequest(-1)
                        settingState.isUsingFlipPage && horizontal -> scope.launch { cancelPageDrag() }
                        !horizontal && abs(movement.y) > viewConfiguration.touchSlop ->
                            changeIsImmersive()
                        movement.getDistance() <= viewConfiguration.touchSlop -> {
                            if (settingState.isUsingFlipPage && settingState.isUsingClickFlipPage) {
                                when {
                                    down.position.x < size.width / 3f -> requestPreviousPage()
                                    down.position.x > size.width * 2f / 3f -> requestNextPage()
                                    else -> changeIsImmersive()
                                }
                            } else changeIsImmersive()
                        }
                    }
                }
        },
        components = chapterContent.content,
        chapterId = chapterContent.id,
        prevChapterId = chapterContent.prevChapter,
        nextChapterId = chapterContent.nextChapter,
        pagerState = uiState.pagerState,
        contentPadding = paddingValues,
        flipAnimation = settingState.flipAnime,
        onComponentLocated = uiState.locateComponent,
        onBeyondStart = onClickPrevChapter,
        onBeyondEnd = onClickNextChapter,
        measurementWindow = uiState.pagerState.measurementWindow,
        )
        uiState.prevChapterContent?.onOk { adjacent ->
            PremeasureFlipChapter(adjacent, paddingValues, uiState.pagerState.measurementWindow)
        }
        uiState.nextChapterContent?.onOk { adjacent ->
            PremeasureFlipChapter(adjacent, paddingValues, uiState.pagerState.measurementWindow)
        }
    }
}

@Composable
private fun PremeasureFlipChapter(
    chapter: ChapterContentUiState,
    contentPadding: PaddingValues,
    measurementWindow: ChapterMeasurementWindow,
) {
    val premeasureState = remember(chapter.id) { FlipPagerState() }
    FlipPageFragment(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 0f }
            .clearAndSetSemantics { },
        components = chapter.content,
        chapterId = chapter.id,
        prevChapterId = null,
        nextChapterId = null,
        pagerState = premeasureState,
        contentPadding = contentPadding,
        flipAnimation = MenuOptions.FlipAnimationOptions.None,
        onComponentLocated = { _, _ -> },
        onBeyondStart = {},
        onBeyondEnd = {},
        measurementWindow = measurementWindow,
        exposeCurrentPageForTesting = false,
    )
}

private const val CHAPTER_TRANSITION_INPUT_TIMEOUT_MS = 1_500L

fun nextPage(pagerState: FlipPagerState): Boolean {
    if (pagerState.currentPage + 1 >= pagerState.pageCount && pagerState.endReached) return false
    pagerState.moveTo(pagerState.currentPage + 1)
    return true
}

fun lastPage(pagerState: FlipPagerState): Boolean {
    if (pagerState.currentPage == 0) return false
    pagerState.moveTo(pagerState.currentPage - 1)
    return true
}

@Composable
private fun FlipPageFragment(
    modifier: Modifier = Modifier,
    components: List<AbstractContentComponentData>,
    chapterId: String,
    prevChapterId: String?,
    nextChapterId: String?,
    pagerState: FlipPagerState,
    contentPadding: PaddingValues,
    flipAnimation: String,
    onComponentLocated: (Int, Int) -> Unit,
    onBeyondStart: () -> Unit,
    onBeyondEnd: () -> Unit,
    measurementWindow: ChapterMeasurementWindow,
    exposeCurrentPageForTesting: Boolean = true,
) {
    val context = LocalContext.current
    val readerStyle = LocalReaderStyle.current
    val baseStyle = MaterialTheme.typography.bodyMedium
    val componentRender = LocalComponentRender.current
    val contentSignature = remember(components) {
        components.fold(1) { hash, component -> 31 * hash + component.hashCode() }
    }
    val styleSignature = 31 * readerStyle.hashCode() + baseStyle.hashCode()
    val cachedPagination = remember(chapterId, contentSignature, styleSignature) {
        measurementWindow.find(chapterId, contentSignature, styleSignature)
    }
    val measurements = remember(components, readerStyle, baseStyle) {
        // The unresolved tail is measured only once per composition. A conflated channel let a
        // redundant visible-page measurement overwrite that result during rapid turns, leaving
        // pagination permanently incomplete and the chapter transition waiting forever.
        Channel<PageMeasurement>(Channel.BUFFERED)
    }
    val initialComponents = remember(components) {
        components.map { AnchoredComponent(it.hashCode(), it.hashCode(), it) }
    }
    var pages by remember(components, readerStyle, baseStyle) {
        mutableStateOf(
            cachedPagination?.pages ?: if (initialComponents.isEmpty()) emptyList()
            else listOf(FlipPage(seed = initialComponents))
        )
    }
    var paginationSize by remember(components, readerStyle, baseStyle) {
        mutableStateOf(cachedPagination?.let { it.width to it.height })
    }
    LaunchedEffect(measurements, components, readerStyle, baseStyle) {
        measurements.receiveAsFlow().collect { measurement ->
            val page = pages.getOrNull(measurement.pageIndex) ?: return@collect
            val newSize = measurement.width to measurement.height
            if (paginationSize != null && paginationSize != newSize) {
                val currentLocation = pages.getOrNull(pagerState.currentPage)
                    ?.displayed
                    ?.let { displayed ->
                        displayed.firstOrNull { component ->
                            val divisible = component.data as? Divisible<*>
                            divisible == null || !divisible.split
                        } ?: displayed.firstOrNull()
                    }
                val restoreTargetHash = pagerState.restoreTargetHash
                    ?: currentLocation?.componentHash
                val restoreTargetFragmentHash = pagerState.restoreTargetFragmentHash
                    ?: currentLocation?.fragmentHash
                val restoreInProgress = pagerState.restoreInProgress
                val restoreToEnd = pagerState.restoreToEnd
                paginationSize = newSize
                pages = if (initialComponents.isEmpty()) emptyList()
                else listOf(FlipPage(seed = initialComponents))
                pagerState.reset()
                pagerState.restoreTargetHash = restoreTargetHash
                pagerState.restoreTargetFragmentHash = restoreTargetFragmentHash
                pagerState.restoreInProgress = restoreInProgress || restoreTargetHash != null
                pagerState.restoreToEnd = restoreToEnd
                return@collect
            }
            paginationSize = newSize
            if (page.resolved || page.seedKey != measurement.seedKey) return@collect
            val overflowIndex = measurement.overflowIndex
            val prefix = page.seed.take(overflowIndex ?: page.seed.size)
            val resolution = if (overflowIndex == null) {
                PageResolution(prefix, emptyList())
            } else {
                val overflow = page.seed[overflowIndex]
                val splitComponents = if (
                    measurement.availableHeight > 0 && overflow.data is Divisible<*>
                ) {
                    withContext(Dispatchers.Default) {
                        (overflow.data as Divisible<*>).split(
                            height = measurement.availableHeight,
                            width = measurement.width,
                            context = context,
                            readerStyle = readerStyle,
                            baseStyle = baseStyle,
                        )
                    }
                } else null

                if (!splitComponents.isNullOrEmpty() && splitComponents.size > 1) {
                    val fragments = splitComponents.map {
                        AnchoredComponent(overflow.componentHash, it.hashCode(), it)
                    }
                    PageResolution(
                        prefix + fragments.first(),
                        fragments.drop(1) + page.seed.drop(overflowIndex + 1),
                    )
                } else if (
                    splitComponents?.size == 1 &&
                    measurement.componentHeight != null &&
                    measurement.componentHeight <= measurement.availableHeight
                ) {
                    PageResolution(
                        prefix + overflow,
                        page.seed.drop(overflowIndex + 1),
                    )
                } else if (prefix.isNotEmpty()) {
                    PageResolution(prefix, page.seed.drop(overflowIndex))
                } else {
                    PageResolution(listOf(overflow), page.seed.drop(overflowIndex + 1))
                }
            }

            val newPages = buildList {
                addAll(pages.take(measurement.pageIndex))
                add(
                    page.copy(
                        displayed = resolution.displayed,
                        resolved = true,
                        measuredWidth = measurement.width,
                        measuredHeight = measurement.height,
                    )
                )
                if (resolution.nextSeed.isNotEmpty()) add(FlipPage(resolution.nextSeed))
            }
            pages = newPages
            pagerState.updatePageCount(newPages.size)
            pagerState.endReached = resolution.nextSeed.isEmpty()
            measurementWindow.put(
                PaginationCacheKey(
                    chapterId = chapterId,
                    contentSignature = contentSignature,
                    styleSignature = styleSignature,
                    width = measurement.width,
                    height = measurement.height,
                ),
                CachedPagination(newPages, pagerState.endReached)
            )
        }
    }

    LaunchedEffect(chapterId, cachedPagination) {
        pagerState.updatePageCount(pages.size)
        pagerState.endReached = cachedPagination?.endReached ?: initialComponents.isEmpty()
    }

    // Every non-final resolution appends an unresolved tail page. Therefore no unresolved
    // page means that this local pagination snapshot is complete, independently of the shared
    // pager flag (which a chapter-state reset may have cleared).
    val paginationComplete = pages.none { !it.resolved }

    LaunchedEffect(
        chapterId,
        pages.size,
        paginationComplete,
        pagerState.restoreInProgress,
    ) {
        pagerState.updatePageCount(pages.size)
        // pageCount and endReached describe the same pagination snapshot. A ViewModel reset
        // can happen after cached pages were composed; republish both when restoration settles
        // so the last cached page can cross the chapter boundary instead of requesting a
        // non-existent page forever.
        pagerState.endReached = paginationComplete
    }

    val componentPageIndex = remember(pages) {
        buildMap {
            pages.forEachIndexed { pageIndex, page ->
                if (!page.resolved) return@forEachIndexed
                page.displayed.forEach { component ->
                    putIfAbsent(component.componentHash, pageIndex)
                }
            }
        }
    }
    val fragmentPageIndex = remember(pages) {
        buildMap {
            pages.forEachIndexed { pageIndex, page ->
                if (!page.resolved) return@forEachIndexed
                page.displayed.forEach { component ->
                    put(component.fragmentHash, pageIndex)
                }
            }
        }
    }

    LaunchedEffect(
        pagerState.restoreTargetHash,
        pagerState.restoreTargetFragmentHash,
        componentPageIndex,
        fragmentPageIndex,
        paginationComplete,
    ) {
        val targetHash = pagerState.restoreTargetHash ?: return@LaunchedEffect
        val targetPage = pagerState.restoreTargetFragmentHash
            ?.let(fragmentPageIndex::get)
            ?: componentPageIndex[targetHash]
        // Page indices are append-only while a chapter is measured, so a target
        // can be restored as soon as its page is resolved. Waiting for the whole
        // chapter made low-memory devices show a blank reader for many seconds.
        if (targetPage == null && !paginationComplete) return@LaunchedEffect
        Snapshot.withMutableSnapshot {
            // The ViewModel may reset the shared pager after this composable has already
            // restored a cached pagination list. In that case pages.size does not change,
            // so LaunchedEffect(pages.size) cannot republish pageCount and moveTo() would
            // retain the request without committing currentPage.
            pagerState.updatePageCount(pages.size)
            pagerState.endReached = paginationComplete
            targetPage?.let(pagerState::moveTo)
            pagerState.restoreTargetHash = null
            pagerState.restoreTargetFragmentHash = null
            pagerState.restoreInProgress = false
            pagerState.pageOffset = 0f
            pagerState.pendingChapterDirection = 0
            pagerState.isAnimating = false
        }
    }

    LaunchedEffect(pagerState.restoreToEnd, pages, paginationComplete) {
        if (!pagerState.restoreToEnd) return@LaunchedEffect
        if (!paginationComplete) return@LaunchedEffect
        Snapshot.withMutableSnapshot {
            pagerState.updatePageCount(pages.size)
            pagerState.endReached = paginationComplete
            pagerState.moveTo(pages.lastIndex.coerceAtLeast(0))
            pagerState.restoreToEnd = false
            pagerState.pageOffset = 0f
            pagerState.pendingChapterDirection = 0
            pagerState.isAnimating = false
        }
    }

    LaunchedEffect(
        pagerState.currentPage,
        pages,
        pagerState.restoreTargetHash,
        pagerState.restoreTargetFragmentHash,
        pagerState.restoreToEnd,
    ) {
        if (
            pagerState.restoreInProgress ||
            pagerState.restoreTargetHash != null ||
            pagerState.restoreTargetFragmentHash != null ||
            pagerState.restoreToEnd
        ) return@LaunchedEffect
        pages.getOrNull(pagerState.currentPage)
            ?.displayed
            ?.let { displayed ->
                displayed.firstOrNull { component ->
                    val divisible = component.data as? Divisible<*>
                    divisible == null || !divisible.split
                } ?: displayed.firstOrNull()
            }
            ?.let { onComponentLocated(it.componentHash, it.fragmentHash) }
    }

    val restorePending = pagerState.restoreInProgress ||
            pagerState.restoreTargetHash != null ||
            pagerState.restoreTargetFragmentHash != null || pagerState.restoreToEnd
    // Never draw a pending target page before currentPage has been committed. In that state the
    // reader looked restored, but gestures still operated on the old page and were then
    // overwritten by the restore effect. The snapshot above commits the page and clears the
    // restore marker atomically, so the first visible frame is already interactive.
    val currentPageIndex = pagerState.currentPage
    val currentPageHash = pages.getOrNull(currentPageIndex)
        ?.displayed
        ?.firstOrNull()
        ?.componentHash
    val currentPageTestTag = if (restorePending) {
        "flip-page-pending"
    } else {
        "flip-page-$currentPageIndex-${currentPageHash ?: "pending"}"
    }
    val unresolvedPageIndex = pages.indexOfFirst { !it.resolved }
    val visiblePageAlpha = if (restorePending) 0f else 1f
    val restoreSemantics = if (restorePending) {
        Modifier.clearAndSetSemantics { }
    } else {
        Modifier
    }
    val pageTestTag = if (exposeCurrentPageForTesting) {
        Modifier.testTag(currentPageTestTag)
    } else {
        Modifier
    }
    val testTagResourceIds = if (exposeCurrentPageForTesting) {
        Modifier.semantics { testTagsAsResourceId = true }
    } else {
        Modifier
    }
    val chapterTestTag = if (exposeCurrentPageForTesting) {
        Modifier
            .testTag("flip-chapter-$chapterId")
            .semantics {
                contentDescription = "flip-state-page=${pagerState.currentPage};" +
                    "count=${pagerState.pageCount};end=${pagerState.endReached};" +
                    "animating=${pagerState.isAnimating};" +
                    "direction=${pagerState.pendingChapterDirection}"
            }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .then(testTagResourceIds)
            .then(chapterTestTag)
    ) {
        if (flipAnimation == MenuOptions.FlipAnimationOptions.None) {
            PageLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .then(pageTestTag)
                    .graphicsLayer { alpha = visiblePageAlpha }
                    .then(restoreSemantics)
                    .padding(contentPadding),
                pageIndex = currentPageIndex,
                page = pages.getOrNull(currentPageIndex),
                componentRender = componentRender,
                selectable = exposeCurrentPageForTesting,
                onMeasured = { measurements.trySend(it) },
            )
        } else {
            var pageWidth by remember { mutableStateOf(0) }
            val offset = pagerState.pageOffset
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        pageWidth = it.width
                        pagerState.viewportWidth = it.width
                    }
            ) {
                PageLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(pageTestTag)
                        .graphicsLayer {
                            alpha = visiblePageAlpha
                            translationX = offset
                        }
                        .then(restoreSemantics)
                        .padding(contentPadding),
                    pageIndex = currentPageIndex,
                    page = pages.getOrNull(currentPageIndex),
                    componentRender = componentRender,
                    selectable = exposeCurrentPageForTesting,
                    onMeasured = { measurements.trySend(it) },
                )
                val adjacentPageIndex = when {
                    offset < 0f -> currentPageIndex + 1
                    offset > 0f -> currentPageIndex - 1
                    else -> -1
                }
                val adjacentPage = pages.getOrNull(adjacentPageIndex) ?: when {
                    offset < 0f -> nextChapterId?.let {
                        measurementWindow.find(it, styleSignature)?.pages?.firstOrNull()
                    }
                    offset > 0f -> prevChapterId?.let {
                        measurementWindow.find(it, styleSignature)?.pages?.lastOrNull()
                    }
                    else -> null
                }
                if (adjacentPage != null) {
                    val baseOffset = if (offset < 0f) pageWidth.toFloat() else -pageWidth.toFloat()
                    PageLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = visiblePageAlpha
                                translationX = baseOffset + offset
                            }
                            .then(restoreSemantics)
                            .padding(contentPadding),
                        pageIndex = adjacentPageIndex.coerceAtLeast(0),
                        page = adjacentPage,
                        componentRender = componentRender,
                        selectable = false,
                        onMeasured = { measurements.trySend(it) },
                    )
                }
            }
        }

        // Always paginate the unresolved tail, independent of the page the reader is viewing.
        // Each result creates the next tail page, so composition keeps advancing until the
        // complete chapter has a stable component-hash-to-page mapping.
        if (unresolvedPageIndex >= 0 && unresolvedPageIndex != currentPageIndex) {
            PageLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0f }
                    .clearAndSetSemantics { }
                    .padding(contentPadding),
                pageIndex = unresolvedPageIndex,
                page = pages[unresolvedPageIndex],
                componentRender = componentRender,
                selectable = false,
                onMeasured = { measurements.trySend(it) },
            )
        }
    }
}

internal data class PaginationCacheKey(
    val chapterId: String,
    val contentSignature: Int,
    val styleSignature: Int,
    val width: Int,
    val height: Int,
)

internal data class CachedPagination(
    val pages: List<FlipPage>,
    val endReached: Boolean,
) {
    val width: Int get() = pages.firstOrNull()?.measuredWidth ?: 0
    val height: Int get() = pages.firstOrNull()?.measuredHeight ?: 0
}

internal class ChapterMeasurementWindow {
    private val entries = LinkedHashMap<PaginationCacheKey, CachedPagination>(8, 0.75f, true)

    fun find(chapterId: String, contentSignature: Int, styleSignature: Int): CachedPagination? {
        val key = entries.keys.lastOrNull {
            it.chapterId == chapterId &&
                    it.contentSignature == contentSignature &&
                    it.styleSignature == styleSignature
        } ?: return null
        return entries[key]
    }

    fun find(chapterId: String, styleSignature: Int): CachedPagination? {
        val key = entries.keys.lastOrNull {
            it.chapterId == chapterId && it.styleSignature == styleSignature
        } ?: return null
        return entries[key]
    }

    fun put(key: PaginationCacheKey, value: CachedPagination) {
        entries[key] = value.copy(
            pages = value.pages.map { it.copy(measuredWidth = key.width, measuredHeight = key.height) }
        )
        while (entries.size > 3) entries.remove(entries.keys.first())
    }
}

internal data class AnchoredComponent(
    val componentHash: Int,
    val fragmentHash: Int,
    val data: AbstractContentComponentData,
)

internal data class FlipPage(
    val seed: List<AnchoredComponent>,
    val displayed: List<AnchoredComponent> = emptyList(),
    val resolved: Boolean = false,
    val measuredWidth: Int = 0,
    val measuredHeight: Int = 0,
) {
    val seedKey: Int = seed.fold(1) { hash, component ->
        31 * (31 * hash + component.componentHash) + component.fragmentHash
    }
}

private data class PageMeasurement(
    val pageIndex: Int,
    val seedKey: Int,
    val width: Int,
    val height: Int,
    val overflowIndex: Int?,
    val availableHeight: Int,
    val componentHeight: Int?,
)

private data class PageResolution(
    val displayed: List<AnchoredComponent>,
    val nextSeed: List<AnchoredComponent>,
)

@Composable
private fun PageLayout(
    modifier: Modifier,
    pageIndex: Int,
    page: FlipPage?,
    componentRender: io.nightfish.lightnovelreader.api.content.component.ComponentRender,
    selectable: Boolean,
    onMeasured: (PageMeasurement) -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        if (page == null) {
            return@SubcomposeLayout layout(constraints.maxWidth, constraints.maxHeight) {}
        }
        val source = if (page.resolved) page.displayed else page.seed
        val placeables = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var occupiedHeight = 0
        var overflowIndex: Int? = null
        var overflowHeight: Int? = null

        for ((index, component) in source.withIndex()) {
            val placeable = subcompose("$pageIndex-${page.seedKey}-$index") {
                if (selectable) SelectionContainer {
                    componentRender.Component(Modifier.fillMaxWidth(), component.data)
                } else componentRender.Component(Modifier.fillMaxWidth(), component.data)
            }.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
            val remainingHeight = constraints.maxHeight - occupiedHeight
            if (
                !page.resolved &&
                (placeable.height > remainingHeight ||
                        placeable.height == remainingHeight && component.data is Divisible<*>)
            ) {
                overflowIndex = index
                overflowHeight = placeable.height
                break
            }
            placeables += placeable
            occupiedHeight += placeable.height.coerceAtMost(remainingHeight)
            if (!page.resolved && occupiedHeight >= constraints.maxHeight && index < source.lastIndex) {
                overflowIndex = index + 1
                break
            }
        }

        if (
            !page.resolved ||
            page.measuredWidth != constraints.maxWidth ||
            page.measuredHeight != constraints.maxHeight
        ) {
            onMeasured(
                PageMeasurement(
                    pageIndex = pageIndex,
                    seedKey = page.seedKey,
                    width = constraints.maxWidth,
                    height = constraints.maxHeight,
                    overflowIndex = overflowIndex,
                    availableHeight = constraints.maxHeight - placeables.sumOf { it.height },
                    componentHeight = overflowHeight,
                )
            )
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(0, y)
                y += placeable.height
            }
        }
    }
}
