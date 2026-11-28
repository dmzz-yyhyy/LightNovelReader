package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.valentinilk.shimmer.shimmer
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.ReaderScreenUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip.FlipContent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll.ScrollContent
import indi.dmzz_yyhyy.lightnovelreader.ui.components.rememberLoadingSkeletonShimmer
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.readerBackgroundColor
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import io.nightfish.lightnovelreader.api.content.builder.ParagraphBuilder
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.ui.LocalComponentRender
import io.nightfish.lightnovelreader.api.ui.LocalReaderStyle
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

internal data class ChapterLayoutWindow(
    val previous: ChapterLayout?,
    val current: ChapterLayout,
    val next: ChapterLayout?,
    val previousId: Identifier.Chapter?,
    val nextId: Identifier.Chapter?,
) {
    val chapters get() = listOfNotNull(previous, current, next)
}

@Composable
fun ChapterContent(
    modifier: Modifier = Modifier,
    uiState: ReaderScreenUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onReadingProgress: (chapterId: Identifier.Chapter, progress: Float, settled: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val style = LocalReaderStyle.current
    val mode = style.writeMode
    val render = LocalComponentRender.current
    val baseStyle = MaterialTheme.typography.bodyMedium
    val flip = settingState.isUsingFlipPage
    val bookId = uiState.bookId
    val current = uiState.currentChapterContent?.get()
    val previous = uiState.preChapterContent?.get()
    val next = uiState.nextChapterContent?.get()
    val volumes = uiState.bookVolumes?.get()

    BoxWithConstraints(modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        val viewport = IntSize(constraints.maxWidth, constraints.maxHeight)
        val layouter = remember(bookId, flip, viewport, render, context, density, style, baseStyle) {
            ChapterLayouter(render, viewport, context, density, style, baseStyle, flip)
        }
        val layouts = remember(layouter) {
            mutableStateMapOf<ChapterContentUiState, Result<ChapterLayout, WebRequestError>>()
        }

        if (viewport.width > 0 && viewport.height > 0) {
            val chapters = listOfNotNull(current, previous, next).distinctBy { it.id }
            val activeChapters by rememberUpdatedState(chapters)
            for (chapter in chapters) {
                key(layouter, chapter) {
                    LaunchedEffect(Unit) {
                        if (chapter in layouts) return@LaunchedEffect
                        val result = try {
                            Ok(withContext(Dispatchers.Default) {
                                val components = if (chapter.title.isBlank()) {
                                    chapter.content
                                } else {
                                    buildList {
                                        add(chapterHeading(chapter.title, style))
                                        addAll(chapter.content)
                                    }
                                }
                                layouter.layout(chapter.id, components)
                            })
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            Log.e("ChapterContent", "Failed to lay out chapter ${chapter.id}", error)
                            Err(WebRequestError("Chapter layout failed", error.message.orEmpty(), error))
                        }
                        if (layouts.size >= 6) {
                            layouts.keys.firstOrNull { it !in activeChapters }?.let(layouts::remove)
                        }
                        layouts[chapter] = result
                    }
                }
            }
        }

        val measured = current?.let { layouts[it] }
        val layout = measured?.get()
        val error = uiState.currentChapterContent?.getError() ?: measured?.getError()
        val ready = current != null && layout != null && uiState.userReadingData != null
        if (ready) {
            val window = ChapterLayoutWindow(
                previous = previous?.let { layouts[it]?.get() },
                current = layout,
                next = next?.let { layouts[it]?.get() },
                previousId = current.previousId(volumes),
                nextId = current.nextId(volumes),
            )
            val onAdjacent = rememberChapterNavigation(window, onClickPrevChapter, onClickNextChapter)
            key(layouter) {
                if (flip) {
                    FlipContent(
                        window = window,
                        mode = mode,
                        settingState = settingState,
                        entryProgress = uiState.readingProgress ?: 0f,
                        onAdjacent = onAdjacent,
                        onToggleImmersive = changeIsImmersive,
                        onReadingProgress = onReadingProgress,
                    )
                } else {
                    ScrollContent(
                        window = window,
                        mode = mode,
                        viewportLength = mode.main(viewport),
                        entryProgress = uiState.readingProgress ?: 0f,
                        onAdjacent = onAdjacent,
                        onToggleImmersive = changeIsImmersive,
                        onReadingProgress = onReadingProgress,
                    )
                }
            }
        }
        // 加载中 / 加载失败时正文组件(Flip/ScrollContent)并不存在，原先整页没有任何点击响应，
        // 用户无法呼出菜单栏(返回、目录、设置)；若返回拦截设为"完全拦截"则会被困在阅读页。
        // 这里让加载页与错误页也能通过点击切换菜单栏。
        val toggleImmersive by rememberUpdatedState(changeIsImmersive)
        val tapToToggleImmersive = remember {
            Modifier.pointerInput(Unit) {
                detectTapGestures { toggleImmersive() }
            }
        }
        AnimatedVisibility(!ready && error == null, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier
                .fillMaxSize()
                .then(tapToToggleImmersive)
                .background(readerBackgroundColor(settingState).copy(alpha = .92f))) {
                ChapterContentLoading(mode, style)
            }
        }
        AnimatedVisibility(!ready && error != null, enter = fadeIn(), exit = fadeOut()) {
            ChapterContentError(tapToToggleImmersive)
        }
    }
}

