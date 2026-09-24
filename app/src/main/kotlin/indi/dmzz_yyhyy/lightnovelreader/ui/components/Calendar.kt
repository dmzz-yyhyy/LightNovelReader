/****************************************************************************
 * Copyright (c) 2019 Kizito Nwose                                           *
 *                                                                           *
 * Distributed under MIT license.                                            *
 *                                                                           *
 * Permission is hereby granted, free of charge, to any person obtaining a   *
 * copy of this software and associated documentation files (the "Software"),*
 * to deal in the Software without restriction, including without limitation *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,  *
 * and/or sell copies of the Software, and to permit persons to whom the     *
 * Software is furnished to do so, subject to the following conditions:      *
 *                                                                           *
 * The above copyright notice and this permission notice shall be included   *
 * in all copies or substantial portions of the Software.                    *
 *                                                                           *
 *****************************************************************************/

package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.HeatMapCalState
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.HeatMapCalendarImpl
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarDay
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarMonth
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarWeek
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.rememberHeatMapCalendarState
import java.time.DayOfWeek

@Composable
fun HeatMapCalendar(
    modifier: Modifier = Modifier,
    state: HeatMapCalState = rememberHeatMapCalendarState(),
    hideWeek: Boolean = false,
    allowScroll: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    dayContent: @Composable ColumnScope.(day: CalendarDay, week: CalendarWeek) -> Unit,
    weekHeader: (@Composable ColumnScope.(DayOfWeek) -> Unit)? = null,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
): Unit = HeatMapCalendarImpl(
    modifier = modifier,
    state = state,
    hideWeek = hideWeek,
    allowScroll = allowScroll,
    dayContent = dayContent,
    weekHeader = weekHeader,
    monthHeader = monthHeader,
    contentPadding = contentPadding,
)
