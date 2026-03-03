package indi.dmzz_yyhyy.lightnovelreader.utils.network

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.work.ListenableWorker
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ImageDownloader(
    private val context: Context,
    private val tasks: List<Task>,
    val onProgress: (Int, Int) -> Unit,
) {
    var count = 0
        private set

    data class Task(val file: File, val uri: Uri)

    suspend fun run(): ListenableWorker.Result = withContext(Dispatchers.IO) {
        Log.i("ImageDownloader", "total tasks: ${tasks.size}")
        tasks.forEach { task ->
            ImageUtils.uriToBitmap(task.uri, context)
                .onSuccess { bitmap ->
                    task.file.parentFile?.mkdirs()
                    task.file.outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                }
                .onFailure {
                    return@withContext ListenableWorker.Result.failure()
                }
            count++
            onProgress(count, tasks.size)
            Log.i("ImageDownloader", "tasks: ${count}/${tasks.size}")
        }
        return@withContext ListenableWorker.Result.success()
    }
}