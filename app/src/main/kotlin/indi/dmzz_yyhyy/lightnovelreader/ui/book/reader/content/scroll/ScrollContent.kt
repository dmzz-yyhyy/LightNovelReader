package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterLayout
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterLayoutWindow
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.MeasuredComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.chapterOverscroll
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.isVertical
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import kotlin.math.roundToLong

private data class ScrollItem(
    val chapter: ChapterLayout,
    val index: Int,
    val component: MeasuredComponent?,
    val blankLength: Int = 0,
) {
    val key = "${chapter.chapterId.length}:${chapter.chapterId}:$index"
}

private class ScrollItems(val chapters: List<ChapterLayout>, private val viewportLength: Int) {
    val items = buildList {
        chapters.forEach { chapter ->
            chapter.items.forEachIndexed { index, component ->
                add(ScrollItem(chapter, index, component))
            }
            if (chapter.items.isEmpty()) add(ScrollItem(chapter, 0, null, viewportLength))
        }
        val last = chapters.last()
        val blankLength = (viewportLength - last.length).coerceAtLeast(0).toInt()
        if (last.items.isNotEmpty() && blankLength > 0) {
            add(ScrollItem(last, last.items.size, null, blankLength))
        }
    }
    private val indexByKey = items.withIndex().associate { it.value.key to it.index }

    fun indexOf(key: Any): Int? = indexByKey[key as? String]

    fun item(key: Any): ScrollItem? = indexOf(key)?.let(items::get)

    fun keepPrefixOf(displayed: ScrollItems): ScrollItems {
        val commonIndex = displayed.chapters.indexOfFirst { it in chapters }
        if (commonIndex < 0) return this
        val shared = displayed.chapters[commonIndex]
        val merged = displayed.chapters.take(commonIndex) + chapters.drop(chapters.indexOf(shared))
        return when (merged) {
            chapters -> this
            displayed.chapters -> displayed
            else -> ScrollItems(merged, viewportLength)
        }
    }

    fun position(chapter: ChapterLayout, progress: Float): Pair<Int, Int> {
        val offset = (progress.toDouble() * progressLength(chapter)).roundToLong()
        val index = chapter.offsets.binarySearch(offset).let { if (it >= 0) it else -it - 2 }
            .coerceIn(0, chapter.items.lastIndex.coerceAtLeast(0))
        val first = items.indexOfFirst { it.chapter === chapter }
        return first + index to (offset - chapter.offsets[index]).toInt()
    }

    fun progress(item: ScrollItem, clipped: Int): Float {
        val length = progressLength(item.chapter)
        return if (length == 0L) 1f else {
            ((item.chapter.offsets[item.index] + clipped).toDouble() / length).toFloat()
                .coerceIn(0f, 1f)
        }
    }

    private fun progressLength(chapter: ChapterLayout): Long =
        (chapter.length - viewportLength).coerceAtLeast(0)
}

