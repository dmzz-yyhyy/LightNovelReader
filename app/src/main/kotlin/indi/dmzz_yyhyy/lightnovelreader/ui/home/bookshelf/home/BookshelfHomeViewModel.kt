package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.MutableBookshelf
import indi.dmzz_yyhyy.lightnovelreader.data.work.ImportDataWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookshelfHomeViewModel @Inject constructor(
    private val bookshelfRepository: BookshelfRepository,
    private val bookRepository: BookRepository,
    private val workManager: WorkManager,
) : ViewModel() {
    private val _uiState = MutableBookshelfHomeUiState()
    val uiState: BookshelfHomeUiState = _uiState

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.coroutineContext.cancelChildren()
            _uiState.bookshelfList = bookshelfRepository.getAllBookshelfIds().map(::getBookshelf)
            if (_uiState.selectedBookshelf.isEmpty())
                _uiState.bookshelfList.getOrNull(0)?.let {
                    changePage(it.id)
                }
        }
    }

    private fun getBookshelf(id: Int): MutableBookshelf {
        val bookshelfFlow = bookshelfRepository.getBookshelfFlow(id)
        val mutableBookshelf = MutableBookshelf().apply { this.id = id }
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfFlow.collect { oldMutableBookshelf ->
                oldMutableBookshelf ?: return@collect
                mutableBookshelf.id = oldMutableBookshelf.id
                mutableBookshelf.name = oldMutableBookshelf.name
                mutableBookshelf.sortType = oldMutableBookshelf.sortType
                mutableBookshelf.autoCache = oldMutableBookshelf.autoCache
                mutableBookshelf.systemUpdateReminder = oldMutableBookshelf.systemUpdateReminder
                mutableBookshelf.allBookIds = oldMutableBookshelf.allBookIds
                mutableBookshelf.pinnedBookIds = oldMutableBookshelf.pinnedBookIds
                mutableBookshelf.updatedBookIds = oldMutableBookshelf.updatedBookIds
                oldMutableBookshelf.allBookIds.forEach {
                    viewModelScope.launch(Dispatchers.IO) {
                        bookRepository.getBookInformationFlow(it, viewModelScope).collect {
                            _uiState.bookInformationMap[it.id] = it
                        }
                    }
                }
                oldMutableBookshelf.updatedBookIds.forEach { bookId ->
                    viewModelScope.launch(Dispatchers.IO) {
                        bookRepository.getBookVolumesFlow(bookId, viewModelScope).collect {
                            if (it.volumes.isNotEmpty())
                                _uiState.bookLastChapterTitleMap[bookId] = "${it.volumes.last().volumeTitle} ${it.volumes.last().chapters.last().title}"
                        }
                    }
                }
            }
        }
        return mutableBookshelf

    }

    fun changePage(bookshelfId: Int) {
        _uiState.selectedBookshelfId = bookshelfId
    }

    fun enableSelectMode() {
        _uiState.selectMode = true
        _uiState.selectedBookIds.clear()
    }

    fun disableSelectMode() {
        _uiState.selectMode = false
        _uiState.selectedBookIds.clear()
    }

    fun changeBookSelectState(bookId: Int) {
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

    fun pinSelectedBooks(bookId: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val pinnedBookIds = _uiState.selectedBookshelf.pinnedBookIds
            val newPinnedBooksIds = _uiState.selectedBookIds
                .filter { pinnedBookIds.contains(it) }
                .let { removeList ->
                    (pinnedBookIds + (if (bookId == null) _uiState.selectedBookIds else listOf(bookId))).toMutableList().apply {
                        removeAll { removeList.contains(it) }
                    }
                }.distinct()
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
            _uiState.selectedBookIds.forEach { bookshelfRepository.deleteBookFromBookshelf(_uiState.selectedBookshelfId, it) }
            _uiState.selectedBookIds.clear()
        }
    }

    @Suppress("UNUSED")
    fun markSelectedBooks(bookshelfIds: List<Int>) {
        _uiState.selectedBookIds.forEach { bookId ->
            _uiState.bookInformationMap[bookId]?.let { bookInformation ->
                bookshelfIds.forEach {
                    bookshelfRepository.addBookIntoBookShelf(it,
                        bookInformation
                    )
                }

            }
        }
        _uiState.selectedBookIds.clear()
        _uiState.selectMode = false
    }

    fun saveAllBookshelfJsonData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.saveBookshelfJsonData(-1, uri)
        }
    }
    fun saveThisBookshelfJsonData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.saveBookshelfJsonData(_uiState.selectedBookshelfId, uri)
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