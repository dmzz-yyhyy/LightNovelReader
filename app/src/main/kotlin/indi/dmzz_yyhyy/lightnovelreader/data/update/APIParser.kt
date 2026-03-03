package indi.dmzz_yyhyy.lightnovelreader.data.update

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipFile

/**
 * LNR API 更新源
 * 通过 LNR API 获取更新信息
 */
object APIParser {
    private const val TAG = "APIParser"
    private const val BASE_URL = "https://lnr.curiousers.org"
    private const val API_PATH = "/api/update"

    class APIRelease(
        override val version: Int,
        override val versionName: String,
        override val releaseNotes: String,
        override val downloadUrl: String,
        override val downloadFileProgress: ((File, File) -> Unit)? = null
    ) : Release

    private fun fetchUpdate(
        channel: String,
        updatePhase: MutableStateFlow<String>,
        allowZipFallback: Boolean = false
    ): Release? {
        return try {
            updatePhase.tryEmit("API步骤: 正在请求 $channel 频道更新信息")
            val url = URL("$BASE_URL$API_PATH?channel=$channel")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "API request failed with status code: $responseCode")
                return null
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            updatePhase.tryEmit("API步骤: 解析更新信息")
            val json = JSONObject(responseBody)

            if (json.has("error")) {
                Log.e(TAG, "API returned error: ${json.getString("error")}")
                return null
            }

            val versionCode = json.getInt("version_code")
            val versionName = json.getString("version")
            val releaseNotes = json.getString("release_notes")

            updatePhase.tryEmit("API步骤: 获取下载链接")
            val artifacts = json.getJSONArray("artifacts")
            var downloadUrl: String? = null
            var isZip = false
            for (i in 0 until artifacts.length()) {
                val artifact = artifacts.getJSONObject(i)
                if (artifact.getString("name").endsWith(".apk")) {
                    downloadUrl = artifact.getString("download_url")
                    break
                }
            }
            if (downloadUrl == null && allowZipFallback) {
                for (i in 0 until artifacts.length()) {
                    val artifact = artifacts.getJSONObject(i)
                    val contentType = artifact.optString("content_type", "")
                    if (contentType.contains("zip")) {
                        downloadUrl = artifact.getString("download_url")
                        isZip = true
                        break
                    }
                }
            }

            if (downloadUrl == null) {
                Log.e(TAG, "No suitable artifact found in API response")
                return null
            }

            val downloadFileProgress: ((File, File) -> Unit)? = if (isZip) { zipFile, targetApk ->
                try {
                    ZipFile(zipFile).use { zip ->
                        val apkEntry = zip.entries().asSequence()
                            .filterNot { it.isDirectory }
                            .find { entry ->
                                entry.name.endsWith(".apk") &&
                                        "release" in entry.name
                            }
                            ?: throw IOException("在压缩包 [${zipFile.name}] 中未找到 APK 文件")

                        targetApk.parentFile?.mkdirs()
                        if (targetApk.exists()) targetApk.delete()

                        zip.getInputStream(apkEntry).use { input ->
                            targetApk.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解压失败: ${e.message}")
                    targetApk.delete()
                    throw e
                }
            } else null

            updatePhase.tryEmit("API步骤: 更新信息获取完成")
            APIRelease(
                version = versionCode,
                versionName = versionName,
                releaseNotes = releaseNotes,
                downloadUrl = downloadUrl,
                downloadFileProgress = downloadFileProgress
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch update from API: ${e.message}", e)
            null
        }
    }

    object StableParser : UpdateParser {
        override fun parser(updatePhase: MutableStateFlow<String>): Release? {
            return fetchUpdate("stable", updatePhase)
        }
    }

    object BetaParser : UpdateParser {
        override fun parser(updatePhase: MutableStateFlow<String>): Release? {
            return fetchUpdate("beta", updatePhase)
        }
    }

    object UnstableParser : UpdateParser {
        override fun parser(updatePhase: MutableStateFlow<String>): Release? {
            return fetchUpdate("unstable", updatePhase, allowZipFallback = true)
        }
    }
}