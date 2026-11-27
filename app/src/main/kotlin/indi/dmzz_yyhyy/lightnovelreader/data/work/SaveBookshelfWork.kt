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
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.CancellationException

@HiltWorker
class SaveBookshelfWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localDataManager: LocalDataManager
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val TAG = "ExportDataWork"
    }

    override suspend fun doWork(): Result {
        val uri = inputData.getString("uri")?.let(Uri::parse) ?: return Result.failure()
        return try {
            val shelfId = inputData.getString("bookshelfId")
                ?.let { Identifier.parseAs<Identifier.Bookshelf>(it) }
            val selection = BackupSelection(
                localBookCache = false,
                bookshelf = true,
                readingRecord = false,
                settings = false,
                bookshelfId = shelfId,
                sourceNamespace = webBookDataSourceProvider.value.id.sourceNamespace
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
