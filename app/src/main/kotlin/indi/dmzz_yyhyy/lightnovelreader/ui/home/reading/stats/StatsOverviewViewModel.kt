package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.DurationFormat
import indi.dmzz_yyhyy.lightnovelreader.utils.quickSelect
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@HiltViewModel
class StatsOverviewViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val bookRepository: BookRepository
) : ViewModel() {
    private var _uiState = MutableStatisticsOverviewUiState()
    val uiState: StatsOverviewUiState = _uiState

    init {
        reloadData()
    }

    private fun reloadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.isLoading = true

            val time = System.currentTimeMillis()
            Log.d("AppReadingStats", "Refresh started")
            _uiState.selectedDate = LocalDate.now()

            val startDate = _uiState.startDate
            val endDate = LocalDate.now()

            val dailyCounts = statsRepository.getDailyCounts(startDate, endDate)
            generateLevelMap(dailyCounts, startDate, endDate)

            val bookRecordsMap = statsRepository.getBookRecords(startDate, endDate)
            _uiState.bookRecordsByDate = bookRecordsMap
            selectDate(_uiState.selectedDate)

            _uiState.isLoading = false
            val elapsed = (System.currentTimeMillis() - time) / 1000.0
            Log.d("AppReadingStats", "Refresh completed in $elapsed seconds")
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.selectedDate = date
        getDateDetails(date)
    }

    private fun getDateDetails(selectedDate: LocalDate) {
        val records = _uiState.bookRecordsByDate[selectedDate] ?: emptyList()
        if (records.isEmpty()) {
            _uiState.selectedDateDetails = null
            return
        }
        var totalSeconds = 0L
        val detailsList = mutableListOf<Pair<Flow<Result<BookInformation, WebRequestError>>, Int>>()

        for (rec in records) {
            val seconds = rec.seconds
            totalSeconds += seconds

            detailsList += bookRepository.getBookInformationFlow(rec.bookId) to seconds
        }

        val sortedDetails = detailsList
            .sortedByDescending { it.second }
            .toMutableList()

        val formattedTotal = DurationFormat()
            .format(totalSeconds.toDuration(DurationUnit.SECONDS), DurationFormat.Unit.MINUTE)

        _uiState.selectedDateDetails = DailyDateDetails(
            formattedTotalTime = formattedTotal,
            timeDetails = sortedDetails
        )
    }

    @Suppress("unused")
    private fun generateLevelMap(
        dailyCounts: Map<LocalDate, Count>,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val dateTotalTimeMap = dailyCounts.mapValues { (_, count) -> count.getTotalMinutes() }
        val localDateList = dateTotalTimeMap.keys.sorted()
        val readingTimes = dateTotalTimeMap.values.toList()
        val thresholds = readingTimes.filter { it > 0 }.run {
            if (isEmpty()) listOf(0, 0, 0) else listOf(
                quickSelect(this, 0.25),
                quickSelect(this, 0.5),
                quickSelect(this, 0.75)
            )
        }
        _uiState.thresholds = thresholds[2]

        val dateLevelMap = localDateList.associateWith { date ->
            val readingTime = dateTotalTimeMap[date] ?: 0
            when {
                thresholds.all { it == 0 } -> Level.Zero
                readingTime >= thresholds[2] -> Level.Four
                readingTime >= thresholds[1] -> Level.Three
                readingTime >= thresholds[0] -> Level.Two
                readingTime > 0 -> Level.One
                else -> Level.Zero
            }
        }
        _uiState.dateLevelMap = dateLevelMap
    }
}
