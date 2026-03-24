package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import android.text.format.DateUtils
import androidx.compose.compiler.plugins.kotlin.lower.fastForEach
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.BookStack
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.StatsCard
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.StatsDetailedUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.currentDateRange
import io.nightfish.lightnovelreader.api.book.BookInformation
import java.time.LocalDate

val predefinedColors = listOf(
    Color(0xFF2196F3),
    Color(0xFF4CAF50),
    Color(0xFFFF9800),
    Color(0xFFF44336),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFF3F51B5),
    Color(0xFFFF5722),
)

private fun assignColors(
    records: List<BookRecordEntity>
): Map<String, Color> {
    return records
        .groupBy { it.bookId }
        .mapValues { (_, list) -> list.sumOf { it.totalTime } }
        .toList()
        .sortedByDescending { it.second }
        .mapIndexed { index, (bookId, _) ->
            val color = if (index < predefinedColors.size) {
                predefinedColors[index]
            } else {
                Color.Gray
            }
            bookId to color
        }
        .toMap()
}

/**
 * @return startedBooks/favoriteBooks/finishedBooks 在日期范围内的 BookId 列表
 */
private fun getBooksInRange(
    statsMap: Map<LocalDate, ReadingStatisticsEntity>,
    dateRange: ClosedRange<LocalDate>,
    selector: (ReadingStatisticsEntity) -> List<String>
): List<String> {
    return statsMap
        .filterKeys { it in dateRange }
        .values
        .flatMap(selector)
}

/**
 * 统计详情: 活动卡片的行
 */
@Composable
private fun BookActivitySection(
    titleResId: Int,
    bookIds: List<String>,
    bookInfoMap: Map<String, BookInformation>,
    uiState: StatsDetailedUiState,
    modifier: Modifier = Modifier
) {
    if (bookIds.isEmpty()) return

    val angle = remember(titleResId) {
        when (titleResId % 3) {
            0 -> -1.2f
            1 -> 0.6f
            else -> 1.0f
        }
    }

    val displayedTitles = bookIds.distinct().mapNotNull {
        bookInfoMap[it]?.title
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            Text(
                text = stringResource(titleResId),
                style = typography.titleMedium
            )

            val titleList = displayedTitles.take(2)

            titleList.forEach {
                Text(
                    text = it,
                    style = typography.labelMedium,
                    maxLines = 1,
                    color = colorScheme.secondary,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (displayedTitles.size > titleList.size) {
                Text(
                    text = stringResource(R.string.activity_etc, displayedTitles.size),
                    style = typography.labelMedium,
                    maxLines = 1,
                    color = colorScheme.secondary,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .graphicsLayer {
                    rotationZ = angle
                    translationY = 10.dp.toPx()
                }
        ) {
            BookStack(
                uiState = uiState,
                books = bookIds,
                count = 5
            )
        }
    }
}

/**
 * 活动卡片（适用各种时间范围）
 */
@Composable
fun ActivityStatsCard(
    uiState: StatsDetailedUiState,
    modifier: Modifier = Modifier
) {
    val dateRange = uiState.currentDateRange
    val statsMap = uiState.targetDateRangeStatsMap

    val startedBooks = getBooksInRange(statsMap, dateRange) { it.startedBooks }
    val favoriteBooks = getBooksInRange(statsMap, dateRange) { it.favoriteBooks }
    val finishedBooks = getBooksInRange(statsMap, dateRange) { it.finishedBooks }

    val hasActivity = startedBooks.isNotEmpty() || favoriteBooks.isNotEmpty() || finishedBooks.isNotEmpty()
    if (!hasActivity) return

    StatsCard(
        modifier = modifier,
        title = stringResource(R.string.activity)
    ) {
        Column {
            val sections = listOf(
                R.string.activity_first_read to startedBooks,
                R.string.activity_collections to favoriteBooks,
                R.string.activity_finished to finishedBooks
            ).filter { it.second.isNotEmpty() }

            sections.forEachIndexed { index, (title, books) ->
                BookActivitySection(
                    titleResId = title,
                    bookIds = books,
                    bookInfoMap = uiState.bookInformationMap,
                    uiState = uiState
                )

                if (index != sections.lastIndex) {
                    Row {
                        HorizontalDivider(modifier = Modifier.weight(4f))
                        Spacer(Modifier.weight(6f))
                    }
                }
            }
        }
    }
}

/**
 * 阅读详情卡片（适用各种时间范围）
 */
@Composable
fun ReadingDetailStatsCard(
    uiState: StatsDetailedUiState
) {
    val dateRange = uiState.currentDateRange
    val allRecords = uiState.targetDateRangeRecordsMap
        .filterKeys { it in dateRange }
        .values
        .flatten()

    StatsCard(title = stringResource(R.string.reading_details)) {
        Column {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val orderedBooks = allRecords
                    .sortedBy { it.lastSeen }
                    .map { it.bookId }
                    .distinct()
                BookStack(
                    uiState = uiState,
                    books = orderedBooks,
                    count = 8,
                    compact = false
                )
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            ReadingTimeBar(
                recordList = allRecords,
                bookInformationMap = uiState.bookInformationMap
            )
        }
    }
}

@Composable
fun ReadingTimeBar(
    recordList: List<BookRecordEntity>?,
    bookInformationMap: Map<String, BookInformation>
) {
    if (recordList.isNullOrEmpty()) {
        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_records))
        }
        return
    }

    val grouped = recordList.groupBy { it.bookId }
    val totalTime = recordList.sumOf { it.totalTime }.toFloat()
    val sortedBooks = grouped
        .mapValues { it.value.sumOf { r -> r.totalTime } }
        .toList()
        .sortedByDescending { it.second }

    val colors = assignColors(recordList)
    val topBooks = sortedBooks.take(8)
    val othersTime = sortedBooks.drop(8).sumOf { it.second }

    val barItems = buildList {
        addAll(topBooks.map { (bookId, time) ->
            Triple(
                bookInformationMap[bookId]?.title ?: "Unknown",
                time to (time / totalTime),
                colors[bookId] ?: Color.Gray
            )
        })
        if (othersTime > 0) {
            add(Triple(
                stringResource(R.string.others),
                othersTime to (othersTime / totalTime),
                Color.Gray
            ))
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            barItems.fastForEach { (_, pair, color) ->
                val ratio = pair.second
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(ratio.coerceAtLeast(0.01f))
                        .background(color)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            barItems.fastForEach { (title, pair, color) ->
                val (timeMinutes, _) = pair
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        modifier = Modifier.weight(1f, fill = true),
                        text = title,
                        style = typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(12.dp))
                    val formattedTime = DateUtils.formatElapsedTime(timeMinutes * 1L)
                    Text(
                        text = formattedTime,
                        style = typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )

                }
            }
        }
    }
}