@Composable
internal fun ScrollContent(
    window: ChapterLayoutWindow,
    mode: WriteMode,
    viewportLength: Int,
    entryProgress: Float,
    onAdjacent: (Int) -> Unit,
    onToggleImmersive: () -> Unit,
    onReadingProgress: (String, Float, Boolean) -> Unit,
) {
    val chapters = window.chapters
    val scrollItems = remember(chapters, viewportLength) { ScrollItems(chapters, viewportLength) }
    val (initialIndex, initialOffset) = remember {
        scrollItems.position(window.current, entryProgress)
    }
    val listState = rememberLazyListState(initialIndex, initialOffset)
    var displayedItems by remember { mutableStateOf(scrollItems) }
    var currentChapterId by remember { mutableStateOf(window.current.chapterId) }
    var awaitingChapter by remember { mutableStateOf<String?>(window.current.chapterId) }
    var requestedChapter by remember { mutableStateOf<String?>(null) }
    val keepPrefix = listState.isScrollInProgress && listState.lastScrolledForward

    SideEffect {
        val active = window.current.chapterId
        val changed = currentChapterId != active
        if (changed) {
            currentChapterId = active
            awaitingChapter = active.takeUnless { it == requestedChapter }
            requestedChapter = null
        }

        val oldItems = displayedItems
        // Keep existing indices while a forward gesture or fling carries into the next chapter.
        val newItems = if (keepPrefix && awaitingChapter == null) {
            scrollItems.keepPrefixOf(oldItems)
        } else scrollItems
        if (newItems === oldItems && !changed) return@SideEffect

        val position = if (awaitingChapter == active) {
            newItems.position(window.current, entryProgress)
        } else {
            val anchor = listState.layoutInfo.firstContentItem()
            val newIndex = anchor?.let { newItems.indexOf(it.key) }
            if (anchor != null && newIndex != null && newIndex != anchor.index) {
                newIndex to (listState.layoutInfo.viewportStartOffset - anchor.offset).coerceAtLeast(0)
            } else null
        }
        Snapshot.withMutableSnapshot {
            displayedItems = newItems
            position?.let { (index, offset) -> listState.requestScrollToItem(index, offset) }
        }
    }

    val latestWindow by rememberUpdatedState(window)
    val adjacent by rememberUpdatedState(onAdjacent)
    val report by rememberUpdatedState(onReadingProgress)
    val toggleImmersive by rememberUpdatedState(onToggleImmersive)

    fun promoteVisibleChapter(chapterId: String) {
        if (awaitingChapter != null || requestedChapter != null) return
        val direction = when (chapterId) {
            latestWindow.previousId -> -1
            latestWindow.nextId -> 1
            else -> return
        }
        requestedChapter = chapterId
        adjacent(direction)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val first = info.firstContentItem() ?: return@snapshotFlow null
            val items = displayedItems
            val item = items.item(first.key) ?: return@snapshotFlow null
            if (items.indexOf(first.key) != first.index) return@snapshotFlow null
            val active = latestWindow.current.chapterId
            if (active == item.chapter.chapterId) {
                val clipped = (info.viewportStartOffset - first.offset).coerceAtLeast(0)
                ScrollPosition(active, items.progress(item, clipped), !listState.isScrollInProgress)
            } else if (awaitingChapter == null && requestedChapter == null && currentChapterId == active) {
                ScrollPosition(item.chapter.chapterId, 0f, false)
            } else null
        }.collect { position ->
            if (position == null) return@collect
            val active = latestWindow.current.chapterId
            if (position.chapterId == active) {
                awaitingChapter = null
                report(active, position.progress, position.settled)
            } else {
                promoteVisibleChapter(position.chapterId)
            }
        }
    }

    val modifier = Modifier.fillMaxSize()
        .chapterOverscroll(mode, mode.isVertical, window.current.chapterId) { direction ->
            if (awaitingChapter == null && requestedChapter == null) {
                val visible = listState.layoutInfo.firstContentItem()?.let { displayedItems.item(it.key) }
                val visibleChapter = visible?.chapter?.chapterId
                if (visibleChapter == latestWindow.current.chapterId) {
                    adjacent(direction)
                } else if (visibleChapter != null) {
                    promoteVisibleChapter(visibleChapter)
                }
            }
        }
        .pointerInput(Unit) { detectTapGestures { toggleImmersive() } }
    val content: LazyListScope.() -> Unit = {
        items(displayedItems.items, key = ScrollItem::key, contentType = { it.component?.data?.id }) { item ->
            val component = item.component
            if (component != null) {
                ChapterComponent(component, mode)
            } else {
                val length = with(LocalDensity.current) { item.blankLength.toDp() }
                Box(if (mode.isVertical) Modifier.width(length) else Modifier.height(length))
            }
        }
    }

    SelectionContainer {
        if (mode.isVertical) {
            val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            LazyRow(
                modifier = modifier,
                state = listState,
                reverseLayout = (mode == WriteMode.VerticalRTL) != rtl,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        } else {
            LazyColumn(
                modifier = modifier,
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

private data class ScrollPosition(val chapterId: String, val progress: Float, val settled: Boolean)

private fun LazyListLayoutInfo.firstContentItem() =
    visibleItemsInfo.firstOrNull { it.offset + it.size > viewportStartOffset }
