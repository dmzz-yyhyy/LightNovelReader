package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings

import android.content.Context
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.work.ExportDataWork
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ExportContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportUserDataDialogViewModel @Inject constructor(
    private val workManager: WorkManager,
) : ViewModel() {

    @Suppress("DuplicatedCode")
    fun exportAndSendToFile(exportContext: ExportContext, context: Context) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.applicationInfo.processName}.provider",
            File(context.cacheDir, "LightNovelReaderData.lnr")
        )
        val workRequest = OneTimeWorkRequestBuilder<ExportDataWork>()
            .setInputData(
                workDataOf(
                    "uri" to uri.toString(),
                    "exportLocalBookCache" to exportContext.localBookCache,
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
        viewModelScope.launch(Dispatchers.IO) {
            workManager.getWorkInfoByIdFlow(workRequest.id).collect {
                when (it?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        ShareCompat.IntentBuilder(context)
                            .setType("application/zip")
                            .setSubject("分享文件")
                            .addStream(uri)
                            .setChooserTitle("分享")
                            .startChooser()
                    }
                    else -> return@collect
                }
            }
        }
    }

    @Suppress("DuplicatedCode")
    fun exportToFile(uri: Uri, exportContext: ExportContext): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<ExportDataWork>()
            .setInputData(
                workDataOf(
                    "uri" to uri.toString(),
                    "exportLocalBookCache" to exportContext.localBookCache,
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
}