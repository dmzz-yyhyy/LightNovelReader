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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.BookRecord
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.BookStack
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.StatsCard
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.StatsDetailedUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.currentDateRange
import indi.dmzz_yyhyy.lightnovelreader.utils.stats.generateTimeBarItems
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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
    records: List<BookRecord>
): Map<String, Color> {
    return records
        .groupBy { it.bookId }
        .mapValues { (_, list) -> list.sumOf { it.seconds } }
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
 * @return startedBooks/finishedBooks 在日期范围内的 BookId 列表
 */
private fun getBooksInRange(
    bookDateMap: Map<Pair<String, Flow<Result<BookInformation, WebRequestError>>>, LocalDate>,
    dateRange: ClosedRange<LocalDate>
): List<Pair<String, Flow<Result<BookInformation, WebRequestError>>>> {
    return bookDateMap
        .filterValues { it in dateRange }
        .toList()
        .sortedBy { it.second }
        .map { it.first }
}

/**
 * 统计详情: 活动卡片的行
 */
@Composable
private fun BookActivitySection(
    titleResId: Int,
    books: List<Pair<String, Flow<Result<BookInformation, WebRequestError>>>>,
    modifier: Modifier = Modifier
) {
    if (books.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(titleResId),
                style = typography.titleMedium
            )
            val bookList = books
                .take(2)
                .map { it.second }
            bookList.forEach { flow ->
                val result by flow.collectAsStateWithLifecycle(null)
                result?.onOk {
                    Text(
                        text = it.title,
                        style = typography.bodyMedium,
                        maxLines = 1,
                        color = colorScheme.secondary,
                        overflow = TextOverflow.Ellipsis
                    )
                }?.onErr {
                    //TODO 错误显示
                } ?: {
                    //TODO 加载显示
                }
            }
            if (books.size > bookList.size)
                Text(
                    text = stringResource(R.string.activity_etc, books.size),
                    style = typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
        }
        Spacer(Modifier.width(12.dp))
        Box {
            BookStack(
                modifier = Modifier,
                books = books,
                count = 5,
                rotate = 4.5f,
                scaleEnabled = false
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
    val startedBooks = getBooksInRange(uiState.bookFirstReadDateMap, dateRange)
    val finishedBooks = getBooksInRange(uiState.bookFirstFinishedDateMap, dateRange)
    val favoriteBooks = getBooksInRange(uiState.bookFavoriteDateMap, dateRange)

    val hasActivity = startedBooks.isNotEmpty() || finishedBooks.isNotEmpty() || favoriteBooks.isNotEmpty()
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
                    books = books
                )

                if (index != sections.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
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
                val books = allRecords
                    .sortedBy { it.lastSeen }
                    .map { it.bookId to it.bookInformationFlow }
                    .distinct()
                BookStack(
                    books = books,
                    count = 8,
                    compact = false
                )
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))

            ReadingTimeBar(
                recordList = allRecords,
            )
        }
    }
}

@Composable
fun ReadingTimeBar(
    recordList: List<BookRecord>?,
) {

    if (recordList.isNullOrEmpty()) return

    val colorMap = remember(recordList) {
        assignColors(recordList)
    }

    val barItemsFlow = remember(recordList) {
        generateTimeBarItems(
            recordList,
            colorMap
        ).let { flows ->
            if (flows.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(flows) {
                    it.toList()
                }
            }
        }
    }

    val barItems by barItemsFlow.collectAsStateWithLifecycle(emptyList())
    val normalizedItems = remember(barItems) {
        barItems.normalize()
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            normalizedItems.fastForEach { (item, ratio) ->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(ratio)
                        .background(item.color)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            normalizedItems.fastForEach { (item, _) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(item.color)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = item.title,
                        style = typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = DateUtils.formatElapsedTime(item.timeSeconds.toLong()),
                        style = typography.labelMedium,
                        color = colorScheme.onSurfaceVariant                    )
                }
            }
        }
    }
}