private fun chapterHeading(title: String, readerStyle: ReaderStyle) =
    ParagraphBuilder().apply {
        text(title)
        style {
            fontSize((readerStyle.fontSize.value + 6).sp)
            fontWeight(FontWeight.SemiBold)
            textIndent(TextIndent.None)
            textAlign(TextAlign.Center)
        }
    }.build()

@Composable
private fun rememberChapterNavigation(
    window: ChapterLayoutWindow,
    previous: () -> Unit,
    next: () -> Unit,
): (Int) -> Unit {
    val host = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val start = stringResource(R.string.reader_reached_start)
    val end = stringResource(R.string.reader_reached_end)
    val confirm = stringResource(R.string.confirm)
    val latestWindow by rememberUpdatedState(window)
    val goBack by rememberUpdatedState(previous)
    val goForward by rememberUpdatedState(next)
    return remember(host, scope, start, end, confirm) {
        { direction ->
            val id = if (direction < 0) latestWindow.previousId else latestWindow.nextId
            if (id != null) {
                host.currentSnackbarData?.dismiss()
                if (direction < 0) goBack() else goForward()
            } else if (host.currentSnackbarData == null) {
                showSnackbar(scope, host, if (direction < 0) start else end, confirm)
            }
        }
    }
}

@Composable
internal fun Modifier.chapterOverscroll(
    mode: WriteMode,
    horizontal: Boolean,
    chapterId: Identifier.Chapter,
    onAdjacent: (Int) -> Unit,
): Modifier {
    val rtl = if (mode.isVertical) mode == WriteMode.VerticalRTL
        else LocalLayoutDirection.current == LayoutDirection.Rtl
    val threshold = with(LocalDensity.current) { 36.dp.toPx() }
    val onEdge by rememberUpdatedState(onAdjacent)
    val connection = remember(horizontal, rtl, threshold, chapterId) {
        object : NestedScrollConnection {
            private var overscroll = 0f
            private var notified = false

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput || notified) return Offset.Zero
                val remaining = if (horizontal) available.x else available.y
                val moved = if (horizontal) consumed.x else consumed.y
                if (moved != 0f || remaining * overscroll < 0f) overscroll = 0f
                overscroll += remaining
                if (abs(overscroll) >= threshold) {
                    notified = true
                    onEdge(if ((overscroll > 0f) != (horizontal && rtl)) -1 else 1)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                overscroll = 0f
                notified = false
                return Velocity.Zero
            }
        }
    }
    return nestedScroll(connection)
}

@Composable
private fun ChapterContentLoading(mode: WriteMode, style: ReaderStyle) {
    val shimmer = rememberLoadingSkeletonShimmer()
    val color = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(Modifier
        .fillMaxSize()
        .shimmer(shimmer)) {
        val main = if (mode.isVertical) size.width else size.height
        val cross = if (mode.isVertical) size.height else size.width
        val fontSize = style.fontSize.toPx()
        val spacing = fontSize * 1.65f
        val thickness = fontSize * .6f
        if (spacing <= 0f) return@Canvas
        repeat((main / spacing).toInt()) { line ->
            val length = cross * if (line % 5 == 4) .62f else .94f
            val offset = line * spacing + thickness
            val topLeft = when (mode) {
                WriteMode.Horizontal -> Offset(0f, offset)
                WriteMode.VerticalLTR -> Offset(offset, 0f)
                WriteMode.VerticalRTL -> Offset((main - offset - thickness).coerceAtLeast(0f), 0f)
            }
            drawRoundRect(
                color = color,
                topLeft = topLeft,
                size = if (mode.isVertical) Size(thickness, length) else Size(length, thickness),
                cornerRadius = CornerRadius(thickness / 2f),
            )
        }
    }
}

@Composable
private fun ChapterContentError(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.error_chapter_content_generic),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}
