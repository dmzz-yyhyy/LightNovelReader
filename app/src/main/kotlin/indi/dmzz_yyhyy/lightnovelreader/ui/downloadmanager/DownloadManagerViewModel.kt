package indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager

<<<<<<< HEAD
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
=======
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
>>>>>>> 5ea137dc (user costume color)
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadItem
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadProgressRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadType
import indi.dmzz_yyhyy.lightnovelreader.data.work.CacheBookWork
import indi.dmzz_yyhyy.lightnovelreader.data.work.ExportBookToEPUBWork
import io.nightfish.lightnovelreader.api.book.BookInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DownloadManagerViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val downloadProgressRepository: DownloadProgressRepository,
    val workManager: WorkManager
) : ViewModel() {
    val downloadItemIdList get() = downloadProgressRepository.downloadItemIdList
<<<<<<< HEAD
    var bookInformationMap: Map<String, BookInformation> by mutableStateOf(emptyMap())
        private set
    private val loadingJobs = mutableMapOf<String, Job>()

    fun loadBookInfo(bookId: String) {
        if (bookInformationMap[bookId]?.isEmpty() == false) return
        if (loadingJobs.containsKey(bookId)) return

        loadingJobs[bookId] = viewModelScope.launch(Dispatchers.IO) {
            try {
                bookRepository.getBookInformationFlow(bookId).collect { bookInformation ->
                    withContext(Dispatchers.Main) {
                        bookInformationMap = bookInformationMap + (bookId to bookInformation)
=======
    private val _bookInformationMap = mutableStateMapOf<String, BookInformation>()
    val bookInformationMap: Map<String, BookInformation> = _bookInformationMap

    init {
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow {
                downloadProgressRepository.downloadItemIdList
            }.collect { downloadItems ->
                downloadItems.forEach { downloadItem ->
                    if (_bookInformationMap.containsKey(downloadItem.bookId))
                         return@forEach
                    viewModelScope.launch(Dispatchers.IO) {
                        bookRepository.getBookInformationFlow(downloadItem.bookId).collect {
                            _bookInformationMap[it.id] = it
                        }
>>>>>>> 5ea137dc (user costume color)
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    loadingJobs.remove(bookId)
                }
            }
        }
    }

    fun onClickCancel(item: DownloadItem) {
        workManager.cancelUniqueWork(
            when (item.type) {
                DownloadType.EPUB_EXPORT -> ExportBookToEPUBWork.ofId(item.bookId)
                DownloadType.CACHE -> CacheBookWork.ofId(item.bookId)
            }
        )
        downloadProgressRepository.removeExportItem(item)
    }
    fun onClickClearCompleted() = downloadProgressRepository.clearCompleted()
}