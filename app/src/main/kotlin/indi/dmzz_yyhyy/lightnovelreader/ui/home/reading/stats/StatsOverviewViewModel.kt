package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import indi.dmzz_yyhyy.lightnovelreader.data.statistics.buildStatsSummary
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.buildDailyDateDetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.DailyDateDetails
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.quickSelect
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class StatsOverviewViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {
    private var _uiState = MutableStatisticsOverviewUiState()
    val uiState: StatsOverviewUiState = _uiState
    private var dailyDetails: Map<LocalDate, DailyDateDetails> = emptyMap()
    private var reloadJob: Job? = null
    private var reloadID = 0

    init {
        reloadData()
    }

    fun reloadData() {
        reloadJob?.cancel()
        val id = ++reloadID
        reloadJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.isLoading = true
            _uiState.loadFailed = false
            try {
                val time = System.currentTimeMillis()
                Log.d("AppReadingStats", "Refresh started")

                val today = LocalDate.now()
                val dailyCounts = statsRepository.getAllDailyCounts()
                    .filterKeys { !it.isAfter(today) }
                val rawRecords = statsRepository.getAllBookRecords()
                    .filter { !it.date.isAfter(today) }
                val bookFlows = rawRecords.distinctBy { it.bookId }.associate { record ->
                    record.bookId to record.bookInformationFlow.take(1).shareIn(
                        scope = viewModelScope,
                        started = SharingStarted.Lazily,
                        replay = 1
                    )
                }
                val bookRecords = rawRecords.map { record ->
                    record.copy(bookInformationFlow = bookFlows.getValue(record.bookId))
                }

                val dayMinutes = dailyCounts
                    .mapValues { (_, count) -> count.getTotalMinutes() }
                    .filterValues { it > 0 }
                val startDate = today.minusMonths(6)

                if (id != reloadID) return@launch

                _uiState.today = today
                _uiState.startDate = dayMinutes.keys.minOrNull()?.coerceAtMost(startDate) ?: startDate
                _uiState.dayMinutes = dayMinutes
                _uiState.monthMinutes = dayMinutes.entries
                    .groupBy { YearMonth.from(it.key) }
                    .mapValues { (_, entries) -> entries.sumOf { it.value } }
                generateLevelMap(dayMinutes)

                dailyDetails = bookRecords.groupBy { it.date }.mapNotNull { (date, records) ->
                    buildDailyDateDetails(date, records, dailyCounts[date])?.let { date to it }
                }.toMap()
                _uiState.summary = buildStatsSummary(dayMinutes, dailyCounts, bookRecords, today, topBookCount = 10)
                selectDate(today)

                val elapsed = (System.currentTimeMillis() - time) / 1000.0
                Log.d("AppReadingStats", "Refresh completed in $elapsed seconds")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AppReadingStats", "Failed to load statistics", e)
                if (id == reloadID) _uiState.loadFailed = true
            } finally {
                if (id == reloadID) _uiState.isLoading = false
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.selectedDate = date
        _uiState.selectedDateDetails = dailyDetails[date]
    }

    private fun generateLevelMap(dayMinutes: Map<LocalDate, Int>) {
        val thresholds = dayMinutes.values.toList().run {
            if (isEmpty()) listOf(0, 0, 0) else listOf(
                quickSelect(this, 0.25),
                quickSelect(this, 0.5),
                quickSelect(this, 0.75)
            )
        }
        _uiState.thresholds = thresholds[2]

        _uiState.dateLevelMap = dayMinutes.mapValues { (_, readingTime) ->
            when {
                thresholds.all { it == 0 } -> Level.Zero
                readingTime >= thresholds[2] -> Level.Four
                readingTime >= thresholds[1] -> Level.Three
                readingTime >= thresholds[0] -> Level.Two
                else -> Level.One
            }
        }
    }
}
