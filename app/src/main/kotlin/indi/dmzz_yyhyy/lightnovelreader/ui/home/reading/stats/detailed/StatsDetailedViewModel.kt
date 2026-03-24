package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.BookRecord
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatsDetailedViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStatsDetailedUiState()
    val uiState: StatsDetailedUiState = _uiState


    fun initialize(targetDate: LocalDate) {
        _uiState.selectedDate = targetDate
        val viewOption = StatsViewOption.fromIndex(_uiState.selectedViewIndex)
        val range = viewOption.rangeFor(targetDate)
        _uiState.targetDateRange = range.start to range.endInclusive

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.isLoading = true
            loadStatistics()
            _uiState.isLoading = false
        }
    }

    fun setSelectedView(index: Int) {
        val viewOption = StatsViewOption.fromIndex(index)
        _uiState.selectedViewIndex = index
        val range = viewOption.rangeFor(_uiState.selectedDate)
        _uiState.targetDateRange = range.start to range.endInclusive

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.isLoading = true
            loadStatistics()
            _uiState.isLoading = false
        }
    }

    private suspend fun loadStatistics() {
        val (startDate, endDate) = _uiState.targetDateRange
        val bookRecordsMap: Map<LocalDate, List<BookRecord>> =
            statsRepository.getBookRecords(startDate, endDate)
        val dailyCountsMap: Map<LocalDate, Count> =
            statsRepository.getDailyCounts(startDate, endDate)
        val firstReadDateMap = statsRepository.getBookFirstReadDateMap()
        val firstFinishedDateMap = statsRepository.getBookFirstFinishedDateMap()
        val favoriteDateMap = statsRepository.getBookFavoriteDateMap()

        val allDates = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()

        val recordsMap: Map<LocalDate, List<BookRecord>> = allDates.associateWith { date ->
            bookRecordsMap[date] ?: emptyList()
        }.toSortedMap()

        _uiState.targetDateRangeCountMap = allDates.associateWith { date ->
            dailyCountsMap[date] ?: Count()
        }.toSortedMap()
        _uiState.targetDateRangeRecordsMap = recordsMap
        _uiState.bookFirstReadDateMap = firstReadDateMap
        _uiState.bookFirstFinishedDateMap = firstFinishedDateMap
        _uiState.bookFavoriteDateMap = favoriteDateMap

        val bookIds = recordsMap.values.flatten().map { it.bookId }.toSet()

        bookIds.forEach { id ->
            _uiState.bookInformationMap[id] = bookRepository.getStateBookInformation(id, viewModelScope)
        }
    }
}
