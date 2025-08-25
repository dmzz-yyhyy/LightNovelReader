package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.work.ExportDataWork
import indi.dmzz_yyhyy.lightnovelreader.data.work.ImportDataWork
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ExportContext
import indi.dmzz_yyhyy.lightnovelreader.ui.components.MutableExportContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltViewModel
class SourceChangeDialogViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val workManager: WorkManager,
    private val webBookDataSource: WebBookDataSource,
    private val allWebDataSources: List<WebBookDataSource>,
    private val localBookDataSource: LocalBookDataSource,
    private val bookshelfRepository: BookshelfRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    val webBookDataSourceId = webBookDataSource.id
    val availableDataSources = allWebDataSources

    fun getWebDataSourceItems(): List<indi.dmzz_yyhyy.lightnovelreader.ui.components.WebDataSourceItem> {
        return allWebDataSources.map { dataSource ->
            indi.dmzz_yyhyy.lightnovelreader.ui.components.WebDataSourceItem(
                id = dataSource.id,
                name = when (dataSource.id) {
                    "wenku8".hashCode() -> "Wenku8"
                    "ZaiComic".hashCode() -> "ZaiComic"
                    else -> {
                        // For extensions, get the name from the adapter
                        if (dataSource is indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionWebDataSourceAdapter) {
                            dataSource.extensionName
                        } else {
                            "Unknown Source"
                        }
                    }
                },
                provider = when (dataSource.id) {
                    "wenku8".hashCode() -> "LightNovelReader from wenku8.net"
                    "ZaiComic".hashCode() -> "LightNovelReader from zaimanhua.com"
                    else -> {
                        if (dataSource is indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionWebDataSourceAdapter) {
                            "Extension (${dataSource.extensionId})"
                        } else {
                            "Unknown Source"
                        }
                    }
                }
            )
        }
    }

    private fun exportToFile(uri: Uri, exportContext: ExportContext): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<ExportDataWork>()
            .setInputData(
                workDataOf(
                    "uri" to uri.toString(),
                    "exportBookshelf" to exportContext.bookshelf,
                    "exportReadingData" to exportContext.readingData,
                    "exportSetting" to exportContext.settings,
                    "exportBookmark" to exportContext.bookmark,
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            uri.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest
    }

    private fun importFromFile(uri: Uri): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<ImportDataWork>()
            .setInputData(
                workDataOf(
                    "uri" to uri.toString(),
                    "ignoreDataIdCheck" to true
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            uri.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest
    }

    fun changeWebSource(webDataSourceId: Int, fileDir: File) {
        if (webDataSourceId == webBookDataSourceId) return

        CoroutineScope(Dispatchers.IO).launch {
            val oldUri = File(fileDir, "${webBookDataSource.id}.data.lnr").toUri()
            val exportRequest = exportToFile(oldUri, MutableExportContext().apply { settings = false })

            try {
                workManager.getWorkInfoByIdFlow(exportRequest.id).collect { workInfo ->
                    when (workInfo?.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            performDataClearAndImport(webDataSourceId, fileDir, oldUri)
                            cancel()
                        }
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            Log.e("SourceChange", "Export failed, aborting")
                            cancel()
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("SourceChange", "Export failed", e)
            }
        }
    }

    private suspend fun performDataClearAndImport(
        newSourceId: Int,
        fileDir: File,
        fallbackUri: Uri
    ) {
        val oldSourceId = webBookDataSourceId
        try {
            localBookDataSource.clear()
            bookshelfRepository.clear()
            userDataRepository.remove(UserDataPath.ReadingBooks.path)
            userDataRepository.remove(UserDataPath.Search.History.path)
            userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).set(newSourceId)
            statsRepository.clear()

            val newFile = File(fileDir, "$newSourceId.data.lnr")
            if (!newFile.exists()) {
                restartApp()
                return
            }

            val importRequest = importFromFile(newFile.toUri())
            workManager.getWorkInfoByIdFlow(importRequest.id).collect { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        Log.i("SourceChange", "All operations completed, restarting")
                        restartApp()
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        Log.e("SourceChange", "Import failed. Attempting rollback")
                        restoreFallbackData(fallbackUri, oldSourceId)
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            Log.e("SourceChange", "Error during import", e)
            restoreFallbackData(fallbackUri, oldSourceId)
        }
    }

    private suspend fun restoreFallbackData(uri: Uri, fallbackSourceId: Int) {
        try {
            userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).set(fallbackSourceId)
            val restoreRequest = importFromFile(uri)
            workManager.getWorkInfoByIdFlow(restoreRequest.id).collect { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    Log.i("SourceChange", "Rollback succeeded, restarting")
                    restartApp()
                } else if (workInfo?.state == WorkInfo.State.FAILED) {
                    Log.e("SourceChange", "Rollback failed.")
                }
            }
        } catch (e: Exception) {
            Log.e("SourceChange", "Rollback import failed with exception", e)
        }
    }

    private fun restartApp() {
        exitProcess(0)
    }
}
