package indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.VibrantFloatingActionButton
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.TagChip
import indi.dmzz_yyhyy.lightnovelreader.utils.FileSizeUnit
import indi.dmzz_yyhyy.lightnovelreader.utils.fadeEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.fadeExit
import indi.dmzz_yyhyy.lightnovelreader.utils.formTime
import indi.dmzz_yyhyy.lightnovelreader.utils.formatSize
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocalBookManagerContent(
    uiState: LocalBookManagerUiState
) {
    var deleteDialogVisible by remember { mutableStateOf(false) }
    var infoItem by remember { mutableStateOf<LocalBookItem?>(null) }
    var clearItem by remember { mutableStateOf<LocalBookItem?>(null) }
    var clearTargets by remember { mutableStateOf(emptyList<LocalBookClearTarget>()) }
    var confirmClear by remember { mutableStateOf(false) }
    var indexOnlyCollapsed by rememberSaveable { mutableStateOf(true) }
    val list = when (uiState.sort) {
        LocalBookSort.Size -> uiState.bookList.sortedBy { it.size }
        LocalBookSort.LastRead -> uiState.bookList.sortedBy { it.lastReadTime }
        LocalBookSort.ChapterCount -> uiState.bookList.sortedBy { it.chapterCount }
    }.let {
        if (uiState.sortReverse) it.reversed() else it
    }
    val contentList = remember(list) { list.filter(LocalBookItem::hasChapterContent) }
    val indexOnlyList = remember(list) { list.filterNot(LocalBookItem::hasChapterContent) }
    val shownInfoItem = remember(uiState.bookList, infoItem) {
        infoItem?.let { current ->
            uiState.bookList.firstOrNull { it.id == current.id } ?: current
        }
    }
    BackHandler(enabled = shownInfoItem != null) {
        if (clearItem != null) {
            clearItem = null
            clearTargets = emptyList()
        } else {
            infoItem = null
        }
    }
    LaunchedEffect(uiState.isSelecting) {
        if (uiState.isSelecting) {
            infoItem = null
            clearItem = null
            clearTargets = emptyList()
            confirmClear = false
        }
    }
    if (uiState.isLoading && uiState.bookList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    if (uiState.bookList.isEmpty()) {
        EmptyPage(
            modifier = Modifier.navigationBarsPadding(),
            icon = painterResource(id = R.drawable.menu_book_24px),
            title = stringResource(R.string.book_manager_empty_title),
            description = stringResource(R.string.book_manager_empty_description)
        )
        return
    }
    if (deleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { deleteDialogVisible = false },
            title = { Text(stringResource(R.string.book_manager_delete_cache_title)) },
            text = { Text(stringResource(R.string.book_manager_delete_cache_content)) },
            dismissButton = {
                TextButton(onClick = { deleteDialogVisible = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteDialogVisible = false
                        uiState.deleteSelected()
                    },
                    enabled = uiState.selectedIds.isNotEmpty() && !uiState.isDeleting
                ) {
                    Text(
                        if (uiState.isDeleting) stringResource(R.string.processing)
                        else stringResource(R.string.confirm)
                    )
                }
            }
        )
    }
    clearItem?.let { item ->
        val selectedBytes = clearTargets.sumOf { item.bytesOf(it) }
        val selectedContent = clearTargets.map {
            stringResource(it.label) to formatSize(item.bytesOf(it))
        }
        if (confirmClear) {
            AlertDialog(
                onDismissRequest = { confirmClear = false },
                title = { Text(stringResource(R.string.local_book_clear_confirm_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            stringResource(
                                R.string.local_book_clear_confirm_content,
                                formatSize(selectedBytes)
                            )
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            selectedContent.forEach { (label, size) ->
                                Text("\u2022 $label ($size)")
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmClear = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            uiState.clearBookData(item.id, clearTargets)
                            infoItem = null
                            clearItem = null
                            clearTargets = emptyList()
                            confirmClear = false
                        },
                        enabled = clearTargets.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.local_book_clear))
                    }
                }
            )
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(
                top = 4.dp,
                bottom = when {
                    uiState.isSelecting -> 96.dp
                    shownInfoItem != null -> 320.dp
                    else -> 0.dp
                }
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(
                items = contentList,
                key = { "book_${it.id}" }
            ) { item ->
                LocalBookRow(
                    modifier = Modifier.animateItem(),
                    item = item,
                    uiState = uiState,
                    onToggleInfo = {
                        clearItem = null
                        clearTargets = emptyList()
                        confirmClear = false
                        infoItem = if (infoItem?.id == item.id) null else item
                    }
                )
            }
            if (indexOnlyList.isNotEmpty()) {
                stickyHeader(key = "index_only_header") {
                    CollapseHeader(
                        icon = painterResource(R.drawable.menu_book_24px),
                        title = stringResource(R.string.book_manager_group_index_only, indexOnlyList.size),
                        expanded = !indexOnlyCollapsed,
                        onToggleExpand = { indexOnlyCollapsed = !indexOnlyCollapsed }
                    )
                }
                if (!indexOnlyCollapsed) {
                    items(
                        items = indexOnlyList,
                        key = { "index_only_${it.id}" }
                    ) { item ->
                        LocalBookRow(
                            modifier = Modifier.animateItem(),
                            item = item,
                            uiState = uiState,
                            onToggleInfo = {
                                clearItem = null
                                clearTargets = emptyList()
                                confirmClear = false
                                infoItem = if (infoItem?.id == item.id) null else item
                            }
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        AnimatedContent(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            targetState = shownInfoItem,
            transitionSpec = {
                (
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(200, easing = FastOutSlowInEasing))
                    ) togetherWith (
                    slideOutVertically(
                        targetOffsetY = { it / 3 },
                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(150, easing = FastOutSlowInEasing))
                    )
            },
            label = "infoBar"
        ) { item ->
            if (item != null) {
                LocalBookInfoCard(
                    item = item,
                    isClearing = clearItem?.id == item.id,
                    clearTargets = clearTargets,
                    onBack = {
                        if (clearItem?.id == item.id) {
                            clearItem = null
                            clearTargets = emptyList()
                            confirmClear = false
                        } else {
                            infoItem = null
                        }
                    },
                    onToggleClearTarget = { target ->
                        clearTargets = clearTargets.toMutableList().apply {
                            if (contains(target)) remove(target) else add(target)
                        }
                    },
                    onEnterClear = {
                        clearItem = item
                        clearTargets = emptyList()
                        confirmClear = false
                    },
                    onCancelClear = {
                        clearItem = null
                        clearTargets = emptyList()
                        confirmClear = false
                    },
                    onConfirmClear = { confirmClear = true },
                    onOpenDetail = { uiState.openBookDetailScreen(item.id) }
                )
            } else {
                Spacer(Modifier)
            }
        }
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp),
            visible = uiState.isSelecting,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(200, easing = FastOutSlowInEasing)),
            exit = slideOutVertically(
                targetOffsetY = { it / 4 },
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(150, easing = FastOutSlowInEasing))
        ) {
            val vibrantColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

            HorizontalFloatingToolbar(
                expanded = true,
                floatingActionButtonPosition = FloatingToolbarHorizontalFabPosition.Start,
                floatingActionButton = {
                    VibrantFloatingActionButton(
                        onClick = uiState.exitSelection
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close_24px),
                            contentDescription = "close"
                        )
                    }
                },
                colors = vibrantColors,
                content = {
                    IconButton(uiState.selectAll) {
                        Icon(
                            painter = painterResource(id = R.drawable.select_all_24px),
                            contentDescription = "select_all"
                        )
                    }
                    IconButton({ deleteDialogVisible = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete_forever_24px),
                            contentDescription = "delete"
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun CollapseHeader(
    icon: Painter,
    title: String,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggleExpand)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.width(20.dp),
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.W600
        )
        val rotation by animateFloatAsState(if (expanded) 0f else 180f, label = "rotation")
        Icon(
            modifier = Modifier.graphicsLayer { rotationZ = rotation },
            painter = painterResource(R.drawable.keyboard_arrow_up_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocalBookRow(
    modifier: Modifier = Modifier,
    item: LocalBookItem,
    uiState: LocalBookManagerUiState,
    onToggleInfo: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (uiState.isSelecting) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "selectionProgress"
    )

    val roundedCorner = RoundedCornerShape(10.dp)
    val sideSlotWidth = 48.dp
    val density = LocalDensity.current
    val sideSlotPx = with(density) { sideSlotWidth.roundToPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(roundedCorner)
            .combinedClickable(
                onClick = {
                    if (uiState.isSelecting) {
                        uiState.toggleSelect(item.id)
                    } else {
                        uiState.openBookDetailScreen(item.id)
                    }
                },
                onLongClick = {
                    uiState.enterSelection(item.id)
                }
            )
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = sideSlotWidth)
                .offset {
                    IntOffset(
                        x = (sideSlotPx * progress).roundToInt(),
                        y = 0
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Cover(
                width = 64.dp,
                height = 93.dp,
                uri = item.bookInformation.coverUri
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.bookInformation.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W600
                )
                Text(
                    text = formatSize(item.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TagChip(painterResource(R.drawable.update_24px))
                    Text(
                        text = if (item.lastReadTime != null) formTime(item.lastReadTime)
                        else stringResource(R.string.book_manager_no_local_reading_record),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        if (progress > 0.001f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(sideSlotWidth),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = item.id in uiState.selectedIds,
                    onCheckedChange = {
                        uiState.toggleSelect(item.id)
                    },
                    modifier = Modifier.graphicsLayer {
                        alpha = progress
                        scaleX = 0.85f + 0.15f * progress
                        scaleY = 0.85f + 0.15f * progress
                        translationX = -sideSlotPx * 0.35f * (1f - progress)
                    }
                )
            }
        }

        if (progress < 0.999f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(sideSlotWidth),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onToggleInfo,
                    modifier = Modifier.graphicsLayer {
                        val p = 1f - progress
                        alpha = p
                        scaleX = 0.85f + 0.15f * p
                        scaleY = 0.85f + 0.15f * p
                        translationX = sideSlotPx * 0.35f * progress
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.info_24px),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "info"
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalBookInfoCard(
    item: LocalBookItem,
    isClearing: Boolean,
    clearTargets: List<LocalBookClearTarget>,
    onBack: () -> Unit,
    onToggleClearTarget: (LocalBookClearTarget) -> Unit,
    onEnterClear: () -> Unit,
    onCancelClear: () -> Unit,
    onConfirmClear: () -> Unit,
    onOpenDetail: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.outlinedIconButtonColors()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.expand_circle_down_24px),
                        contentDescription = "expand"
                    )
                }
                Text(
                    text = stringResource(R.string.local_book_info_title),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Cover(
                    width = 64.dp,
                    height = 93.dp,
                    uri = item.bookInformation.coverUri
                )

                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.bookInformation.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.W600
                    )
                    Text(
                        text = item.bookInformation.author,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.detail_info_stats_count_content,
                            item.volumeCount,
                            item.chapterCount
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            AnimatedContent(
                targetState = isClearing,
                transitionSpec = {
                    fadeEnter() togetherWith fadeExit()
                },
                label = "LocalBookInfoContent"
            ) { clearing ->
                if (!clearing) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LocalBookInfoRow(
                            stringResource(R.string.local_book_info_book_information),
                            formatSize(
                                size = item.bookInformationBytes,
                                minUnit = FileSizeUnit.KB
                            )
                        )
                        LocalBookInfoRow(
                            stringResource(R.string.local_book_info_volume_index),
                            formatSize(
                                size = item.volumeBytes,
                                minUnit = FileSizeUnit.KB
                            )
                        )
                        LocalBookInfoRow(
                            stringResource(R.string.local_book_info_chapter_information),
                            formatSize(
                                size = item.chapterInformationBytes,
                                minUnit = FileSizeUnit.KB
                            )
                        )
                        LocalBookInfoRow(
                            stringResource(R.string.local_book_info_chapter_content),
                            formatSize(
                                size = item.chapterContentBytes,
                                minUnit = FileSizeUnit.KB
                            )
                        )
                        LocalBookInfoRow(
                            stringResource(R.string.local_book_info_reading_record),
                            formatSize(
                                size = item.readingRecordBytes,
                                minUnit = FileSizeUnit.KB
                            )
                        )
                        HorizontalDivider()
                        LocalBookInfoRow(
                            stringResource(R.string.local_book_info_total),
                            formatSize(item.size),
                            emphasized = true
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        SectionHeader(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(R.string.local_book_clear_options)
                        )
                        LocalBookClearTarget.entries.forEachIndexed { index, target ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        onClick = { onToggleClearTarget(target) }
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .weight(1f)
                                    ) {
                                    Text(
                                        text = stringResource(target.label),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = formatSize(item.bytesOf(target)),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Checkbox(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    checked = target in clearTargets,
                                    onCheckedChange = { onToggleClearTarget(target) }
                                )
                            }
                            if (index != LocalBookClearTarget.entries.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isClearing) {
                    OutlinedButton(
                        onClick = onCancelClear,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = onConfirmClear,
                        enabled = clearTargets.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(stringResource(R.string.local_book_clear))
                    }
                } else {
                    OutlinedButton(
                        onClick = onEnterClear,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.local_book_clear))
                    }

                    Button(
                        onClick = onOpenDetail,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.detail_title))
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalBookInfoRow(
    label: String,
    value: String,
    emphasized: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = if (emphasized) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.W600 else null
        )
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.W600 else null
        )
    }
}
