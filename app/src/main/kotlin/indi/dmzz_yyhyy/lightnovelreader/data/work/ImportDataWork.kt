package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.michaelbull.result.unwrapError
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalDataManager
import indi.dmzz_yyhyy.lightnovelreader.data.local.cbor.AppLocalData
import indi.dmzz_yyhyy.lightnovelreader.utils.readAppLocalData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import java.io.FileInputStream

@HiltWorker
class ImportDataWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataManager: LocalDataManager
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val TAG = "ImportDataWork"
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun doWork(): Result {
        val fileUri = inputData.getString("uri")?.let(Uri::parse) ?: return Result.failure()
        val appLocalData = try {
            applicationContext.contentResolver.openFileDescriptor(fileUri, "r")?.use { parcelFileDescriptor ->
                FileInputStream(parcelFileDescriptor.fileDescriptor).use { inputStream ->
                    Cbor.decodeFromByteArray<AppLocalData>(inputStream.readAppLocalData())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load file")
            e.printStackTrace()
            return Result.failure()
        } ?: return Result.failure()
        val result = localDataManager.importAppLocalData(appLocalData)
        if (result.isOk) return Result.success()
        else {
            Log.e(TAG, "Failed to import the data")
            result.unwrapError().printStackTrace()
            return Result.failure()
        }
    }
}