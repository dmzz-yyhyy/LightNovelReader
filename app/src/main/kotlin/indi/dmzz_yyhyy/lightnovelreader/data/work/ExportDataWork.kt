package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalDataManager
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupSelection
import kotlinx.coroutines.CancellationException

@HiltWorker
class ExportDataWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataManager: LocalDataManager
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val TAG = "ExportDataWork"
    }

    override suspend fun doWork(): Result {
        val uri = inputData.getString("uri")?.let(Uri::parse) ?: return Result.failure()
        return try {
            val selection = BackupSelection(
                localBookCache = inputData.getBoolean("exportLocalBookCache", true),
                bookshelf = inputData.getBoolean("exportBookshelf", true),
                readingRecord = inputData.getBoolean("exportReadingData", true),
                settings = inputData.getBoolean("exportSetting", true)
            )
            appContext.contentResolver.openOutputStream(uri, "wt")
                ?.use { localDataManager.export(it, selection) }
                ?: error("Cannot open export destination")
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e(TAG, "Backup operation failed", error)
            Result.failure()
        }
    }
}
