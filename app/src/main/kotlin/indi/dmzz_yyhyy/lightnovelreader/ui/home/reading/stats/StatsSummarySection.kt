package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsSummary
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.BookReadingTotal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import java.time.DayOfWeek
import java.time.format.TextStyle
import androidx.compose.ui.platform.LocalLocale

private val CoverWidth = 63.dp
private val CoverHeight = 90.dp

@Composable
fun StatsSummarySection(uiState: StatsOverviewUiState) {
    val summary = uiState.summary

    Column(Modifier.padding(horizontal = 18.dp)) {
        StatsSectionTitle(stringResource(R.string.overview))
        if (summary == null) {
            StatsPanel { StatsEmptyHint(stringResource(R.string.no_records_yet)) }
            Spacer(Modifier.height(12.dp))
            return@Column
        }
        SummaryHero(summary)
        Spacer(Modifier.height(8.dp))
        SummaryLedger(summary)
        StatsSectionTitle(stringResource(R.string.distribution))
        SummaryDistribution(summary)
        if (summary.topBooks.isNotEmpty()) {
            StatsSectionTitle(stringResource(R.string.most_read))
            SummaryTopBooks(summary)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SummaryHero(summary: StatsSummary) {
    val hours = summary.totalMinutes / 60
    val minutes = summary.totalMinutes % 60

    StatsPanel {
        Text(
            text = stringResource(R.string.total_reading_time),
            style = typography.titleSmall,
            color = colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row {
            if (hours > 0) {
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = hours.toString(),
                    style = typography.displayLarge
                )
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = stringResource(R.string.unit_hours),
                    style = typography.titleMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Text(
                modifier = Modifier.alignByBaseline(),
                text = minutes.toString(),
                style = typography.displayLarge)
            Text(
                modifier = Modifier.alignByBaseline(),
                text = stringResource(R.string.unit_minutes),
                style = typography.titleMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.since_date, summary.firstDate.toStatsDate()),
            style = typography.bodyMedium,
            color = colorScheme.outline
        )
    }
}

@Composable
private fun SummaryLedger(
    summary: StatsSummary
) {
    val rows = listOf(
        LedgerRow(
            stringResource(R.string.reading_sessions),
            pluralStringResource(R.plurals.n_reads, summary.totalReads, summary.totalReads)
        ),
        LedgerRow(
            stringResource(R.string.average),
            formatMinutes(summary.averageMinutesPerActiveDay)
        ),
        LedgerRow(
            stringResource(R.string.average_all_days),
            formatMinutes(summary.averageMinutesPerDay)
        ),
        LedgerRow(
            stringResource(R.string.longest_day),
            formatMinutes(summary.longestDayMinutes),
            summary.longestDayDate.toStatsDate()
        ),
        LedgerRow(
            stringResource(R.string.current_streak),
            pluralStringResource(R.plurals.n_days, summary.currentStreak, summary.currentStreak)
        ),
        LedgerRow(
            stringResource(R.string.longest_streak),
            pluralStringResource(R.plurals.n_days, summary.longestStreak, summary.longestStreak),
            "${summary.longestStreakStart.toStatsDate()} – " +
                    summary.longestStreakEnd.toStatsDate()
        ),
        LedgerRow(
            stringResource(R.string.books_read),
            pluralStringResource(R.plurals.n_books, summary.startedCount, summary.startedCount)
        )
    )

    StatsPanel {
        rows.forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = row.label,
                    style = typography.titleSmall,
                    fontWeight = FontWeight.W600
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = row.value,
                        style = typography.bodyMedium
                    )
                    row.note?.let {
                        Text(
                            text = it,
                            style = typography.bodySmall,
                            color = colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

private data class LedgerRow(
    val label: String,
    val value: String,
    val note: String? = null
)

@Composable
private fun SummaryDistribution(
    summary: StatsSummary
) {
    val locale = LocalLocale.current.platformLocale
    val primaryColor = colorScheme.primary
    val tertiaryColor = colorScheme.tertiary

    StatsPanel {
        Text(
            text = stringResource(R.string.by_hour),
            style = typography.titleSmall,
            color = colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        StatsMiniBars(
            values = summary.hourTotals.map { it.toFloat() },
            labels = StatsHourLabels,
            colors = remember(primaryColor) { List(24) { primaryColor } },
            barHeight = 64.dp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.by_weekday),
            style = typography.titleSmall,
            color = colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        StatsMiniBars(
            values = summary.weekdayAverages,
            labels = DayOfWeek.entries.map { it.getDisplayName(TextStyle.SHORT, locale) },
            colors = remember(primaryColor, tertiaryColor) {
                List(7) { index -> if (index >= 5) tertiaryColor else primaryColor }
            },
            barHeight = 64.dp
        )
    }
}

@Composable
private fun SummaryTopBooks(summary: StatsSummary) {
    val topBooks = summary.topBooks
    StatsPanel {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp, start = 8.dp)
            .height(CoverHeight)
        ) {
            val step = if (topBooks.size > 1) (maxWidth - CoverWidth).coerceAtLeast(0.dp) / (topBooks.size - 1)
            else 0.dp
            topBooks.forEachIndexed { index, book ->
                Box(modifier = Modifier.zIndex((topBooks.size - index).toFloat())
                    .offset(x = step * index)
                    .graphicsLayer {
                        rotationZ = 7f
                        shape = RoundedCornerShape(6.dp)
                        clip = true
                        shadowElevation = 1.dp.toPx()
                    }
                    .size(CoverWidth, CoverHeight)
                ) {
                    BookCover(book)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            topBooks.forEachIndexed { index, book ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier.width(22.dp),
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.W600,
                        color = colorScheme.outline
                    )
                    StatsBookBarRow(
                        bookInformationFlow = book.bookInformationFlow,
                        timeText = formatMinutes((book.seconds / 60).toInt()),
                        fraction = book.seconds.toFloat() / topBooks.first().seconds
                    )
                }
            }
        }
    }
}

@Composable
private fun BookCover(book: BookReadingTotal) {
    val result by book.bookInformationFlow.collectAsStateWithLifecycle(null)
    result?.onOk {
        Cover(CoverWidth, CoverHeight, it.coverUri, it.title, 6.dp)
    }?.onErr {
        Box(
            Modifier
                .fillMaxSize()
                .background(colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.help_center_24px),
                contentDescription = null,
                tint = colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
        }
    } ?: Box(modifier = Modifier.fillMaxSize()
        .background(colorScheme.surfaceContainerHighest)
    )
}
