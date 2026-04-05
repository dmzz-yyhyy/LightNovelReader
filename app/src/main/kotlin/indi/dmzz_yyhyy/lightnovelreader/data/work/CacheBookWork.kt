package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
    private val downloadProgressRepository: DownloadProgressRepository
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun ofId(id: String): String = "cache:$id"
    }

    override suspend fun doWork(): Result {
        val bookId = inputData.getString("bookId") ?: return Result.failure()
        if (bookId.isBlank()) return Result.failure()
        val downloadItem = MutableDownloadItem(DownloadType.CACHE, bookId)
        downloadProgressRepository.addExportItem(downloadItem)
        var count = 0
        val bookVolumes = webBookDataSourceProvider.default.getBookVolumes(bookId)
        val total = bookVolumes.volumes.sumOf { it.chapters.size } + 1
        if (bookVolumes.volumes.isEmpty()) return Result.failure()
        localBookDataSource.updateBookVolumes(bookVolumes)
        bookVolumes.volumes.forEach { volume ->
            volume.chapters.map { it.id }.forEach { chapterId ->
                val chapter = webBookDataSourceProvider.default.getChapterContent(
                    chapterId = chapterId,
                    bookId = bookId
                )
                if (chapter.isEmpty()) return Result.failure().also {
                    Looper.prepare()
                    Toast.makeText(applicationContext, "缓存失败，请检查网络环境", Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }
                localBookDataSource.updateChapterContent(chapter)
                count ++
                downloadItem.progress = count.toFloat() / total
            }
        }
        webBookDataSourceProvider.default.getBookInformation(bookId)
            .let {
                if (it.isEmpty()) return Result.failure()
                localBookDataSource.updateBookInformation(it)
            }
        downloadItem.progress = 1f
        return Result.success()
    }
}