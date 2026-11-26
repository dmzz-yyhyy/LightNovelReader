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
import kotlinx.coroutines.CancellationException

@HiltWorker
class ImportDataWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataManager: LocalDataManager
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val TAG = "ImportDataWork"
    }

    override suspend fun doWork(): Result {
        val uri = inputData.getString("uri")?.let(Uri::parse) ?: return Result.failure()
        return try {
            appContext.contentResolver.openInputStream(uri)?.use {
                localDataManager.import(it, inputData.getBoolean("overwrite", false))
            } ?: error("Cannot open backup")
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e(TAG, "Backup operation failed", error)
            Result.failure()
        }
    }
}
