package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
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
        val bookRecordsMap: Map<LocalDate, List<BookRecordEntity>> =
            statsRepository.getBookRecords(startDate, endDate)
        val statsEntitiesMap: Map<LocalDate, ReadingStatisticsEntity> =
            statsRepository.getReadingStatistics(startDate, endDate)

        val allDates = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()

        val statsMap: Map<LocalDate, ReadingStatisticsEntity> = allDates.associateWith { date ->
            statsEntitiesMap[date] ?: statsRepository.createStatsEntity(date)
        }.toSortedMap()

        val recordsMap: Map<LocalDate, List<BookRecordEntity>> = allDates.associateWith { date ->
            bookRecordsMap[date] ?: emptyList()
        }.toSortedMap()

        _uiState.targetDateRangeStatsMap = statsMap
        _uiState.targetDateRangeRecordsMap = recordsMap

        val bookIds = mutableSetOf<Int>()
        statsMap.values.forEach { entity ->
            bookIds += entity.favoriteBooks
            bookIds += entity.startedBooks
            bookIds += entity.finishedBooks
        }
        recordsMap.values.flatten().forEach { rec ->
            bookIds += rec.bookId
        }

        bookIds.forEach { id ->
            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookInformationFlow(id, viewModelScope).collect { info ->
                    _uiState.bookInformationMap[id] = info
                }
            }
        }
    }
}