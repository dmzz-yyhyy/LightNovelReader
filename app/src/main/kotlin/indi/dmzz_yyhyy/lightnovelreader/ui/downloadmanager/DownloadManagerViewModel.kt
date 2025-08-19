package indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadItem
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadManagerViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val downloadProgressRepository: DownloadProgressRepository
) : ViewModel() {
    val downloadItemIdList = downloadProgressRepository.downloadItemIdList
    private val _bookInformationMap = mutableStateMapOf<Int, BookInformation>()
    val bookInformationMap: Map<Int, BookInformation> = _bookInformationMap

    init {
        viewModelScope.launch(Dispatchers.IO) {
            downloadProgressRepository.downloadItemIdListFlow.collect { downloadItems ->
                downloadItems.forEach { downloadItem ->
                    if (_bookInformationMap.containsKey(downloadItem.bookId))
                         return@forEach
                    viewModelScope.launch(Dispatchers.IO) {
                        bookRepository.getBookInformationFlow(downloadItem.bookId, viewModelScope).collect {
                            _bookInformationMap[it.id] = it
                        }
                    }
                 }
            }
        }
    }

    fun onClickCancel(item: DownloadItem) = downloadProgressRepository.removeExportItem(item)
    fun onClickClearCompleted() = downloadProgressRepository.clearCompleted()
}