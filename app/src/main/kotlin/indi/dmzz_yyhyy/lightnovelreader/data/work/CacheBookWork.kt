package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadProgressRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadType
import indi.dmzz_yyhyy.lightnovelreader.data.download.MutableDownloadItem
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider

@HiltWorker
class CacheBookWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localBookDataSource: LocalBookDataSource,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val downloadProgressRepository: DownloadProgressRepository,
    private val bookRepository: BookRepository
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        private const val TAG = "CacheBookWork"

        fun ofId(id: String): String = "cache:$id"
    }

    override suspend fun doWork(): Result {
        val bookId = inputData.getString("bookId") ?: return Result.failure()
        if (bookId.isBlank()) return Result.failure()
        val downloadItem = MutableDownloadItem(
            DownloadType.CACHE,
            bookId,
            bookRepository.getBookInformationFlow(bookId)
        )
        downloadProgressRepository.addExportItem(downloadItem)
        webBookDataSourceProvider.value.getBookVolumes(bookId)
            .andThen { bookVolumes ->
                coroutineBinding {
                    var count = 0
                    val total = bookVolumes.volumes.sumOf { it.chapters.size } + 1
                    localBookDataSource.updateBookVolumes(bookVolumes)
                    bookVolumes.volumes.forEach { volume ->
                        volume.chapters.map { it.id }.forEach { chapterId ->
                            val chapter = webBookDataSourceProvider.value.getChapterContent(
                                chapterId = chapterId,
                                bookId = bookId
                            ).bind()
                            localBookDataSource.updateChapterContent(chapter)
                            count ++
                            downloadItem.progress = count.toFloat() / total
                        }
                    }
                }
            }
            .andThen {
                coroutineBinding {
                    val bookInformation = webBookDataSourceProvider.value.getBookInformation(bookId).bind()
                    localBookDataSource.updateBookInformation(bookInformation)
                }
            }
            .onOk {
                downloadItem.progress = 1f
            }
            .onErr {
                Looper.prepare()
                Toast.makeText(applicationContext, "缓存失败, ${it.message}", Toast.LENGTH_SHORT).show()
                it.throwable?.stackTraceToString()?.let { msg -> Log.e(TAG, msg) }
                Looper.loop()
            }

        return Result.success()
    }
}