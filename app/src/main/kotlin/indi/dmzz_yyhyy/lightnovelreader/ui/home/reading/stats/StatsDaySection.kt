package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import indi.dmzz_yyhyy.lightnovelreader.data.statistics.DailyDateDetails

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import java.time.LocalDate

@Composable
fun StatsDaySection(
    uiState: StatsOverviewUiState,
    onSelectDate: (LocalDate) -> Unit
) {
    val date = uiState.selectedDate
    val today = uiState.today
    val details = uiState.selectedDateDetails
    val dateText = date.toStatsDate()

    Column(Modifier.padding(horizontal = 18.dp)) {
        StatsSectionTitle(stringResource(R.string.day)) {
            IconButton(
                onClick = { onSelectDate(date.minusDays(1)) },
                enabled = date.isAfter(uiState.startDate)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24px),
                    contentDescription = stringResource(R.string.previous_day)
                )
            }
            IconButton(
                onClick = { onSelectDate(date.plusDays(1)) },
                enabled = date.isBefore(today)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_forward_24px),
                    contentDescription = stringResource(R.string.next_day)
                )
            }
        }
        StatsPanel(Modifier.animateContentSize()) {
            AnimatedText(
                text = dateText,
                style = typography.titleSmall,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            AnimatedContent(
                targetState = details,
                transitionSpec = {
                    (fadeIn(tween(180)) togetherWith fadeOut(tween(100)))
                        .using(SizeTransform(clip = false))
                },
                label = "day"
            ) { target ->
                DayContent(target)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DayContent(
    details: DailyDateDetails?
) {
    if (details == null) {
        Column {
            Text("—", style = typography.displayLarge)
            StatsEmptyHint(stringResource(R.string.no_records_day))
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatSeconds(details.totalSeconds),
                style = typography.displayLarge
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = pluralStringResource(R.plurals.n_reads, details.readCount, details.readCount),
                style = typography.bodySmall,
                color = colorScheme.outline
            )
        }
        val primaryColor = colorScheme.primary
        StatsMiniBars(
            values = details.hourMinutes.map { it.toFloat() },
            labels = StatsHourLabels,
            colors = remember(primaryColor) { List(24) { primaryColor } },
            barHeight = 36.dp,
            fixedMax = 60f
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            details.books.forEach {
                StatsBookBarRow(
                    bookInformationFlow = it.bookInformationFlow,
                    timeText = formatSeconds(it.seconds),
                    fraction = it.seconds.toFloat() / details.totalSeconds
                )
            }
        }
    }
}
