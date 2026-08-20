package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import io.nightfish.lightnovelreader.api.book.BookVolumes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSelectionBottomSheet(
    sheetState: SheetState,
    selectedVolumeId: String,
    bookVolumes: BookVolumes,
    readingChapterId: String,
    onDismissRequest: () -> Unit,
    onClickChapter: (chapterId: String) -> Unit,
    onChangeSelectedVolumeId: (volumeId: String) -> Unit
) {
    val lazyColumnState = rememberLazyListState()

    var autoScrolled by remember(readingChapterId, bookVolumes) {
        mutableStateOf(false)
    }

    LaunchedEffect(sheetState.currentValue, autoScrolled, readingChapterId, bookVolumes) {
        if (autoScrolled) return@LaunchedEffect
        if (sheetState.currentValue != SheetValue.Expanded) return@LaunchedEffect
        if (readingChapterId.isBlank()) return@LaunchedEffect

        val volumes = bookVolumes.volumes
        val volumeIndex = volumes.indexOfFirst { volume ->
            volume.chapters.any { it.id == readingChapterId }
        }
        if (volumeIndex < 0) return@LaunchedEffect

        val volume = volumes[volumeIndex]
        onChangeSelectedVolumeId(volume.volumeId)

        lazyColumnState.scrollToItem(volumeIndex)
        autoScrolled = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.read_more_24px),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(R.string.select_chapter),
                        style = typography.displayMedium,
                        fontWeight = FontWeight.W600
                    )
                }

                Spacer(Modifier.height(8.dp))

                val isEmpty = bookVolumes.volumes.all { it.chapters.isEmpty() }

                if (isEmpty) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Loading()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = lazyColumnState
                    ) {
                        items(
                            items = bookVolumes.volumes,
                            key = { it.volumeId }
                        ) { volume ->
                            val expanded = selectedVolumeId == volume.volumeId

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = tween(durationMillis = 200))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            onChangeSelectedVolumeId(
                                                if (selectedVolumeId == volume.volumeId) ""
                                                else volume.volumeId
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = volume.volumeTitle,
                                                fontWeight = FontWeight.W600,
                                                style = typography.titleMedium,
                                                color = colorScheme.onSurface
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.info_volume_chapters_count,
                                                    volume.chapters.size
                                                ),
                                                color = colorScheme.secondary,
                                                style = typography.labelMedium
                                            )
                                        }
                                        Spacer(Modifier.weight(2f))
                                        Icon(
                                            modifier = Modifier
                                                .scale(0.75f)
                                                .rotate(if (expanded) -90f else 90f),
                                            painter = painterResource(R.drawable.arrow_forward_ios_24px),
                                            tint = colorScheme.onSurface,
                                            contentDescription = null
                                        )
                                    }
                                }

                                if (expanded) {
                                    volume.chapters.forEach { chapter ->
                                        val isSelected = readingChapterId == chapter.id
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(42.dp)
                                                .clickable { onClickChapter(chapter.id) }
                                                .padding(horizontal = 22.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isSelected) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.play_arrow_24px),
                                                        tint = colorScheme.outline,
                                                        contentDescription = null
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                }
                                                Text(
                                                    text = chapter.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    style = typography.titleSmall,
                                                    color = colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
