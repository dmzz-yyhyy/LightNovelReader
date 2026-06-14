package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.work.ImportDataWork
import indi.dmzz_yyhyy.lightnovelreader.data.work.SaveBookshelfWork
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfBookMetadata
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import io.nightfish.lightnovelreader.api.bookshelf.MutableBookshelf
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookshelfHomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val bookshelfRepository: BookshelfRepository,
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository,
    private val workManager: WorkManager,
) : ViewModel() {
    private val _uiState = MutableBookshelfHomeUiState(
        changePage = ::changePage,
        changeSortType = ::changeSortType,
        changeSortReversed = ::changeSortReversed,
        changeBookSelectState = ::changeBookSelectState,
        enableReorderMode = ::enableReorderMode,
        disableReorderMode = ::disableReorderMode,
        moveBook = ::moveBook,
        enableBookshelfReorderMode = ::enableBookshelfReorderMode,
        disableBookshelfReorderMode = ::disableBookshelfReorderMode,
        moveBookshelf = ::moveBookshelf,
        onEnableSelectMode = ::enableSelectMode,
        onDisableSelectMode = ::disableSelectMode,
        onSelectAll = ::selectAllBooks,
        onPin = ::pinSelectedBooks,
        onRemove = ::removeSelectedBooks,
        saveAllBookshelfJsonData = ::saveAllBookshelf,
        saveBookshelfJsonData = ::saveThisBookshelf,
        importBookshelf = ::importBookshelf,
        clearToast = ::clearToast,
    )
    val uiState: BookshelfHomeUiState = _uiState
    private val bookshelfOrderUserData = userDataRepository.intListUserData(UserDataPath.BookshelfOrder.path)

    private val bookInfoStateFlows = mutableMapOf<String, StateFlow<BookInformation>>()
    private val bookVolumesStateFlows = mutableMapOf<String, StateFlow<BookVolumes>>()
    private val bookMetadataStateFlows = mutableMapOf<String, StateFlow<BookshelfBookMetadata?>>()
    private val bookshelfStateMap = mutableMapOf<Int, MutableBookshelf>()
    private val bookLastChapterJobs = mutableMapOf<String, Job>()

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val bookshelfIds = bookshelfRepository.getAllBookshelfIds()
            val savedOrder = bookshelfOrderUserData.getOrDefault(emptyList())
            val orderedIds = savedOrder.filter(bookshelfIds::contains) + bookshelfIds.filterNot(savedOrder::contains)
            _uiState.bookshelfList = orderedIds.map(::getBookshelf)
            if (_uiState.selectedBookshelf.isEmpty())
                _uiState.bookshelfList.getOrNull(0)?.let {
                    changePage(it.id)
                }
        }
    }

    fun getBookInfoStateFlow(id: String): StateFlow<BookInformation> {
        return bookInfoStateFlows.getOrPut(id) {
            bookRepository.getBookInformationFlow(id)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    initialValue = BookInformation.empty(id)
                )
        }
    }

    fun getBookVolumesStateFlow(id: String): StateFlow<BookVolumes> {
        return bookVolumesStateFlows.getOrPut(id) {
            bookRepository.getBookVolumesFlow(id)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    initialValue = BookVolumes.empty(id)
                )
        }
    }

    fun getBookshelfBookMetadataStateFlow(id: String): StateFlow<BookshelfBookMetadata?> {
        return bookMetadataStateFlows.getOrPut(id) {
            bookshelfRepository.getBookshelfBookMetadataFlow(id)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    initialValue = null
                )
        }
    }

    private fun getBookshelf(id: Int): MutableBookshelf {
        return bookshelfStateMap.getOrPut(id) {
            val bookshelfFlow = bookshelfRepository.getBookshelfFlow(id)
            MutableBookshelf().apply {
                this.id = id
                viewModelScope.launch(Dispatchers.IO) {
                    bookshelfFlow.collect { oldMutableBookshelf ->
                        oldMutableBookshelf ?: return@collect
                        this@apply.id = oldMutableBookshelf.id
                        this@apply.name = oldMutableBookshelf.name
                        this@apply.sortType = oldMutableBookshelf.sortType
                        this@apply.sortReversed = oldMutableBookshelf.sortReversed
                        this@apply.autoCache = oldMutableBookshelf.autoCache
                        this@apply.systemUpdateReminder = oldMutableBookshelf.systemUpdateReminder
                        this@apply.allBookIds = oldMutableBookshelf.allBookIds
                        this@apply.pinnedBookIds = oldMutableBookshelf.pinnedBookIds
                        this@apply.updatedBookIds = oldMutableBookshelf.updatedBookIds

                        val updatedBookIdSet = oldMutableBookshelf.updatedBookIds.toHashSet()
                        bookLastChapterJobs.keys
                            .filterNot(updatedBookIdSet::contains)
                            .forEach { bookId ->
                                bookLastChapterJobs.remove(bookId)?.cancel()
                                _uiState.bookLastChapterTitleMap.remove(bookId)
                            }
                        oldMutableBookshelf.updatedBookIds.forEach { bookId ->
                            if (bookLastChapterJobs.containsKey(bookId)) return@forEach
                            bookLastChapterJobs[bookId] = viewModelScope.launch(Dispatchers.IO) {
                                bookRepository.getBookVolumesFlow(bookId).collect {
                                    if (it.volumes.isNotEmpty()) {
                                        viewModelScope.launch(Dispatchers.Main) {
                                            _uiState.bookLastChapterTitleMap[bookId] =
                                                "${it.volumes.last().volumeTitle} ${it.volumes.last().chapters.last().title}"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun changePage(bookshelfId: Int) {
        _uiState.selectedBookshelfId = bookshelfId
    }

    fun changeSortType(sortType: BookshelfSortType) {
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.updateBookshelf(_uiState.selectedBookshelfId) {
                it.apply {
                    this.sortType = sortType
                }
            }
        }
    }

    fun changeSortReversed(sortReversed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.updateBookshelf(_uiState.selectedBookshelfId) {
                it.apply {
                    this.sortReversed = sortReversed
                }
            }
        }
    }

    fun enableReorderMode(bookshelfId: Int = _uiState.selectedBookshelfId) {
        val bookshelf = _uiState.bookshelfList.firstOrNull { it.id == bookshelfId } ?: return
        _uiState.selectedBookshelfId = bookshelfId
        if (bookshelf.sortType != BookshelfSortType.Default) return
        _uiState.reorderBookIds.clear()
        _uiState.reorderBookIds.addAll(bookshelf.allBookIds)
        _uiState.reorderMode = true
    }

    fun disableReorderMode() {
        if (_uiState.reorderMode) {
            val reorderedIds = _uiState.reorderBookIds.toList()
            viewModelScope.launch(Dispatchers.IO) {
                bookshelfRepository.updateBookshelf(_uiState.selectedBookshelfId) { oldBookshelf ->
                    oldBookshelf.apply {
                        this.allBookIds = reorderedIds
                    }
                }
            }
        }
        _uiState.reorderMode = false
        _uiState.reorderBookIds.clear()
    }

    fun moveBook(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex !in _uiState.reorderBookIds.indices || toIndex !in _uiState.reorderBookIds.indices) return
        val item = _uiState.reorderBookIds.removeAt(fromIndex)
        _uiState.reorderBookIds.add(toIndex, item)
    }

    fun enableBookshelfReorderMode() {
        _uiState.reorderBookshelfIds.clear()
        _uiState.reorderBookshelfIds.addAll(_uiState.bookshelfList.map { it.id })
        _uiState.reorderBookshelfMode = true
    }

    fun disableBookshelfReorderMode() {
        disableBookshelfReorderMode(_uiState.reorderBookshelfIds.toList())
    }

    fun disableBookshelfReorderMode(reorderedIds: List<Int>) {
        if (_uiState.reorderBookshelfMode) {
            _uiState.bookshelfList = reorderedIds.map(::getBookshelf)
            _uiState.reorderBookshelfIds.clear()
            _uiState.reorderBookshelfIds.addAll(reorderedIds)
            viewModelScope.launch(Dispatchers.IO) {
                bookshelfOrderUserData.set(reorderedIds)
            }
        }
        _uiState.reorderBookshelfMode = false
        _uiState.reorderBookshelfIds.clear()
    }

    fun moveBookshelf(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex !in _uiState.reorderBookshelfIds.indices || toIndex !in _uiState.reorderBookshelfIds.indices) return
        val item = _uiState.reorderBookshelfIds.removeAt(fromIndex)
        _uiState.reorderBookshelfIds.add(toIndex, item)
    }

    fun enableSelectMode() {
        _uiState.selectMode = true
        _uiState.selectedBookIds.clear()
    }

    fun disableSelectMode() {
        _uiState.selectMode = false
        _uiState.selectedBookIds.clear()
    }

    fun changeBookSelectState(bookId: String) {
        if (_uiState.selectedBookIds.contains(bookId))
            _uiState.selectedBookIds.remove(bookId)
        else _uiState.selectedBookIds.add(bookId)
        if (_uiState.selectedBookIds.isEmpty()) disableSelectMode()
    }

    fun selectAllBooks() {
        if (_uiState.selectedBookIds.size == _uiState.selectedBookshelf.allBookIds.size) {
            _uiState.selectedBookIds.clear()
            return
        }
        _uiState.selectedBookIds.clear()
        _uiState.selectedBookIds.addAll(_uiState.selectedBookshelf.allBookIds)
    }

    fun pinSelectedBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            val pinnedBookIds = _uiState.selectedBookshelf.pinnedBookIds
            val newPinnedBooksIds = _uiState.selectedBookIds
                .filter { pinnedBookIds.contains(it) }
                .toMutableList()
                .let { removeList ->
                    (pinnedBookIds + (_uiState.selectedBookIds))
                        .toMutableList()
                        .apply {
                            removeAll { removeList.contains(it) }
                        }
                }
                .distinct()

            bookshelfRepository.updateBookshelf(_uiState.selectedBookshelfId) {
                it.apply {
                    this.pinnedBookIds = newPinnedBooksIds
                }
            }
            disableSelectMode()
        }
    }


    fun removeSelectedBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.selectedBookIds.forEach {
                bookshelfRepository.deleteBookFromBookshelf(
                    _uiState.selectedBookshelfId,
                    it
                )
            }
            _uiState.selectedBookIds.clear()
        }
    }

    @Suppress("UNUSED")
    fun markSelectedBooks(bookshelfIds: List<Int>) {
        _uiState.selectedBookIds.forEach { bookId ->
            _uiState.bookInformationMap[bookId]?.let { bookInformation ->
                bookshelfIds.forEach {
                    bookshelfRepository.addBookIntoBookShelf(
                        it,
                        bookInformation
                    )
                }

            }
        }
        _uiState.selectedBookIds.clear()
        _uiState.selectMode = false
    }

    fun saveAllBookshelf(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val workRequest = OneTimeWorkRequestBuilder<SaveBookshelfWork>()
                .setInputData(
                    workDataOf(
                        "uri" to uri.toString(),
                        "bookshelfId" to -1
                    )
                )
                .build()
            workManager.enqueueUniqueWork(
                uri.toString(),
                ExistingWorkPolicy.KEEP,
                workRequest
            )
            CoroutineScope(Dispatchers.Main).launch {
                workManager.getWorkInfoByIdFlow(workRequest.id).collect {
                    it ?: return@collect
                    when(it.state) {
                        WorkInfo.State.SUCCEEDED -> Toast.makeText(context, "导出成功", Toast.LENGTH_LONG).show()
                        WorkInfo.State.FAILED -> Toast.makeText(context, "导出失败", Toast.LENGTH_LONG).show()
                        else -> return@collect
                    }
                }
            }
        }
    }

    fun saveThisBookshelf(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val workRequest = OneTimeWorkRequestBuilder<SaveBookshelfWork>()
                .setInputData(
                    workDataOf(
                        "uri" to uri.toString(),
                        "bookshelfId" to uiState.selectedBookshelfId
                    )
                )
                .build()
            workManager.enqueueUniqueWork(
                uri.toString(),
                ExistingWorkPolicy.KEEP,
                workRequest
            )
            CoroutineScope(Dispatchers.Main).launch {
                workManager.getWorkInfoByIdFlow(workRequest.id).collect {
                    it ?: return@collect
                    when(it.state) {
                        WorkInfo.State.SUCCEEDED -> Toast.makeText(context, "导出成功", Toast.LENGTH_LONG).show()
                        WorkInfo.State.FAILED -> Toast.makeText(context, "导出失败", Toast.LENGTH_LONG).show()
                        else -> return@collect
                    }
                }
            }
        }
    }

    fun importBookshelf(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val workRequest = OneTimeWorkRequestBuilder<ImportDataWork>()
                .setInputData(
                    workDataOf(
                        "uri" to uri.toString(),
                    )
                )
                .build()
            workManager.enqueueUniqueWork(
                uri.toString(),
                ExistingWorkPolicy.KEEP,
                workRequest
            )
            workManager.getWorkInfoByIdFlow(workRequest.id).collect {
                it ?: return@collect
                when(it.state) {
                    WorkInfo.State.ENQUEUED -> return@collect
                    WorkInfo.State.RUNNING -> return@collect
                    WorkInfo.State.SUCCEEDED -> load()
                    WorkInfo.State.FAILED -> _uiState.toast = "文件损坏或格式错误，请检查后重试。"
                    WorkInfo.State.BLOCKED -> return@collect
                    WorkInfo.State.CANCELLED -> return@collect
                }
            }
        }
    }

    fun clearToast() {
        _uiState.toast = ""
    }
}
