package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import java.time.LocalDate

@Composable
fun StatsOverviewScreen(
    uiState: StatsOverviewUiState,
    onClickBack: () -> Unit,
    onClickRetry: () -> Unit,
    onClickDate: (LocalDate) -> Unit
) {
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.nestedScroll(pinnedScrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                scrollBehavior = pinnedScrollBehavior,
                onClickBack = onClickBack
            )
        }
    ) { paddingValues ->
        Crossfade(
            targetState = uiState.isLoading
        ) { isLoading ->
            if (isLoading) {
                Loading()
            } else if (uiState.loadFailed) {
                EmptyPage(
                    icon = painterResource(R.drawable.error_24px),
                    title = stringResource(R.string.load_failed),
                    description = stringResource(R.string.reading_stats_load_failed)
                ) {
                    TextButton(onClick = onClickRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "calendar") {
                        StatsCalendarSection(
                            uiState = uiState,
                            onSelectedDate = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClickDate(it)
                            }
                        )
                    }
                    item(key = "day") {
                        StatsDaySection(
                            uiState = uiState,
                            onSelectDate = onClickDate
                        )
                    }
                    item(key = "trend") {
                        StatsTrendSection(uiState = uiState)
                    }
                    item(key = "summary") {
                        StatsSummarySection(uiState)
                    }
                    navigationBarSpacer()
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_statistics),
                style = typography.displayLarge,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}