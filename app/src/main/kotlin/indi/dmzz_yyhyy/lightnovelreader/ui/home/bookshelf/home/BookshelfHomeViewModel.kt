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
import com.github.michaelbull.result.onOk
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.work.ImportDataWork
import indi.dmzz_yyhyy.lightnovelreader.data.work.SaveBookshelfWork
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.toBookshelfUiState
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
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

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val bookshelfIds = bookshelfRepository.getAllBookshelfIds()
            val savedOrder = bookshelfOrderUserData.getOrDefault(emptyList())
            val orderedIds = savedOrder.filter(bookshelfIds::contains) + bookshelfIds.filterNot(savedOrder::contains)
            _uiState.bookshelfList = orderedIds.mapNotNull {
                getBookshelf(it)
            }
            if (_uiState.selectedBookshelf == null)
                _uiState.bookshelfList.getOrNull(0)?.let {
                    changePage(it.id)
                }
        }
    }

    private suspend fun getBookshelf(id: Int) = bookshelfRepository.getBookshelf(id)?.toBookshelfUiState(bookRepository, bookshelfRepository)

    fun changePage(bookshelfId: Int) {
        _uiState.selectedBookshelfId = bookshelfId
    }

    fun changeSortType(sortType: BookshelfSortType) {
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.updateBookshelf(_uiState.selectedBookshelfId) {
                it.copy(
                    sortType = sortType
                )
            }
        }
    }

    fun changeSortReversed(sortReversed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.updateBookshelf(_uiState.selectedBookshelfId) {
                it.copy(
                    sortReversed = sortReversed
                )
            }
        }
    }

    fun enableReorderMode(bookshelfId: Int = _uiState.selectedBookshelfId) {
        val bookshelf = _uiState.bookshelfList.firstOrNull { it.id == bookshelfId } ?: return
        _uiState.selectedBookshelfId = bookshelfId
        if (bookshelf.sortType != BookshelfSortType.Default) return
        _uiState.reorderBookIds.clear()
        _uiState.reorderBookIds.addAll(bookshelf.allBookFlows)
        _uiState.reorderMode = true
    }

    fun disableReorderMode() {
        if (_uiState.reorderMode) {
            val reorderedIds = _uiState.reorderBookIds.toList()
            viewModelScope.launch(Dispatchers.IO) {
                bookshelfRepository.updateBookshelf(_uiState.selectedBookshelfId) { oldBookshelf ->
                    oldBookshelf.copy(
                        allBookIds = reorderedIds.map { it.first }
                    )
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
        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.reorderBookshelfMode) {
                _uiState.bookshelfList = reorderedIds.mapNotNull {
                    getBookshelf(it)
                }
                _uiState.reorderBookshelfIds.clear()
                _uiState.reorderBookshelfIds.addAll(reorderedIds)
                bookshelfOrderUserData.set(reorderedIds)
            }
            _uiState.reorderBookshelfMode = false
            _uiState.reorderBookshelfIds.clear()
        }
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
        val allBookIds = _uiState.selectedBookshelf?.allBookFlows?.map {
            it.first
        } ?: return
        if (_uiState.selectedBookIds.size == allBookIds.size) {
            _uiState.selectedBookIds.clear()
            return
        }
        _uiState.selectedBookIds.clear()
        _uiState.selectedBookIds.addAll(allBookIds)
    }

    fun pinSelectedBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            val pinnedBookIds = _uiState.selectedBookshelf?.pinnedBookFlows?.map {
                it.first
            } ?: return@launch
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
                it.copy(
                    pinnedBookIds = newPinnedBooksIds
                )
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
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.selectedBookIds.forEach { bookId ->
                bookshelfIds.forEach { bookshelfId ->
                    bookRepository.getBookInformationFlow(bookId).last().onOk {
                        bookshelfRepository.addBookIntoBookShelf(
                            bookshelfId,
                            it
                        )
                    }
                }
            }
            _uiState.selectedBookIds.clear()
            _uiState.selectMode = false
        }
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
