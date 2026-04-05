package indi.dmzz_yyhyy.lightnovelreader.utils

import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.Direction
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.minutes

fun formTime(
    time: LocalDateTime,
    dateFormat: DateFormat = DateFormat.NUMERIC,
    useRelativeTime: Boolean = true,
): String {
    if (time == LocalDateTime.MIN) return "-"

    val now = LocalDateTime.now()
    val locale = appDisplayLocale

    val absFormatter = dateFormatter(
        dateFormat,
        FormattingSettings.dateShowYear,
        DateOrder.fromString(FormattingSettings.dateOrder)
    ).withLocale(locale)

    if (!useRelativeTime) {
        return time.format(absFormatter)
    }

    val minutesAgo = ChronoUnit.MINUTES.between(time, now)
    val hoursAgo = ChronoUnit.HOURS.between(time, now)

    val rdf = RelativeDateTimeFormatter.getInstance(locale)

    return when {
        hoursAgo >= 72 -> time.format(absFormatter)
        hoursAgo >= 24 -> rdf.format((hoursAgo / 24).toDouble(), Direction.LAST, RelativeUnit.DAYS)
        hoursAgo >= 1 -> rdf.format(hoursAgo.toDouble(), Direction.LAST, RelativeUnit.HOURS)
        minutesAgo >= 1 -> rdf.format(minutesAgo.toDouble(), Direction.LAST, RelativeUnit.MINUTES)
        minutesAgo in 0..1  -> rdf.format(minutesAgo.toDouble(), Direction.LAST, RelativeUnit.MINUTES)
        else -> time.format(absFormatter)
    }
}

fun formTime(time: LocalDateTime): String =
    formTime(time, DateFormat.fromString(FormattingSettings.dateFormat), FormattingSettings.useRelativeTime)

fun formMinutes(totalMinutes: Int): String =
    DurationFormat(appDisplayLocale).format(totalMinutes.minutes, DurationFormat.Unit.MINUTE, DurationFormat.Unit.HOUR)

fun formReadingDuration(totalMinutes: Int): String {
    val df = DurationFormat(appDisplayLocale)

    return if (totalMinutes < 60) {
        df.format(
            totalMinutes.minutes,
            DurationFormat.Unit.MINUTE,
            DurationFormat.Unit.MINUTE
        )
    } else {
        df.format(
            (totalMinutes / 60).minutes,
            DurationFormat.Unit.HOUR,
            DurationFormat.Unit.HOUR
        )
    }
}