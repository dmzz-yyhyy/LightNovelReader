package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadProgressRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadType
import indi.dmzz_yyhyy.lightnovelreader.data.download.MutableDownloadItem
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.ExportType
import indi.dmzz_yyhyy.lightnovelreader.utils.network.ImageDownloader
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.book.isNullOrEmpty
import io.nightfish.potatoepub.builder.ChapterBuilder
import io.nightfish.potatoepub.builder.EpubBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.dom4j.Element
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime

@HiltWorker
class ExportBookToEPUBWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localBookDataSource: LocalBookDataSource,
    private val downloadProgressRepository: DownloadProgressRepository,
    private val contentComponentRepository: ContentComponentRepository
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun ofId(id: String): String = "export_to_epub:$id"
    }

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notification: Notification? = null
    private var includeImages = true

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BookEpubExport",
                "EPUB 导出进度",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateProgressNotification(bookId: String, progress: Int) {
        notification = NotificationCompat.Builder(applicationContext, "BookEpubExport")
            .setContentTitle("导出『${inputData.getString("title")}』")
            .setContentText("已处理 ${progress}%")
            .setSmallIcon(R.drawable.file_export_24px)
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        notificationManager.notify(bookId.hashCode(), notification)
    }

    private fun showProgressNotification(bookId: String) {
        notification = NotificationCompat.Builder(applicationContext, "BookEpubExport")
            .setContentTitle("导出『${inputData.getString("title")}』")
            .setContentText("准备中...")
            .setSmallIcon(R.drawable.file_export_24px)
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        notificationManager.notify(bookId.hashCode(), notification)
    }

    private fun updateFailureNotification(bookId: String) {

        notification = NotificationCompat.Builder(applicationContext, "BookEpubExport")
            .setContentTitle("导出『${inputData.getString("title")}』")
            .setContentText("导出失败")
            .setSmallIcon(R.drawable.file_export_24px)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setProgress(0, 0, false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(bookId.hashCode(), notification)
    }

    private fun updateCompletionNotification(bookId: String) {
        notification = NotificationCompat.Builder(applicationContext, "BookEpubExport")
            .setContentTitle("导出『${inputData.getString("title")}』")
            .setContentText("已成功完成")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.file_export_24px)
            .setProgress(0, 0, false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(bookId.hashCode(), notification)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        createNotificationChannel()
        val bookId = inputData.getString("bookId") ?: return@withContext Result.failure()
        showProgressNotification(bookId)
        val exportType = ExportType.valueOf(inputData.getString("exportType") ?: return@withContext Result.failure())
        includeImages = inputData.getBoolean("includeImages", true)
        val fileUri = inputData.getString("uri")?.let(Uri::parse) ?: return@withContext Result.failure()
        val tempDir = applicationContext.cacheDir.resolve("epub").resolve(bookId)
        val cover = tempDir.resolve("cover.jpg")
            .also {
                if (it.exists()) it.delete()
            }
        val downloadItem = MutableDownloadItem(DownloadType.EPUB_EXPORT, bookId)
        downloadProgressRepository.addExportItem(downloadItem)
        if (bookId.isBlank()) {
            downloadItem.progress = -1f
            updateFailureNotification(bookId)
            return@withContext Result.failure()
        }
        val tasks = mutableListOf<ImageDownloader.Task>()
        var bookInformation = webBookDataSourceProvider.lowPriority.getBookInformation(bookId)
        if (bookInformation.isEmpty()) {
            val localData = localBookDataSource.getBookInformation(bookId)
            if (localData.isNullOrEmpty()) {
                downloadItem.progress = -1f
                updateFailureNotification(bookId)
                return@withContext Result.failure()
            }
            else bookInformation = localBookDataSource.getBookInformation(bookId)!!
        }
        var bookVolumes = webBookDataSourceProvider.lowPriority.getBookVolumes(bookId)
        if (bookVolumes.isEmpty()) {
            val localData = localBookDataSource.getBookVolumes(bookId)
            if (localData.isNullOrEmpty()) {
                downloadItem.progress = -1f
                updateFailureNotification(bookId)
                return@withContext Result.failure()
            }
            else bookVolumes = localData!!
        }
        val bookContentMap = mutableMapOf<String, ChapterContent>()
        updateProgressNotification(bookId, 0)
        downloadItem.progress = 0f
        val volumesCount = bookVolumes.volumes.size
        var currentVolumeIndex1 = 0

        bookVolumes.volumes.forEach { volume ->
            currentVolumeIndex1++
            val progressForVolume = (20 * currentVolumeIndex1) / volumesCount
            updateProgressNotification(bookId, progressForVolume)
            downloadItem.progress = progressForVolume / 100f
            volume.chapters.forEach {
                var chapterContent = webBookDataSourceProvider.lowPriority.getChapterContent(it.id, bookId)
                if (chapterContent.isEmpty()) {
                    val localData = localBookDataSource.getChapterContent(it.id)
                    if (localData.isNullOrEmpty()) {
                        downloadItem.progress = -1f
                        updateFailureNotification(bookId)
                        return@withContext Result.failure()
                    }
                    else chapterContent = localData!!
                }
                bookContentMap[it.id] = chapterContent
            }
        }
        updateProgressNotification(bookId, 20)
        downloadItem.progress = 0.2f

        updateProgressNotification(bookId, 20)
        downloadItem.progress = 0.2f

        return@withContext when (exportType) {
            ExportType.BOOK -> bookToEPUB(
                bookInformation,
                bookVolumes,
                bookContentMap,
                tempDir,
                tasks,
                volumesCount,
                bookId,
                downloadItem,
                cover,
                fileUri
            )
            ExportType.VOLUMES -> volumesToEPUB(
                inputData.getString("selectedVolume")?.split(",")
                    ?: return@withContext Result.failure().also {
                        updateFailureNotification(bookId)
                        downloadItem.progress = -1f
                    },
                bookInformation,
                bookVolumes,
                bookContentMap,
                tempDir,
                tasks,
                volumesCount,
                bookId,
                downloadItem,
                cover,
                fileUri
            )
        }
    }

    private suspend fun volumesToEPUB(
        selectedVolume: List<String>,
        bookInformation: BookInformation,
        bookVolumes: BookVolumes,
        bookContentMap: MutableMap<String, ChapterContent>,
        tempDir: File,
        tasks: MutableList<ImageDownloader.Task>,
        volumesCount: Int,
        bookId: String,
        downloadItem: MutableDownloadItem,
        cover: File,
        fileUri: Uri
    ): Result = withContext(Dispatchers.IO) {
        val epubMap = mutableMapOf<String, EpubBuilder>()
        tasks.add(ImageDownloader.Task(cover, bookInformation.coverUri))
        for ((currentVolumeIndex, volume) in bookVolumes.volumes.withIndex()) {
            if (!selectedVolume.contains(volume.volumeId)) continue
            val epub = EpubBuilder().apply {
                title = volume.volumeTitle
                modifier = LocalDateTime.now()
                creator = bookInformation.author
                description = bookInformation.description
                publisher = bookInformation.publishingHouse
                if (currentVolumeIndex == 0)
                    cover(cover)
                else {
                    val url = webBookDataSourceProvider.lowPriority.getCoverUriInVolume(bookId, volume, bookContentMap, applicationContext)
                    if (url == null) {
                        cover(cover)
                    } else {
                        val image = tempDir.resolve(url.hashCode().toString() + ".jpg")
                        tasks.add(ImageDownloader.Task(image, url))
                        cover(image)
                    }
                }
                val progressForVolume = (30 * currentVolumeIndex) / volumesCount
                updateProgressNotification(bookId, 20 + progressForVolume)
                downloadItem.progress = (20f + progressForVolume) / 100f
                for (chapterInformation in volume.chapters) {
                    chapter {
                        packChapter(chapterInformation, bookContentMap, tempDir, tasks, this@apply)
                    }
                }
            }
            epubMap[volume.volumeTitle] = epub
        }

        val imageDownloader = ImageDownloader(
            context = applicationContext,
            tasks = tasks,
            onProgress = { current, total ->
                val progress = (50 + current.toFloat() / total * 40).toInt()
                updateProgressNotification(bookId, progress)
                downloadItem.progress = progress / 100f
            }
        )

        if (async { imageDownloader.run() }.await() == Result.failure()) {
            downloadItem.progress = -1f
            return@withContext Result.failure()
        }

        val folder = DocumentFile.fromTreeUri(applicationContext, fileUri)
        if (folder == null) {
            downloadItem.progress = -1f
            return@withContext Result.failure()
        }
        for (epub in epubMap.entries) {
            val epubUri = folder.createFile("application/epub+zip", "${bookInformation.title} ${epub.key}.epub")?.uri
            if (epubUri == null) {
                downloadItem.progress = -1f
                return@withContext Result.failure()
            }
            val result = saveEpub(
                bookId,
                downloadItem,
                tempDir,
                epub.value,
                epubUri
            )
            if (result == Result.failure()) {
                downloadItem.progress = -1f
                return@withContext Result.failure()
            }
        }

        return@withContext Result.success()
    }

    private suspend fun bookToEPUB(
        bookInformation: BookInformation,
        bookVolumes: BookVolumes,
        bookContentMap: MutableMap<String, ChapterContent>,
        tempDir: File,
        tasks: MutableList<ImageDownloader.Task>,
        volumesCount: Int,
        bookId: String,
        downloadItem: MutableDownloadItem,
        cover: File,
        fileUri: Uri
    ): Result = withContext(Dispatchers.IO) {
        var currentVolumeIndex = 0
        val epub = EpubBuilder().apply {
            title = bookInformation.title
            modifier = LocalDateTime.now()
            creator = bookInformation.author
            description = bookInformation.description
            publisher = bookInformation.publishingHouse

            bookVolumes.volumes.forEach { volume ->
                chapter {
                    packVolume(volume, bookContentMap, tempDir, tasks, this@apply)
                }
                currentVolumeIndex++
                val progressForVolume = (30 * currentVolumeIndex) / volumesCount
                updateProgressNotification(bookId, 20 + progressForVolume)
                downloadItem.progress = (20f + progressForVolume) / 100f
            }
            tasks.add(ImageDownloader.Task(cover, bookInformation.coverUri))
            cover(cover)
        }

        val imageDownloader = async {
            ImageDownloader(
                context = applicationContext,
                tasks = tasks,
                onProgress = { current, total ->
                    val progress = (50 + current.toFloat() / total * 40).toInt()
                    updateProgressNotification(bookId, progress)
                    downloadItem.progress = progress / 100f
                }
            ).run()
        }


        return@withContext if (imageDownloader.await() == Result.success()) {
            saveEpub(bookId, downloadItem, tempDir, epub, fileUri).also {
                if (it == Result.success())
                    updateCompletionNotification(bookId)
                else {
                    downloadItem.progress = -1f
                    updateFailureNotification(bookId)
                }
            }
        } else {
            downloadItem.progress = -1f
            updateFailureNotification(bookId)
            Result.failure()
        }
    }

    private fun ChapterBuilder.packVolume(
        volume: Volume,
        bookContentMap: MutableMap<String, ChapterContent>,
        tempDir: File,
        tasks: MutableList<ImageDownloader.Task>,
        epubBuilder: EpubBuilder
    ) {
        title(volume.volumeTitle)
        volume.chapters.forEach {
            chapter {
                packChapter(it, bookContentMap, tempDir, tasks, epubBuilder)
            }
        }
    }

    private fun ChapterBuilder.packChapter(
        it: ChapterInformation,
        bookContentMap: MutableMap<String, ChapterContent>,
        tempDir: File,
        tasks: MutableList<ImageDownloader.Task>,
        epubBuilder: EpubBuilder
    ) {
        title(it.title)
        content {
            contentComponentRepository.getDataFromJsonObject(bookContentMap[it.id]!!.content) {
                bodyElement.add(
                    it.toHtmlElement(applicationContext).also { element ->
                        element.praseSrc(tempDir, tasks, epubBuilder)
                    }
                )
            }
        }
    }

    private fun Element.praseSrc(
        tempDir: File,
        tasks: MutableList<ImageDownloader.Task>,
        epubBuilder: EpubBuilder
    ) {
        val src = this.attributes().firstOrNull { it.name == "src" }
        if (src != null && src.value.runCatching { this.toUri() }.isSuccess) {
            val id = src.value.hashCode()
            val image = tempDir.resolve("image_$id.jpg")
            tasks.add(ImageDownloader.Task(image, src.value.toUri()))
            src.value = "image/image_$id.jpg"
            epubBuilder.imgRes(
                href = src.value,
                id = id.toString(),
                file = image
            )
        }
        this.elements().forEach {
            it.praseSrc(tempDir, tasks, epubBuilder)
        }
    }

    private fun saveEpub(
        bookId: String,
        downloadItem: MutableDownloadItem,
        tempDir: File,
        epub: EpubBuilder,
        fileUri: Uri
    ): Result {
        updateProgressNotification(bookId, 90)
        downloadItem.progress = 90f
        downloadItem.progress = 0.90f

        val file = tempDir.resolve("epub")
        try {
            epub.build().save(file)
        } catch (e: Exception) {
            e.printStackTrace()
            updateFailureNotification(bookId)
            downloadItem.progress = -1f
            return Result.failure()
        }
        updateProgressNotification(bookId, 95)
        downloadItem.progress = 0.95f
        applicationContext.contentResolver.openOutputStream(fileUri)
            ?.use { outputStream ->
                FileInputStream(file).use { inputStream ->
                    val buffer = ByteArray(1024 * 1024) // = 1MB
                    var bytesRead: Int
                    var totalBytes = 0L
                    val fileSize = file.length()

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead
                        if (fileSize > 0) {
                            val writeProgress =
                                95 + (totalBytes.toFloat() / fileSize * 5).toInt()
                            updateProgressNotification(bookId, writeProgress)
                            downloadItem.progress = writeProgress / 100f
                        }
                    }
                }
            }
        tempDir.delete()
        updateCompletionNotification(bookId)
        return Result.success()
    }
}
