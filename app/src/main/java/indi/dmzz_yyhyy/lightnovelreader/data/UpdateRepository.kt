package indi.dmzz_yyhyy.lightnovelreader.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.ServerMetadata
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.ResponseBody
import java.io.*
import javax.inject.Inject


class UpdateRepository @Inject constructor(
    private val webDataSource: WebDataSource,
) {
    private var _isNeedUpdate: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var _isOver: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isNeedUpdate: StateFlow<Boolean> get() = _isNeedUpdate
    val isOver: StateFlow<Boolean> get() = _isOver

    private fun deleteSingleFile(file: File, isThrowFileNotFoundException: Boolean = true): Boolean {
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        return if (file.exists() && file.isFile) {
            if (file.delete()) {
                Log.e(
                    "DeleteFile",
                    "delete ${file.path} succeed"
                )
                true
            } else {
                Log.e("DeleteFile", "failed to delete ${file.path}")
                false
            }
        } else {
            if (isThrowFileNotFoundException) {
                Log.e("DeleteFile", "${file.path} not found")
            }
            false
        }
    }

    private fun writeResponseBodyToDisk(body: ResponseBody): Boolean {
        return try {
            val futureStudioIconFile: File =
                File(Environment.getExternalStorageDirectory().path + "/download/" + "LightNovelReaderUpdate.apk")
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)
                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d("FileDownload", "file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()
                Log.d(
                    "FileDownload",
                    "file saved at" + Environment.getExternalStorageDirectory().path + "/download/" + "LightNovelReaderUpdate.apk"
                )
                true
            } catch (e: IOException) {
                e.message?.let { Log.d("FileDownload", it) }
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            e.message?.let { Log.d("FileDownload", it) }
            false
        }
    }

    private suspend fun saveUpdateApk(): Boolean {
        val apkDate = webDataSource.getUpdateApk() ?: return false
        return writeResponseBodyToDisk(apkDate)
    }

    suspend fun updateApp(context: Context) {
        deleteSingleFile(
            File(Environment.getExternalStorageDirectory().path + "/download/" + "LightNovelReaderUpdate.apk"),
            false
        )
        if (saveUpdateApk()) {
            val intent = Intent(Intent.ACTION_VIEW)
            val apkFile =
                File(Environment.getExternalStorageDirectory().path + "/download/" + "LightNovelReaderUpdate.apk")
            intent.setDataAndType(
                FileProvider.getUriForFile(
                    context, "indi.dmzz_yyhyy.lightnovelreader.fileprovider", apkFile

                ),
                "application/vnd.android.package-archive"
            )
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            _isOver.value = true
            ContextCompat.startActivity(context, intent, null)
        } else {
            Log.i("FileDownload", "file download filed")
            _isOver.value = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun updateDetection(context: Context) {
        val serverMetadata: ServerMetadata = webDataSource.getServerMetadata() ?: return
        println(serverMetadata.clientVersion)
        val childVision: Int = serverMetadata.clientVersion
        var versionCode = -1
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionCode = packageInfo.longVersionCode.toInt()
            Log.i("VersionInfo", "versionCode = $versionCode")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("VersionInfo", "Error getting version code", e)
        }
        _isNeedUpdate.value = (childVision > versionCode)
    }


}
