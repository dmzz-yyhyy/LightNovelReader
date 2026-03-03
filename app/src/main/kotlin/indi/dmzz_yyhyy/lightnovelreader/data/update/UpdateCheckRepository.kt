package indi.dmzz_yyhyy.lightnovelreader.data.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateCheckRepository @Inject constructor(
    @param:ApplicationContext @field:ApplicationContext private val context: Context,
    private val userDataRepository: UserDataRepository
) {
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var checkJob: Job? = null
    var release: Release? = null
        private set
    private val mutableAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val availableFlow: Flow<Boolean> = mutableAvailable
    private val _updatePhase = MutableStateFlow("未检查")
    val updatePhase: Flow<String> = _updatePhase
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "AppUpdateDownload"
        private const val NOTIFICATION_ID = 0x4C4E52
    }

    init {
        coroutineScope.launch {
            if (userDataRepository.booleanUserData(UserDataPath.Settings.App.AutoCheckUpdate.path).getOrDefault(true))
                check()
        }
    }

    fun resetAvailable() {
        coroutineScope.launch {
            mutableAvailable.update { false }
        }
    }

    fun check() {
        if (checkJob != null && checkJob!!.isActive) return
        checkJob = coroutineScope.launch {
            val updateChannelKey = userDataRepository.stringUserData(UserDataPath.Settings.App.UpdateChannel.path).get() ?: MenuOptions.UpdateChannelOptions.DEVELOPMENT
            val distributionPlatform = userDataRepository.stringUserData(UserDataPath.Settings.App.DistributionPlatform.path).get() ?: MenuOptions.UpdatePlatformOptions.GitHub
            Log.i("UpdateChecker", "Checking for updates from $distributionPlatform/$updateChannelKey")
            _updatePhase.update { "已请求更新，等待 $distributionPlatform 应答" }
            try {
                release =
                    MenuOptions.UpdatePlatformOptions
                        .getOptionWithValue(distributionPlatform).value
                        .getOptionWithValue(updateChannelKey).value
                        .parser(_updatePhase)
            } catch (e: Exception) {
                Log.e("UpdateChecker", "failed to get release")
                e.printStackTrace()
                _updatePhase.emit("${dateFormat.format(Date())} | 失败: ${e.javaClass.simpleName}\n${e.message}")
            }
            if (release != null) {
                if (release!!.version > BuildConfig.VERSION_CODE) {
                    Log.i("UpdateChecker", "Updates available: ${release!!.versionName}")
                    _updatePhase.emit("${dateFormat.format(Date())} | 有可用更新: ${release!!.versionName}")
                } else {
                    Log.i("UpdateChecker", "App is up to date (${release!!.versionName})")
                    _updatePhase.emit("${dateFormat.format(Date())} | 已是最新 (远程: ${release!!.versionName})")
                }
            }
            mutableAvailable.emit(release != null && release!!.version > BuildConfig.VERSION_CODE)
        }
    }

    fun downloadUpdate() {
        val release = release
        if (release == null) {
            Log.e("UpdateChecker", "Didn't find the release because release is null!")
            return
        }
        if (_isDownloading.value) {
            Log.w("UpdateChecker", "Download already in progress")
            return
        }

        val cacheDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val apkFile = cacheDir.resolve("LightNovelReader-update.apk").apply {
            if (exists()) delete()
        }
        val tempFile = cacheDir.resolve("LightNovelReader-update.tmp").apply {
            if (exists()) delete()
        }

        coroutineScope.launch {
            _isDownloading.emit(true)
            _downloadProgress.emit(0f)
            try {
                _updatePhase.emit("下载更新中…")
                createNotificationChannel()
                showDownloadNotification(0, release.versionName)

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url(release.downloadUrl)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("UpdateChecker", "Failed to download update: ${response.code}")
                        _updatePhase.emit("下载失败 (${response.code})")
                        showDownloadFailedNotification("HTTP ${response.code}")
                        return@use
                    }

                    response.body.let { body ->
                        val total = body.contentLength()
                        val input = body.byteStream()
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var bytesCopied = 0L
                            var bytes = input.read(buffer)
                            var lastNotificationUpdate = 0L

                            while (bytes >= 0) {
                                output.write(buffer, 0, bytes)
                                bytesCopied += bytes
                                bytes = input.read(buffer)

                                if (total > 0) {
                                    val progress = bytesCopied.toFloat() / total.toFloat()
                                    _downloadProgress.emit(progress)
                                    val progressPercent = (progress * 100).toInt()
                                    _updatePhase.emit("下载中... $progressPercent%")

                                    val now = System.currentTimeMillis()
                                    if (now - lastNotificationUpdate > 500) {
                                        showDownloadNotification(progressPercent, release.versionName)
                                        lastNotificationUpdate = now
                                    }
                                }
                            }
                        }
                    }
                }

                release.downloadFileProgress?.let { transform ->
                    _updatePhase.emit("合并更新文件…")
                    transform(tempFile, apkFile)
                } ?: tempFile.renameTo(apkFile)

                if (apkFile.exists() && apkFile.length() > 0L) {
                    _downloadProgress.emit(1f)
                    _updatePhase.emit("下载完成")
                    showDownloadCompleteNotification(apkFile)
                    withContext(Dispatchers.Main) {
                        installApk(apkFile)
                    }
                } else {
                    Log.e("UpdateChecker", "Downloaded file is empty")
                    _updatePhase.emit("下载失败 (空文件)")
                    showDownloadFailedNotification("文件为空")
                }
            } catch (e: Exception) {
                Log.e("UpdateChecker", "Download failed", e)
                _updatePhase.emit("下载失败 (${e.localizedMessage})")
                showDownloadFailedNotification(e.localizedMessage ?: "未知错误")
            } finally {
                _isDownloading.emit(false)
            }
        }
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "应用更新",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "应用更新下载进度"
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showDownloadNotification(progress: Int, versionName: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_foreground)
            .setContentTitle("正在下载更新 $versionName")
            .setContentText("$progress%")
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun showDownloadCompleteNotification(apkFile: File) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_foreground)
            .setContentTitle("更新下载完成")
            .setContentText("点击安装")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun showDownloadFailedNotification(reason: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_foreground)
            .setContentTitle("更新下载失败")
            .setContentText(reason)
            .setAutoCancel(true)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
