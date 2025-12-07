package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.markAllChaptersAsReadDialog() {
    dialog<Route.MarkAllChaptersAsReadDialog> { backStackEntry ->
        val navController = LocalNavController.current
        val viewModel: MarkAllChaptersAsReadDialogViewModel = hiltViewModel()
        val route = backStackEntry.toRoute<Route.MarkAllChaptersAsReadDialog>()

        LaunchedEffect(route.bookId) {
            viewModel.load(route.bookId)
        }

        val bookVolumes = viewModel.bookVolumes

        MarkAllChaptersAsReadDialog(
            onDismissRequest = navController::popBackStack,
            onConfirmAll = {
                viewModel.markAllChaptersAsRead()
                navController.popBackStack()
            },
            onConfirmRange = { ids ->
                viewModel.markChaptersAsRead(ids)
                navController.popBackStack()
            },
            volumes = bookVolumes.volumes
        )
    }
}

fun NavController.navigateToMarkAllChaptersAsReadDialog(bookId: String) {
    navigate(Route.MarkAllChaptersAsReadDialog(bookId))
}

private enum class MarkReadMode { All, Range }

@Composable
fun MarkAllChaptersAsReadDialog(
    onDismissRequest: () -> Unit,
    onConfirmAll: () -> Unit,
    onConfirmRange: (List<String>) -> Unit,
    volumes: List<Volume>
) {
    val allChapterIds = remember(volumes) {
        volumes.flatMap { it.chapters }.map { it.id }
    }
    val indexMap = remember(allChapterIds) {
        allChapterIds.withIndex().associate { it.value to it.index }
    }

    var mode by remember { mutableStateOf(MarkReadMode.All) }
    var startChapterId by remember { mutableStateOf<String?>(null) }
    var endChapterId by remember { mutableStateOf<String?>(null) }

    val selectedIds = remember(startChapterId, endChapterId, allChapterIds) {
        val s = startChapterId?.let(indexMap::get) ?: return@remember emptyList()
        val e = endChapterId?.let(indexMap::get)

        if (e == null) {
            listOf(allChapterIds[s])
        } else {
            val from = minOf(s, e)
            val to = maxOf(s, e)
            allChapterIds.subList(from, to + 1)
        }
    }

    fun clearSelection() {
        startChapterId = null
        endChapterId = null
    }

    fun onClickChapter(id: String) {
        val s = startChapterId
        val e = endChapterId
        when {
            s == null -> {
                startChapterId = id
                endChapterId = null
            }
            e == null -> {
                if (id == s) clearSelection() else endChapterId = id
            }
            else -> {
                startChapterId = id
                endChapterId = null
            }
        }
    }

    val primaryEnabled = allChapterIds.isNotEmpty() &&
            (mode == MarkReadMode.All || selectedIds.isNotEmpty())

    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.mark_as_read),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MarkReadModeSegmentedButton(
                    mode = mode,
                    onChange = { mode = it }
                )

                val desc = when (mode) {
                    MarkReadMode.All -> "将本书所有章节标记为已读。"
                    MarkReadMode.Range -> "选择起点和终点章节，将范围内章节标记为已读。"
                }
                Text(
                    desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (mode == MarkReadMode.Range) {
                    ChapterRangeTree(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .clipToBounds(),
                        volumes = volumes,
                        selectedIds = selectedIds.toSet(),
                        startId = startChapterId,
                        endId = endChapterId,
                        onClickChapter = ::onClickChapter
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mode == MarkReadMode.All) {
                        onConfirmAll()
                    } else {
                        onConfirmRange(selectedIds)
                    }
                },
                enabled = primaryEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                val label = when (mode) {
                    MarkReadMode.All -> "标记全部为已读"
                    MarkReadMode.Range -> "标记已选 ${selectedIds.size} 章为已读"
                }
                Text(label)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun MarkReadModeSegmentedButton(
    mode: MarkReadMode,
    onChange: (MarkReadMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = mode == MarkReadMode.All,
            onClick = { onChange(MarkReadMode.All) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) {
            Text("全部章节")
        }

        SegmentedButton(
            selected = mode == MarkReadMode.Range,
            onClick = { onChange(MarkReadMode.Range) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) {
            Text("选择范围")
        }
    }
}

@Composable
private fun ChapterRangeTree(
    modifier: Modifier,
    volumes: List<Volume>,
    selectedIds: Set<String>,
    startId: String?,
    endId: String?,
    onClickChapter: (String) -> Unit
) {
    val volumeKeys = remember(volumes) { volumes.map { it.volumeId } }

    val expandStates = remember(volumeKeys) {
        mutableStateMapOf<String, Boolean>().apply {
            volumes.forEach { this[it.volumeId] = true }
        }
    }

    Surface(
        modifier = modifier.clipToBounds(),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 6.dp)
        ) {
            volumes.forEach { volume ->
                val expanded = expandStates[volume.volumeId] ?: true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandStates[volume.volumeId] = !expanded }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = volume.volumeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                        modifier = Modifier.rotate(if (expanded) 180f else 90f),
                        contentDescription = null
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        volume.chapters.forEach { chapter ->
                            ChapterSelectableRow(
                                chapter = chapter,
                                selected = chapter.id in selectedIds,
                                isStart = chapter.id == startId,
                                isEnd = chapter.id == endId,
                                onClick = { onClickChapter(chapter.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterSelectableRow(
    chapter: ChapterInformation,
    selected: Boolean,
    isStart: Boolean,
    isEnd: Boolean,
    onClick: () -> Unit
) {
    val bg =
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surface

    val showMark = isStart || isEnd

    val markWidth by animateDpAsState(
        targetValue = if (showMark) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = 180)
    )

    val markAlpha by animateFloatAsState(
        targetValue = if (showMark) 1f else 0f,
        animationSpec = tween(durationMillis = 140)
    )

    val markTx by animateDpAsState(
        targetValue = if (showMark) 0.dp else (-6).dp,
        animationSpec = tween(durationMillis = 180)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .width(markWidth)
                    .clipToBounds(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.keyboard_double_arrow_right_24px),
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer {
                            alpha = markAlpha
                            translationX = markTx.toPx()
                        }
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = chapter.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
