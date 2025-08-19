package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadProgressRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadType
import indi.dmzz_yyhyy.lightnovelreader.data.work.ExportBookToEPUBWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val downloadProgressRepository: DownloadProgressRepository,
    private val workManager: WorkManager
) : ViewModel() {
    private val _uiState = MutableDetailUiState()
    var exportSettings = ExportSettings()
    var navController: NavController? = null
    val uiState: DetailUiState = _uiState

    fun init(bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookInformationFlow(bookId, viewModelScope).collect {
                if (it.id == -1) return@collect
                _uiState.bookInformation = it
                val bookshelfBookMetadata = bookshelfRepository.getBookshelfBookMetadata(bookId) ?: return@collect
                bookshelfBookMetadata.bookShelfIds.forEach { bookshelfId ->
                    bookshelfRepository.deleteBookFromBookshelfUpdatedBookIds(bookshelfId, bookId)
                }
                bookshelfRepository.updateBookshelfBookMetadataLastUpdateTime(bookId, it.lastUpdated)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookVolumesFlow(bookId, viewModelScope).collect {
                if (it.volumes.isEmpty()) return@collect
                _uiState.bookVolumes = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getUserReadingDataFlow(bookId).collect {
                _uiState.userReadingData = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.isCached = bookRepository.getIsBookCached(bookId)
        }
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.getBookshelfBookMetadataFlow(bookId).collect {
                _uiState.isInBookshelf = it != null
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            downloadProgressRepository.downloadItemIdListFlow.collect { downloadItemList ->
                _uiState.downloadItem = downloadItemList.findLast { it.bookId == _uiState.bookInformation.id && it.type == DownloadType.CACHE }
            }
        }
    }

    fun cacheBook(bookId: Int): Flow<WorkInfo?> {
        val work = bookRepository.cacheBook(bookId)
        val isCachedFlow = bookRepository.isCacheBookWorkFlow(work.id)
        viewModelScope.launch(Dispatchers.IO) {
            isCachedFlow.collect { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    _uiState.isCached = bookRepository.getIsBookCached(bookId)
                }
            }
        }
        return isCachedFlow
    }

    fun onClickTag(tag: String) {
        if (navController == null) return
        bookRepository.progressBookTagClick(tag, navController!!)
    }


    fun exportToEpub(uri: Uri, bookId: Int, title: String): Flow<WorkInfo?> {
        val workRequest = OneTimeWorkRequestBuilder<ExportBookToEPUBWork>()
            .setInputData(
                workDataOf(
                    "bookId" to bookId,
                    "uri" to uri.toString(),
                    "title" to title,
                    "includeImages" to exportSettings.includeImages,
                    "exportType" to exportSettings.exportType.name,
                    "selectedVolume" to exportSettings.selectedVolumeIds.joinToString(",")
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            bookId.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workManager.getWorkInfoByIdFlow(workRequest.id)
    }
}