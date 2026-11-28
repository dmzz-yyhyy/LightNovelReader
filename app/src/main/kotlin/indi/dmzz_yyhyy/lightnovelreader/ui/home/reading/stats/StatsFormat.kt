package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.utils.DateFields
import indi.dmzz_yyhyy.lightnovelreader.utils.DurationFormat
import indi.dmzz_yyhyy.lightnovelreader.utils.formatDate
import java.time.LocalDate
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun formatMinutes(minutes: Int): String =
    DurationFormat().format(
        minutes.toDuration(DurationUnit.MINUTES),
        smallestUnit = DurationFormat.Unit.MINUTE,
        largestUnit = DurationFormat.Unit.HOUR
    )

fun formatSeconds(seconds: Int): String = formatMinutes(seconds / 60)

fun LocalDate.toStatsDate(): String = formatDate(this)

fun LocalDate.toStatsDay(): String = formatDate(this, DateFields.MONTH_DAY)

fun LocalDate.toStatsMonth(): String = formatDate(this, DateFields.YEAR_MONTH)

fun LocalDate.toStatsYear(): String = formatDate(this, DateFields.YEAR)

@Composable
fun axisLabel(minutes: Int): String {
    val platformLocale = LocalLocale.current.platformLocale
    return when {
        minutes < 60 -> stringResource(R.string.duration_short_minutes, minutes)
        minutes % 60 == 0 -> stringResource(R.string.duration_short_hours, "${minutes / 60}")
        else -> stringResource(
            R.string.duration_short_hours,
            String.format(platformLocale, "%.1f", minutes / 60f)
        )
    }
}