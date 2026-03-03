package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.LightNovelReaderApplication
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.book.BookInformation
import kotlinx.coroutines.delay

@HiltWorker
class CheckUpdateWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val bookshelfRepository: BookshelfRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (appContext !is LightNovelReaderApplication) return Result.failure()
        val reminderBookMap = mutableMapOf<String, BookInformation>()
        val needRemindBookIdSet = mutableSetOf<String>()
        bookshelfRepository
            .getAllBookshelves()
            .filter { it.systemUpdateReminder }
            .forEach {
                needRemindBookIdSet.addAll(it.allBookIds)
            }
        bookshelfRepository.getAllBookshelfBooksMetadata().forEach { bookshelfBookMetadata ->
            delay(3000)
            while (!appContext.isAppStopped) {
                delay(5000)
            }
            if (!needRemindBookIdSet.contains(bookshelfBookMetadata.id)) return@forEach
            Log.d("CheckUpdateWork", "Updating book id=${bookshelfBookMetadata.id}")
            val bookInformation = webBookDataSourceProvider.lowPriority.getBookInformation(bookshelfBookMetadata.id)
            val webBookLastUpdate = bookInformation.lastUpdated
            if (webBookLastUpdate.isAfter(bookshelfBookMetadata.lastUpdate)) {
                bookshelfBookMetadata.bookShelfIds.forEach {
                    bookshelfRepository.addUpdatedBooksIntoBookShelf(it, bookshelfBookMetadata.id)
                    val bookshelf = bookshelfRepository.getBookshelf(it)
                    if (bookshelf != null && bookshelf.systemUpdateReminder)
                        reminderBookMap[bookshelfBookMetadata.id] = bookInformation
                }
                bookshelfRepository.updateBookshelfBookMetadataLastUpdateTime(bookInformation.id, webBookLastUpdate)
            }
        }
        reminderBookMap.values.forEach {
            with(NotificationManagerCompat.from(appContext)) {
                if (ActivityCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@forEach
                }
                createNotificationChannel()
                notify(
                    it.id.hashCode(),
                    NotificationCompat.Builder(appContext, "BookUpdate")
                        .setSmallIcon(R.drawable.icon_foreground)
                        .setContentTitle(appContext.getString(R.string.app_name))
                        .setContentText("您关注的轻小说 ${it.title} 更新了")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()
                )
            }
        }
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "更新提示"
            val descriptionText = "轻小说更新提示"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("BookUpdate", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}