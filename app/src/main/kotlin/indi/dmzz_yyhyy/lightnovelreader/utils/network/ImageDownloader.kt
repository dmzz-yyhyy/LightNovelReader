package indi.dmzz_yyhyy.lightnovelreader.utils.network

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.work.ListenableWorker
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException

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
            val result = downloadWithRetry(task, maxRetry = 3)
            result
                .onOk { bitmap ->
                    try {
                        task.file.parentFile?.mkdirs()
                        task.file.outputStream().use {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "ImageDownloader",
                            "task $count: file write failed, file=${task.file}",
                            e
                        )
                        return@withContext ListenableWorker.Result.failure()
                    }
                }
                .onErr { t ->
                    Log.e(
                        "ImageDownloader",
                        "task $count failed, uri=${task.uri}",
                        t
                    )
                    return@withContext ListenableWorker.Result.failure()
                }
            count++
            onProgress(count, tasks.size)
            Log.i("ImageDownloader", "tasks: $count/${tasks.size}")
        }
        return@withContext ListenableWorker.Result.success()
    }

    private suspend fun downloadWithRetry(
        task: Task,
        maxRetry: Int
    ): Result<Bitmap, Throwable> {

        var lastError: Throwable? = null

        repeat(maxRetry) { attempt ->
            val result = ImageUtils.uriToBitmap(task.uri, context)
            var shouldRetry = false

            result
                .onOk {
                    return result
                }
                .onErr { error ->
                    lastError = error
                    if (error is SocketTimeoutException || error is ConnectException) {
                        shouldRetry = true
                        Log.w(
                            "ImageDownloader",
                            "retry ${attempt + 1}/$maxRetry for ${task.uri} (cause: ${error.cause}"
                        )
                    } else {
                        return result
                    }
                }
            if (shouldRetry) {
                delay(500L * (attempt + 1))
            }
        }
        return Err(lastError ?: RuntimeException("unknown error"))
    }
}