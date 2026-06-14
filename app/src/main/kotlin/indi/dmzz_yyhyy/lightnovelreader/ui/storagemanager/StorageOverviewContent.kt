package indi.dmzz_yyhyy.lightnovelreader.ui.storagemanager

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.predefinedColors
import indi.dmzz_yyhyy.lightnovelreader.utils.FileSizeUnit
import indi.dmzz_yyhyy.lightnovelreader.utils.formatSize
import kotlin.text.format

@Composable
fun StorageOverviewContent(
    modifier: Modifier,
    uiState: StorageManagerUiState
) {
    val expandedSection = uiState.sections.firstOrNull { it.title == uiState.expandedTitle } ?: uiState.sections.firstOrNull()
    if (uiState.isLoading && uiState.sections.isEmpty()) {
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
    if (!uiState.isLoading && uiState.totalSize <= 0L) {
        EmptyPage(
            modifier = Modifier.navigationBarsPadding(),
            icon = painterResource(id = R.drawable.menu_book_24px),
            title = stringResource(R.string.storage_manager_title),
            description = stringResource(R.string.storage_manager_empty_description)
        )
        return
    }
    LazyColumn(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                text = stringResource(R.string.overview)
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(102.dp)
                                .padding(all = 8.dp)
                        ) {
                            val stroke = size.minDimension * 0.2f
                            var start = -90f
                            uiState.sections.forEachIndexed { index, item ->
                                val sweep = if (uiState.totalSize == 0L) 0f else item.size.toFloat() / uiState.totalSize * 360f
                                drawArc(
                                    color = sectionColor(index),
                                    startAngle = start,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Butt),
                                    size = Size(size.width, size.height)
                                )
                                start += sweep
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = formatSize(
                                    size = uiState.totalSize,
                                    minUnit = FileSizeUnit.MB
                                ),
                                style = MaterialTheme.typography.displayLarge
                            )
                            Text(
                                text = stringResource(R.string.storage_manager_used),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    uiState.sections.forEachIndexed { index, item ->
                        val progress = if (uiState.totalSize == 0L) 0f else item.size.toFloat() / uiState.totalSize.toFloat()
                        Column(
                            modifier = Modifier
                                .clickable { uiState.selectSection(item.title) }
                                .padding(vertical = 10.dp, horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .background(sectionColor(index), CircleShape)
                                )
                                Text(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 10.dp),
                                    text = stringResource(item.title),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.W600
                                )
                                Text(
                                    text = "%.1f%%".format(progress * 100f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    text = formatSize(item.size),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.W700
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 22.dp),
                                color = sectionColor(index),
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                    HorizontalDivider(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                    expandedSection?.let { section ->
                        Column(
                            modifier = Modifier.padding(all = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(section.title),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.W600
                                )
                                Text(
                                    text = formatSize(section.size),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.W700
                                )
                            }
                            Text(
                                text = stringResource(section.description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

    }
}

private fun sectionColor(index: Int): Color {
    return predefinedColors[index % predefinedColors.size]
}
