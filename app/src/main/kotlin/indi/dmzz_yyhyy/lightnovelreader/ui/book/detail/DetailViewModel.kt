package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import indi.dmzz_yyhyy.lightnovelreader.data.work.cacheBookUniqueWorkName
import indi.dmzz_yyhyy.lightnovelreader.data.work.exportBookUniqueWorkName
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
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
    private var currentCacheState: WorkInfo.State? = null
    private var lastNotifiedCacheState: WorkInfo.State? = null
    var cacheSnackbarState: WorkInfo.State? by mutableStateOf(null)
        private set
    var exportSettings = ExportSettings()
    var navController: NavController? = null
    val uiState: DetailUiState = _uiState

    var isInitialized by mutableStateOf(false)
        private set

    fun init(bookId: String) {
        Log.d("DetailViewModel", "Init bookId = $bookId")
        if (isInitialized) return
        isInitialized = true
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookInformationFlow(bookId, WebDataSourcePriority.High).collect {
                if (it.id.isBlank()) return@collect
                _uiState.bookInformation = it
                _uiState.isLoading = false
                val bookshelfBookMetadata = bookshelfRepository.getBookshelfBookMetadata(bookId) ?: return@collect
                bookshelfBookMetadata.bookShelfIds.forEach { bookshelfId ->
                    bookshelfRepository.deleteBookFromBookshelfUpdatedBookIds(bookshelfId, bookId)
                }
                bookshelfRepository.updateBookshelfBookMetadataLastUpdateTime(bookId, it.lastUpdated)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookVolumesFlow(bookId, WebDataSourcePriority.High).collect {
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
                _uiState.downloadItem = downloadItemList.findLast { it.bookId == bookId && it.type == DownloadType.CACHE }
            }
        }
        startCacheWorkMonitor(bookId)
    }
    fun cacheBook(bookId: String){
        if( currentCacheState == WorkInfo.State.RUNNING ||
            currentCacheState == WorkInfo.State.ENQUEUED ||
            currentCacheState == WorkInfo.State.BLOCKED
            ){
            return
        }
        bookRepository.cacheBook(bookId)
    }

    fun startCacheWorkMonitor(bookId:String){
        viewModelScope.launch(Dispatchers.IO) {
            workManager.getWorkInfosForUniqueWorkFlow(cacheBookUniqueWorkName(bookId)).collect { workInfos ->
                val states = workInfos.map{it.state}.toSet()
                val state = listOf(
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.BLOCKED,
                    WorkInfo.State.SUCCEEDED,
                    WorkInfo.State.CANCELLED,
                    WorkInfo.State.FAILED,
                ).firstOrNull{it in states }
                val previousCacheState = currentCacheState
                currentCacheState = state
                when(state){
                    WorkInfo.State.RUNNING ->{
                        notifyCacheState(state)
                    }
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.BLOCKED -> {
                        notifyCacheState(state)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        _uiState.isCached = bookRepository.getIsBookCached(bookId)
                        if( previousCacheState == WorkInfo.State.RUNNING ||
                            previousCacheState == WorkInfo.State.ENQUEUED ||
                            previousCacheState == WorkInfo.State.BLOCKED
                        ){
                            notifyCacheState(state)
                        }
                    }
                    WorkInfo.State.CANCELLED,
                    WorkInfo.State.FAILED -> {
                        notifyCacheState(state)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun notifyCacheState(state: WorkInfo.State) {
        if(lastNotifiedCacheState == state) return
        lastNotifiedCacheState = state
        cacheSnackbarState = state
    }
    fun onClickTag(tag: String) {
        if (navController == null) return
        bookRepository.progressBookTagClick(tag, navController!!)
    }


    fun exportToEpub(uri: Uri, bookId: String, title: String): Flow<WorkInfo?> {
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
            exportBookUniqueWorkName(bookId),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workManager.getWorkInfoByIdFlow(workRequest.id)
    }
}
