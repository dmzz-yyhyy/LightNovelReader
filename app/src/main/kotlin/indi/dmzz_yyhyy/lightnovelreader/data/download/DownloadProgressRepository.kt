package indi.dmzz_yyhyy.lightnovelreader.data.download

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.CompletedDownload
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadProgressRepository @Inject constructor(
    userDataRepository: UserDataRepository,
    val bookRepository: BookRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val completedBookListUserData =
        userDataRepository.userData(UserDataPath.CompletedDownloadBookList)
    private val _downloadItemList = mutableStateListOf<DownloadItem>()
    val downloadItemIdList: List<DownloadItem> get() = _downloadItemList.toList()

    init {
        coroutineScope.launch {
            val completedBookList = completedBookListUserData.getOrDefault(emptyList())
            _downloadItemList.addAll(completedBookList.map {
                MutableDownloadItem(
                    DownloadType.valueOf(
                        it.type
                    ), it.bookId, bookRepository.getBookInformationFlow(it.bookId)
                ).apply { progress = 1f }
            })
        }
    }

    fun addExportItem(downloadItem: DownloadItem) {
        if (_downloadItemList.contains(downloadItem))
            _downloadItemList.removeIf { it == downloadItem }
        _downloadItemList.add(downloadItem)
        coroutineScope.launch {
            snapshotFlow { downloadItem.progress }.collect { progress ->
                if (progress >= 1f) {
                    completedBookListUserData.update(
                        updater = { downloadItems ->
                            val list = downloadItems.toMutableList()
                            val record =
                                CompletedDownload(downloadItem.type.name, downloadItem.bookId)
                            list.removeAll { it == record }
                            list + record
                        },
                        default = emptyList()
                    )
                    return@collect
                }
            }
        }
    }

    fun removeExportItem(downloadItem: DownloadItem) {
        _downloadItemList.remove(downloadItem)
    }

    fun clearCompleted() {
        _downloadItemList.removeIf { it.progress >= 1 }
        coroutineScope.launch {
            completedBookListUserData.set(emptyList())
        }
    }
}
