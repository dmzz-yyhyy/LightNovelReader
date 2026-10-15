package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadProgressRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadType
import indi.dmzz_yyhyy.lightnovelreader.data.download.MutableDownloadItem
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.book.ChapterContent
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

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

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var notificationId: Int? = null


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BookCache",
                applicationContext.getString(R.string.book_cache_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundInfo(
        id: Int,
        notification: Notification
    ) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                id,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                id,
                notification
            )
        }

    private suspend fun showProgressNotification() {
        val notification = NotificationCompat.Builder(applicationContext, "BookCache")
            .setContentTitle(applicationContext.getString(R.string.cache_book_started, inputData.getString("title") ?: ""))
            .setContentText(applicationContext.getString(R.string.book_cache_notification_preparing))
            .setSmallIcon(R.drawable.cloud_download_24px)
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        notificationId?.let {
            setForeground(buildForegroundInfo(it, notification))
        }
    }

    private suspend fun updateFailureNotification() {
        val notification = NotificationCompat.Builder(applicationContext, "BookCache")
            .setContentTitle(applicationContext.getString(R.string.cache_book_started, inputData.getString("title") ?: ""))
            .setContentText(applicationContext.getString(R.string.book_cache_notification_failed))
            .setSmallIcon(R.drawable.cloud_download_24px)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setProgress(0, 0, false)
            .setAutoCancel(true)
            .build()

        notificationId?.let {
            setForeground(buildForegroundInfo(it, notification))
        }
    }

    private suspend fun updateCompletionNotification() {
        val notification = NotificationCompat.Builder(applicationContext, "BookCache")
            .setContentTitle(applicationContext.getString(R.string.cache_book_started, inputData.getString("title") ?: ""))
            .setContentText(applicationContext.getString(R.string.ebook_cache_notification_success))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.cloud_download_24px)
            .setProgress(0, 0, false)
            .setAutoCancel(true)
            .build()

        notificationId?.let {
            setForeground(buildForegroundInfo(it, notification))
        }
    }

    private suspend fun buildProgressNotification(
        progress: Int,
        currentChapterTitle: String,
        currentVolumeTitle: String
    ) {
        val text = "$currentVolumeTitle / $currentChapterTitle"

        val notification = NotificationCompat.Builder(applicationContext, "BookCache")
            .setContentTitle(applicationContext.getString(R.string.cache_book_started, inputData.getString("title") ?: "") + " ($progress%)")
            .setContentText(text)
            .setSmallIcon(R.drawable.cloud_download_24px)
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationId?.let {
            setForeground(buildForegroundInfo(it, notification))
        }
    }

    override suspend fun doWork(): Result {
        val bookId = inputData.getString("bookId") ?: return Result.failure()
        createNotificationChannel()
        notificationId = ofId(bookId).hashCode()
        if (bookId.isBlank()) return Result.failure()
        showProgressNotification()
        val downloadItem = MutableDownloadItem(DownloadType.CACHE, bookId)
        downloadProgressRepository.addExportItem(downloadItem)
        var count = 0
        val bookVolumes = webBookDataSourceProvider.default.getBookVolumes(bookId)
        val total = bookVolumes.volumes.sumOf { it.chapters.size } + 1
        if (bookVolumes.volumes.isEmpty()) return Result.failure().also {
            downloadProgressRepository.removeExportItem(downloadItem)
            updateFailureNotification()
        }
        var retry = 8
        var penaltyDelay = 0
        localBookDataSource.updateBookVolumes(bookVolumes)
        bookVolumes.volumes.forEach { volume ->
            volume.chapters.forEach { chapter ->
                var chapter = ChapterContent.empty(chapter.id)
                while (retry > 0 && chapter.isEmpty()) {
                    delay(1.5.seconds + penaltyDelay.seconds)
                    chapter = webBookDataSourceProvider.default.getChapterContent(
                        chapterId = chapter.id,
                        bookId = bookId
                    )
                    if (chapter.isEmpty()) {
                        if (penaltyDelay == 0) {
                            penaltyDelay = 2
                        }
                        penaltyDelay *= 2
                        retry--
                    }
                }
                if (chapter.isEmpty()) return Result.failure().also {
                    downloadProgressRepository.removeExportItem(downloadItem)
                    updateFailureNotification()
                } else {
                    penaltyDelay /= 2
                }
                localBookDataSource.updateChapterContent(chapter)
                count ++
                downloadItem.progress = count.toFloat() / total
                buildProgressNotification(
                    progress = (downloadItem.progress * 100).toInt(),
                    currentChapterTitle = chapter.title,
                    currentVolumeTitle = volume.volumeTitle
                )
            }
        }
        webBookDataSourceProvider.default.getBookInformation(bookId)
            .let {
                if (it.isEmpty()) return Result.failure().also {
                    downloadProgressRepository.removeExportItem(downloadItem)
                    updateFailureNotification()
                }
                localBookDataSource.updateBookInformation(it)
            }
        downloadItem.progress = 1f
        updateCompletionNotification()
        return Result.success()
    }
}