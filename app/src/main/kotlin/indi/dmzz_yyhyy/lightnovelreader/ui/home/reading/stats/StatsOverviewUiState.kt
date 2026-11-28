package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsSummary
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.DailyDateDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.time.YearMonth

interface StatsOverviewUiState {
    val isLoading: Boolean
    val loadFailed: Boolean
    val today: LocalDate
    val selectedDate: LocalDate
    val thresholds: Int
    val startDate: LocalDate
    val dateLevelMap: Map<LocalDate, Level>
    val dayMinutes: Map<LocalDate, Int>
    val monthMinutes: Map<YearMonth, Int>
    val summary: StatsSummary?
    val selectedDateDetails: DailyDateDetails?
}

class MutableStatisticsOverviewUiState : StatsOverviewUiState {
    override var isLoading: Boolean by mutableStateOf(true)
    override var loadFailed: Boolean by mutableStateOf(false)
    override var today: LocalDate by mutableStateOf(LocalDate.now())
    override var selectedDate: LocalDate by mutableStateOf(LocalDate.now())
    override var thresholds: Int by mutableIntStateOf(0)
    override var startDate: LocalDate by mutableStateOf(LocalDate.now().minusMonths(6))
    override var dateLevelMap: Map<LocalDate, Level> by mutableStateOf(emptyMap())
    override var dayMinutes: Map<LocalDate, Int> by mutableStateOf(emptyMap())
    override var monthMinutes: Map<YearMonth, Int> by mutableStateOf(emptyMap())
    override var summary: StatsSummary? by mutableStateOf(null)
    override var selectedDateDetails: DailyDateDetails? by mutableStateOf(null)
}
