package indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadItem
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadProgressRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadType
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.LightNovelReaderDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.storage.StorageUsageRepository
import indi.dmzz_yyhyy.lightnovelreader.data.storage.StorageUsageSnapshot
import indi.dmzz_yyhyy.lightnovelreader.data.work.CacheBookWork
import indi.dmzz_yyhyy.lightnovelreader.data.work.ExportBookToEPUBWork
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookManagerViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val downloadProgressRepository: DownloadProgressRepository,
    private val database: LightNovelReaderDatabase,
    private val storageUsageRepository: StorageUsageRepository,
    val workManager: WorkManager
) : ViewModel() {
    val downloadItemIdList get() = downloadProgressRepository.downloadItemIdList
    var bookInformationMap: Map<String, BookInformation> by mutableStateOf(emptyMap())
        private set
    private val _clearedItemsFlow = MutableSharedFlow<Int>()
    val clearedItemsFlow = _clearedItemsFlow.asSharedFlow()
    val localBookManagerUiState = MutableLocalBookManagerUiState(
        load = ::loadLocalBooks,
        setSort = ::setLocalBookSort,
        setReverse = ::setLocalBookReverse,
        enterSelection = ::enterLocalBookSelection,
        exitSelection = ::exitLocalBookSelection,
        toggleSelect = ::toggleLocalBookSelect,
        selectAll = ::selectAllLocalBooks,
        deleteSelected = ::deleteSelected,
        clearOrphanedData = ::clearOrphanedData,
        clearBookData = ::clearBookData,
        openStorageOverview = {},
        openBookDetailScreen = {}
    )
    private val loadingJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            storageUsageRepository.getCachedSnapshot()?.let { updateLocalBooks(it, false) }
            refreshLocalBooks()
        }
    }

    fun loadBookInfo(bookId: String) {
        if (bookInformationMap[bookId]?.isEmpty() == false) return
        if (loadingJobs.containsKey(bookId)) return

        loadingJobs[bookId] = viewModelScope.launch(Dispatchers.IO) {
            try {
                bookRepository.getBookInformationFlow(bookId).collect { bookInformation ->
                    withContext(Dispatchers.Main) {
                        bookInformationMap = bookInformationMap + (bookId to bookInformation)
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

    fun loadLocalBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshLocalBooks()
        }
    }

    fun setLocalBookSort(sort: LocalBookSort) {
        localBookManagerUiState.sort = sort
    }

    fun setLocalBookReverse(reverse: Boolean) {
        localBookManagerUiState.sortReverse = reverse
    }

    fun enterLocalBookSelection(id: String? = null) {
        localBookManagerUiState.isSelecting = true
        localBookManagerUiState.selectedIds = buildSet {
            addAll(localBookManagerUiState.selectedIds)
            if (id != null) add(id)
        }
    }

    fun exitLocalBookSelection() {
        localBookManagerUiState.isSelecting = false
        localBookManagerUiState.selectedIds = emptySet()
    }

    fun toggleLocalBookSelect(id: String) {
        localBookManagerUiState.selectedIds = localBookManagerUiState.selectedIds.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        localBookManagerUiState.isSelecting = true
    }

    fun selectAllLocalBooks() {
        localBookManagerUiState.isSelecting = true
        localBookManagerUiState.selectedIds = localBookManagerUiState.bookList.map { it.id }.toSet()
    }

    fun deleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = deleteSelectedLocalBooks()
            if (count > 0) _clearedItemsFlow.emit(count)
        }
    }

    fun clearOrphanedData() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = clearOrphanedDataItems()
            if (count > 0) _clearedItemsFlow.emit(count)
        }
    }

    fun clearBookData(bookId: String, targets: List<LocalBookClearTarget>) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = clearBookDataItems(bookId, targets)
            if (count > 0) _clearedItemsFlow.emit(count)
        }
    }

    suspend fun deleteSelectedLocalBooks(): Int {
        val ids = localBookManagerUiState.selectedIds.toList()
        if (ids.isEmpty()) return 0
        localBookManagerUiState.isDeleting = true
        val chapterIds = database.bookVolumesDao()
            .getVolumeEntitiesByBookIds(ids)
            .flatMap { it.chapterIds }
            .distinct()
        database.runInTransaction {
            if (chapterIds.isNotEmpty()) {
                database.chapterContentDao().deleteByIds(chapterIds)
            }
            database.bookInformationDao().deleteByIds(ids)
        }
        storageUsageRepository.invalidateSnapshot()
        withContext(Dispatchers.Main) {
            localBookManagerUiState.isDeleting = false
            localBookManagerUiState.isSelecting = false
            localBookManagerUiState.selectedIds = emptySet()
        }
        refreshLocalBooks()
        return ids.size
    }

    suspend fun clearOrphanedDataItems(): Int {
        val linkedChapterIds = database.bookVolumesDao()
            .getAllVolumeEntities()
            .flatMap { it.chapterIds }
            .toSet()
        val orphanChapterInfoIds = database.bookVolumesDao()
            .getAllChapterInformationEntities()
            .map { it.id }
            .filterNot(linkedChapterIds::contains)
        val orphanChapterContentIds = database.chapterContentDao()
            .getAllEntities()
            .map { it.id }
            .filterNot(linkedChapterIds::contains)

        database.runInTransaction {
            if (orphanChapterInfoIds.isNotEmpty()) {
                database.bookVolumesDao().deleteChapterInformationByIds(orphanChapterInfoIds)
            }
            if (orphanChapterContentIds.isNotEmpty()) {
                database.chapterContentDao().deleteByIds(orphanChapterContentIds)
            }
        }
        storageUsageRepository.invalidateSnapshot()
        refreshLocalBooks()
        return orphanChapterInfoIds.size + orphanChapterContentIds.size
    }

    suspend fun clearBookDataItems(bookId: String, targets: List<LocalBookClearTarget>): Int {
        if (targets.isEmpty()) return 0

        val volumeEntities = database.bookVolumesDao().getVolumeEntitiesByBookId(bookId)
        val chapterIds = volumeEntities
            .flatMap { it.chapterIds }
            .distinct()
        val targetSet = targets.toSet()
        val hasReadingRecord = database.userReadingDataDao().getEntityWithoutFlow(bookId) != null
        val chapterContentIds = if (LocalBookClearTarget.ChapterContent in targetSet && chapterIds.isNotEmpty()) {
            chapterIds.filter { database.chapterContentDao().getId(it) != null }
        } else {
            emptyList()
        }

        database.runInTransaction {
            if (LocalBookClearTarget.VolumeAndChapterIndex in targetSet) {
                if (chapterIds.isNotEmpty()) {
                    database.bookVolumesDao().deleteChapterInformationByIds(chapterIds)
                }
                database.bookVolumesDao().deleteByBookIds(listOf(bookId))
            }
            if (chapterContentIds.isNotEmpty()) {
                database.chapterContentDao().deleteByIds(chapterContentIds)
            }
            if (LocalBookClearTarget.ReadingRecord in targetSet && hasReadingRecord) {
                database.userReadingDataDao().deleteByIds(listOf(bookId))
            }
        }

        storageUsageRepository.invalidateSnapshot()
        refreshLocalBooks()

        var clearedCount = 0
        if (LocalBookClearTarget.VolumeAndChapterIndex in targetSet) {
            clearedCount += volumeEntities.size + chapterIds.size
        }
        clearedCount += chapterContentIds.size
        if (LocalBookClearTarget.ReadingRecord in targetSet && hasReadingRecord) {
            clearedCount += 1
        }
        return clearedCount
    }

    private suspend fun refreshLocalBooks() {
        withContext(Dispatchers.Main) {
            localBookManagerUiState.isLoading = true
        }
        updateLocalBooks(storageUsageRepository.refreshSnapshot(), loading = false)
    }

    private suspend fun updateLocalBooks(snapshot: StorageUsageSnapshot, loading: Boolean) {
        val bookInfos = database.bookInformationDao().getAllEntities()
        val volumes = database.bookVolumesDao().getAllVolumeEntities()
        val readingData = database.userReadingDataDao().getAll()
        val bookReadingBytesMap = database.storageStatsDao()
            .getUserReadingBytes()
            .associate { it.id to it.bytes }

        val bookInfoMap = bookInfos.associate { entity ->
            entity.id to MutableBookInformation(
                entity.id,
                entity.title,
                entity.subtitle,
                entity.coverUri,
                entity.author,
                entity.description,
                entity.tags,
                entity.publishingHouse,
                entity.wordCount,
                entity.lastUpdated,
                entity.isComplete
            )
        }
        val bookChapterCountMap = volumes.groupBy { it.bookId }
            .mapValues { (_, list) -> list.sumOf { it.chapterIds.size } }
        val bookVolumeCountMap = volumes.groupBy { it.bookId }
            .mapValues { (_, list) -> list.size }
        val bookLastReadTimeMap = readingData.associate { it.id to it.lastReadTime }

        val bookList = snapshot.books.map { usage ->
            val bookInformation = bookInfoMap[usage.bookId] ?: BookInformation.empty(usage.bookId)
            val readingRecordBytes = bookReadingBytesMap[usage.bookId] ?: 0L
            LocalBookItem(
                id = usage.bookId,
                bookInformation = bookInformation,
                size = usage.totalBytes + readingRecordBytes,
                chapterCount = bookChapterCountMap[usage.bookId] ?: 0,
                volumeCount = bookVolumeCountMap[usage.bookId] ?: 0,
                lastReadTime = bookLastReadTimeMap[usage.bookId],
                bookInformationBytes = usage.bookInformationBytes,
                volumeBytes = usage.volumeBytes,
                chapterInformationBytes = usage.chapterInformationBytes,
                chapterContentBytes = usage.chapterContentBytes,
                readingRecordBytes = readingRecordBytes
            )
        }.filter { it.size > 0L && !it.bookInformation.isEmpty() }
        val retainedSelectedIds = localBookManagerUiState.selectedIds.intersect(bookList.map { it.id }.toSet())

        withContext(Dispatchers.Main) {
            localBookManagerUiState.isLoading = loading
            localBookManagerUiState.bookList = bookList
            localBookManagerUiState.selectedIds = retainedSelectedIds
            localBookManagerUiState.isSelecting =
                localBookManagerUiState.isSelecting && retainedSelectedIds.isNotEmpty()
        }
    }
}
