package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingHomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val readingBooksUserData = userDataRepository.intListUserData(UserDataPath.ReadingBooks.path)
    var recentReadingBookIds: List<Int> by mutableStateOf(listOf())
        private set
    private val _recentReadingBookInformationMap: SnapshotStateMap<Int, BookInformation> = mutableStateMapOf()
    private val _recentReadingUserReadingDataMap: SnapshotStateMap<Int, UserReadingData> = mutableStateMapOf()
    val recentReadingBookInformationMap: Map<Int, BookInformation> = _recentReadingBookInformationMap
    val recentReadingUserReadingDataMap: Map<Int, UserReadingData> = _recentReadingUserReadingDataMap

    fun updateReadingBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            recentReadingBookIds = readingBooksUserData.getOrDefault(emptyList()).reversed()
                .mapNotNull { if (it == -1) null else it  }
            recentReadingBookIds.forEach {
                _recentReadingBookInformationMap[it] = bookRepository.getStateBookInformation(it, viewModelScope)
            }
            recentReadingBookIds.forEach {
                _recentReadingUserReadingDataMap[it] = bookRepository.getStateUserReadingData(it, viewModelScope)
            }
        }
    }
}
