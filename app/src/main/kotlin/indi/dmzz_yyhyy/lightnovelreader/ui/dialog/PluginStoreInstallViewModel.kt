package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.store.StorePlugin
import indi.dmzz_yyhyy.lightnovelreader.utils.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PluginStoreInstallViewModel @Inject constructor(
    private val pluginManager: PluginManager
) : ViewModel() {
    var state: StoreInstallState by mutableStateOf(StoreInstallState.Loading)
    var downloadProgress: Float by mutableFloatStateOf(0f)
    private val base = "eNpb85aBtYRBK6OkpKDYSl-_IKc0PTOvWC8vsSgzO18vvyhdP7EgEyZsn5liCwDAsBIV"

    private val _navigateToInstall = MutableSharedFlow<File>()
    val navigateToInstall = _navigateToInstall.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun load(pluginId: String) {
        if (state is StoreInstallState.Ready) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = "${update(base)}$pluginId&ref=lnr-app&ver=${BuildConfig.VERSION_NAME}"
                val request = Request.Builder().url(url).get().build()
                val body = httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                    response.body.string()
                }
                val plugin = json.decodeFromJsonElement(StorePlugin.serializer(), json.parseToJsonElement(body).jsonObject["plugin"]!!)
                withContext(Dispatchers.Main) {
                    state = StoreInstallState.Ready(plugin)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    state = StoreInstallState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun install(plugin: StorePlugin) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    state = StoreInstallState.Downloading(plugin, 0f)
                }
                val tempDir = pluginManager.pluginsTempDir.also { it.mkdirs() }
                val destFile: File

                if (plugin.download.type == "multipart_zip") {
                    val parts = plugin.download.parts
                    val partFiles = parts.mapIndexed { index, part ->
                        val partFile = File(tempDir, "store_${plugin.id}_part${index + 1}.tmp")
                        downloadFile(part.url, partFile) { p ->
                            val overall = (index + p) / parts.size
                            viewModelScope.launch(Dispatchers.Main) {
                                downloadProgress = overall
                                state = StoreInstallState.Downloading(plugin, overall)
                            }
                        }
                        partFile
                    }
                    val mergedZip = File(tempDir, "store_${plugin.id}.zip")
                    mergedZip.outputStream().buffered().use { out ->
                        partFiles.forEach { f -> f.inputStream().buffered().use { it.copyTo(out) } }
                    }
                    partFiles.forEach { it.delete() }

                    val extracted = File(tempDir, "store_${plugin.id}.lnrp")
                    java.util.zip.ZipFile(mergedZip).use { zip ->
                        val entry = zip.entries().asSequence()
                            .firstOrNull { it.name.endsWith(".lnrp") || it.name.endsWith(".apk") }
                            ?: throw Exception("No plugin file found in archive")
                        zip.getInputStream(entry).buffered().use { input ->
                            extracted.outputStream().buffered().use { input.copyTo(it) }
                        }
                    }
                    mergedZip.delete()
                    destFile = extracted
                } else {
                    val part = plugin.download.parts.first()
                    val outFile = File(tempDir, "store_${plugin.id}.lnrp")
                    downloadFile(part.url, outFile) { p ->
                        viewModelScope.launch(Dispatchers.Main) {
                            state = StoreInstallState.Downloading(plugin, p)
                        }
                    }
                    destFile = outFile
                }

                _navigateToInstall.emit(destFile)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    state = StoreInstallState.Error(e.message ?: "Download failed")
                }
            }
        }
    }

    private fun downloadFile(url: String, dest: File, onProgress: (Float) -> Unit) {
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val body = response.body
            val total = body.contentLength().takeIf { it > 0 } ?: -1L
            body.byteStream().use { input ->
                FileOutputStream(dest).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead
                        if (total > 0) onProgress((downloaded.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
        }
        onProgress(1f)
    }
}
