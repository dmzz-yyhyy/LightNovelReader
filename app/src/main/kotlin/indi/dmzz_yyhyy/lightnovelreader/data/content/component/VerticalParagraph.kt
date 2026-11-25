package indi.dmzz_yyhyy.lightnovelreader.data.content.component

import android.content.ClipData
import android.icu.text.BreakIterator
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.copyText
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.text.layout.VerticalParagraphLayout
import io.nightfish.lightnovelreader.api.text.layout.resolveVerticalTypefaces
import io.nightfish.lightnovelreader.api.text.layout.verticalParagraphText
import kotlinx.coroutines.launch

@Composable
internal fun VerticalParagraph(
    text: AnnotatedString,
    style: TextStyle,
    orientation: WriteMode,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    val resolver = LocalFontFamilyResolver.current
    // Read resolver states during composition so asynchronously loaded fonts trigger a new layout.
    val typefaces = resolveVerticalTypefaces(text, style, resolver)
    val paragraph = remember(text, style, density, typefaces) {
        verticalParagraphText(text, style, density, typefaces)
    }
    var textLayout by remember(paragraph) { mutableStateOf<VerticalParagraphLayout?>(null) }
    val selection = LocalVerticalTextSelection.current
    val selectable = if (selection != null && text.text.isNotBlank()) {
        Modifier.verticalSelectionModifier(text.text, orientation, selection) { textLayout }
    } else Modifier
    Layout(
        content = {},
        modifier = modifier
            .semantics { this.text = text }
            .clipToBounds()
            .then(selectable)
            .drawBehind {
                drawIntoCanvas { canvas ->
                    textLayout?.draw(canvas.nativeCanvas, size.width, orientation)
                }
            },
    ) { _, constraints ->
        val measured = paragraph.layout(constraints.maxHeight)
        textLayout = measured
        layout(
            constraints.constrainWidth(measured.width),
            constraints.constrainHeight(measured.height),
        ) {}
    }
}

@Composable
private fun Modifier.verticalSelectionModifier(
    text: String,
    orientation: WriteMode,
    selection: VerticalTextSelection,
    layout: () -> VerticalParagraphLayout?,
): Modifier {
    val owner = remember(text, orientation) { Any() }
    var range by remember(owner) { mutableStateOf(TextRange.Zero) }
    var dragging by remember(owner) { mutableStateOf(false) }
    var coordinates by remember(owner) { mutableStateOf<LayoutCoordinates?>(null, neverEqualPolicy()) }
    val active = selection.owner === owner
    val haptic = LocalHapticFeedback.current
    val colors = LocalTextSelectionColors.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val radius = with(LocalDensity.current) { 5.dp.toPx() }

    fun selectWord(offset: Int) {
        val words = BreakIterator.getWordInstance().apply { setText(text) }
        range = TextRange(words.preceding(offset + 1), words.following(offset))
        selection.owner = owner
    }

    fun copy() {
        val selected = text.substring(range.min, range.max)
        selection.clear()
        scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(null, selected))) }
    }

    DisposableEffect(selection, owner) {
        onDispose { if (selection.owner === owner) selection.clear() }
    }
    if (active && !dragging) {
        val positioned = coordinates?.takeIf { it.isAttached }
        val rects = positioned?.let { layout()?.selectionRects(range, it.size.width.toFloat(), orientation) }
        val anchor = rects?.let { positioned.selectionBoundsInWindow(it) }
        anchor?.let { bounds ->
            VerticalSelectionMenu(
                anchorInWindow = bounds,
                canSelectAll = range != TextRange(0, text.length),
                copy = ::copy,
                selectAll = { range = TextRange(0, text.length) },
                dismiss = { if (selection.owner === owner) selection.clear() },
            )
        }
    }

    return this
        .onGloballyPositioned { coordinates = it }
        .semantics {
            onLongClick { selectWord(text.indexOfFirst { !it.isWhitespace() }); true }
            if (active) copyText { copy(); true }
        }
        .drawWithContent {
            val rects = if (selection.owner === owner) {
                layout()?.selectionRects(range, size.width, orientation).orEmpty()
            } else emptyList()
            rects.forEach { drawRect(colors.backgroundColor, it.topLeft, it.size) }
            drawContent()
            rects.handles(size.height, radius)?.let { (start, end) ->
                drawCircle(colors.handleColor, radius, start)
                drawCircle(colors.handleColor, radius, end)
            }
        }
        .pointerInput(owner, selection, radius) {
            awaitEachGesture {
                val down = awaitFirstDown()
                val currentLayout = layout() ?: return@awaitEachGesture
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                val handles = if (selection.owner === owner) {
                    currentLayout.selectionRects(range, width, orientation).handles(height, radius)
                } else null
                val hitRadius = 22.dp.toPx()
                val handle = handles?.let { (start, end) ->
                    val startDistance = (down.position - start).getDistance()
                    val endDistance = (down.position - end).getDistance()
                    when {
                        minOf(startDistance, endDistance) > hitRadius -> null
                        startDistance <= endDistance -> true
                        else -> false
                    }
                }
                val point = if (handle != null) down else awaitLongPressOrCancellation(down.id)
                    ?: return@awaitEachGesture
                if (handle == null) {
                    val offset =
                        currentLayout.offsetAt(point.position, width, orientation, nearest = false)
                            ?: return@awaitEachGesture
                    selectWord(offset)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                val initial = range
                val grabbedOffset = when (handle) {
                    true -> handles.first - down.position
                    false -> handles.second - down.position
                    null -> Offset.Zero
                }
                point.consume()
                dragging = true
                try {
                    consumeSelectionDrag(point.id) { position ->
                        val offset = currentLayout.offsetAt(
                            position + grabbedOffset,
                            width,
                            orientation,
                            nearest = true
                        )
                            ?: return@consumeSelectionDrag
                        range = when (handle) {
                            true -> if (offset < initial.end) TextRange(
                                offset,
                                initial.end
                            ) else range

                            false -> if (offset > initial.start) TextRange(
                                initial.start,
                                offset
                            ) else range

                            null -> TextRange(
                                minOf(initial.start, offset),
                                maxOf(initial.end, offset)
                            )
                        }
                    }
                } finally {
                    dragging = false
                }
            }
        }
}

private fun List<Rect>.handles(height: Float, radius: Float): Pair<Offset, Offset>? {
    if (isEmpty()) return null
    val inset = radius.coerceAtMost(height / 2f)
    return Offset(first().center.x, first().top.coerceIn(inset, height - inset)) to
        Offset(last().center.x, last().bottom.coerceIn(inset, height - inset))
}

private fun LayoutCoordinates.selectionBoundsInWindow(rects: List<Rect>): Rect? {
    val visible = boundsInWindow()
    val bounds = rects.mapNotNull { rect ->
        val rect = Rect(localToWindow(rect.topLeft), localToWindow(rect.bottomRight)).intersect(visible)
        rect.takeUnless { it.isEmpty }
    }
    if (bounds.isEmpty()) return null
    return Rect(bounds.minOf { it.left }, bounds.minOf { it.top }, bounds.maxOf { it.right }, bounds.maxOf { it.bottom })
}

private suspend fun AwaitPointerEventScope.consumeSelectionDrag(pointer: PointerId, onMove: (Offset) -> Unit) {
    while (true) {
        val change = awaitPointerEvent().changes.firstOrNull { it.id == pointer } ?: return
        if (change.isConsumed) return
        change.consume()
        if (!change.pressed) return
        if (change.position != change.previousPosition) onMove(change.position)
    }
}
