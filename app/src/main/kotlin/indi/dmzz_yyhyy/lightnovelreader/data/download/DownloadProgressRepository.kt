package indi.dmzz_yyhyy.lightnovelreader.data.download

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import io.nightfish.lightnovelreader.api.userdata.UserData
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadProgressRepository @Inject constructor(
    userDataDao: UserDataDao
) {
    class IntUserData (
        override val path: String,
        private val userDataDao: UserDataDao
    ) : UserData<List<DownloadItem>>(path) {
        override fun set(value: List<DownloadItem>) {
            userDataDao.insert(path, group, "CompletedDownloadItemList", value.joinToString {
                "${it.type.name}|${it.bookId}"
            })
        }

        override fun get(): List<DownloadItem>? {
            return userDataDao.get(path)?.split(",")?.mapNotNull {
                val values = it.split("|")
                try {
                    return@mapNotNull MutableDownloadItem(
                        DownloadType.valueOf(values[0].trim()),
                        values[1]
                    ).apply { progress = 1f }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("CompletedDownloadItemList", "wrong data: $it")
                }
                return@mapNotNull null
            }
        }

        override fun getFlow(): Flow<List<DownloadItem>?> {
            return userDataDao.getFlow(path).map { value ->
                value?.split(",")?.map {
                    val values = it.split("|")
                    MutableDownloadItem(DownloadType.valueOf(values[0].trim()), values[1]).apply { progress = 1f }
                }
            }
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val completedBookListUserData = IntUserData(UserDataPath.CompletedDownloadBookList.path, userDataDao)
    private val _downloadItemList = mutableStateListOf<DownloadItem>()
    val downloadItemIdList: List<DownloadItem> get() = _downloadItemList.toList()

    init {
        coroutineScope.launch {
            val completedBookList = completedBookListUserData.getOrDefault(emptyList())
            _downloadItemList.addAll(completedBookList.map { it })
        }
    }

    fun addExportItem(downloadItem: DownloadItem) {
        if (_downloadItemList.contains(downloadItem))
            _downloadItemList.removeIf { it == downloadItem }
        _downloadItemList.add(downloadItem)
        coroutineScope.launch {
            snapshotFlow{ downloadItem.progress }.collect { progress ->
                if (progress >= 1f) {
                    completedBookListUserData.update(
                        updater = { downloadItems ->
                            val list = downloadItems.toMutableList()
                            if (list.contains(downloadItem))
                                list.removeIf { it == downloadItem }
                            downloadItems + downloadItem
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