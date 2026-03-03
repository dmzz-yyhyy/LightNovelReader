package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.repository

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.RemotePluginMetadata
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class RepoIndex(val plugins: List<RepoIndexEntry>)

@Serializable
data class RepoIndexEntry(val id: String, val name: String)

enum class RepoTaskState {
    Queued, Running, Cancelled, Done
}

sealed class DownloadResult {
    data class Success(val file: File) : DownloadResult()
    data class Failed(val reason: String) : DownloadResult()
}

@HiltViewModel
class PluginRepositoryViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    pluginManager: PluginManager,
) : ViewModel() {

    val pluginList = pluginManager.allPluginList
    val repositoryUiState = MutablePluginRepositoryUiState()
    private val json = Json { ignoreUnknownKeys = true }

    private val repoInstallChannel = Channel<RemotePluginMetadata>(Channel.UNLIMITED)
    private val taskStateMap = mutableStateMapOf<String, RepoTaskState>()

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    private val _navigateToInstallDialog = MutableSharedFlow<File>()
    val navigateToInstallDialog = _navigateToInstallDialog.asSharedFlow()
    private var repoCurrentInstallJob: Job? = null


    init {
        viewModelScope.launch {
            for (meta in repoInstallChannel) {
                val id = meta.id
                if (taskStateMap[id] == RepoTaskState.Cancelled) {
                    taskStateMap.remove(id)
                    continue
                }
                taskStateMap[id] = RepoTaskState.Running
                repositoryUiState.currentInstalling = id
                repositoryUiState.queue = repositoryUiState.queue.filterNot { it == id }

                repoCurrentInstallJob = launch {
                    try {
                        repositoryUiState.progressMap[id] = 0f
                        installPluginFromRepository(meta)
                        taskStateMap[id] = RepoTaskState.Done
                    } catch (e: CancellationException) {
                        taskStateMap[id] = RepoTaskState.Cancelled
                        throw e
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        showSnackbar(
                            context.getString(
                                R.string.plugin_repo_install_failed,
                                meta.name,
                                t.message ?: context.getString(R.string.unspecified)
                            )
                        )
                        taskStateMap[id] = RepoTaskState.Done
                    } finally {
                        repositoryUiState.progressMap.remove(id)
                        repositoryUiState.currentInstalling = null
                        repoCurrentInstallJob = null
                    }
                }

                repoCurrentInstallJob?.join()
            }
        }
    }

    fun enqueueInstallFromRepository(meta: RemotePluginMetadata) {
        val id = meta.id
        val state = taskStateMap[id]
        repositoryUiState.progressMap.remove(id)
        if (state == RepoTaskState.Queued || state == RepoTaskState.Running) return
        taskStateMap[id] = RepoTaskState.Queued
        repositoryUiState.queue += id
        repoInstallChannel.trySend(meta)
    }

    fun cancelQueuedInstall(id: String) {
        when (taskStateMap[id]) {
            RepoTaskState.Queued -> {
                repositoryUiState.queue -= id
                repositoryUiState.progressMap.remove(id)
                taskStateMap[id] = RepoTaskState.Cancelled
            }
            RepoTaskState.Running -> {
                repoCurrentInstallJob?.cancel(CancellationException("user cancelled"))
                repositoryUiState.currentInstalling = null
                repositoryUiState.progressMap.remove(id)
                taskStateMap[id] = RepoTaskState.Cancelled
                showSnackbar(context.getString(R.string.plugin_repo_install_cancelled, id))
            }
            else -> Unit
        }
    }

    fun loadPluginRepository(repoBaseUrl: String = REPO_BASE_URL) {
        if (repositoryUiState.isLoading) return
        repositoryUiState.isLoading = true
        repositoryUiState.errorMessage = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val indexUrl = repoBaseUrl.trimEnd('/') + "/plugins.json"
                val body = Jsoup.connect(indexUrl).ignoreContentType(true).timeout(10_000).execute().body()
                val repoIndex = json.decodeFromString(RepoIndex.serializer(), body)

                withContext(Dispatchers.Main) {
                    repositoryUiState.indexList = repoIndex.plugins
                    repositoryUiState.remotePluginMetadataList = emptyList()
                }

                val semaphore = Semaphore(4)
                repoIndex.plugins.map { entry ->
                    async(Dispatchers.IO) {
                        semaphore.acquire()
                        try {
                            withContext(Dispatchers.Main) { repositoryUiState.metadataLoadingStates[entry.id] = true }
                            val meta = loadPluginMetadata(entry.id, repoBaseUrl)
                            meta?.let {
                                withContext(Dispatchers.Main) {
                                    repositoryUiState.remotePluginMetadataList = repositoryUiState.remotePluginMetadataList + it
                                }
                            }
                        } finally {
                            withContext(Dispatchers.Main) { repositoryUiState.metadataLoadingStates[entry.id] = false }
                            semaphore.release()
                        }
                    }
                }.awaitAll()

                withContext(Dispatchers.Main) { repositoryUiState.isLoading = false }
            } catch (t: Throwable) {
                t.printStackTrace()
                withContext(Dispatchers.Main) {
                    repositoryUiState.errorMessage = t.message
                    repositoryUiState.isLoading = false
                }
            }
        }
    }

    private fun loadPluginMetadata(pluginId: String, repoBaseUrl: String): RemotePluginMetadata? {
        return try {
            val metadataUrl = "${repoBaseUrl.trimEnd('/')}/$pluginId/metadata.json"
            val body = Jsoup.connect(metadataUrl).ignoreContentType(true).timeout(10_000).execute().body()
            val meta = json.decodeFromString(RemotePluginMetadata.serializer(), body)
            if (meta.id != pluginId) meta.copy(id = pluginId) else meta
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    fun loadPluginMetadataIfNeeded(pluginId: String, repoBaseUrl: String = REPO_BASE_URL) {
        if (repositoryUiState.metadataLoadingStates[pluginId] == true ||
            repositoryUiState.remotePluginMetadataList.any { it.id == pluginId }) return

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { repositoryUiState.metadataLoadingStates[pluginId] = true }
            try {
                loadPluginMetadata(pluginId, repoBaseUrl)?.let { meta ->
                    withContext(Dispatchers.Main) {
                        repositoryUiState.remotePluginMetadataList = repositoryUiState.remotePluginMetadataList + meta
                    }
                }
            } finally {
                withContext(Dispatchers.Main) { repositoryUiState.metadataLoadingStates[pluginId] = false }
            }
        }
    }

    private suspend fun installPluginFromRepository(metadata: RemotePluginMetadata) = withContext(Dispatchers.IO) {

    }

    private suspend fun mergePartsToFile(partFiles: List<File>, outputFile: File): File = withContext(Dispatchers.IO) {
        outputFile.outputStream().buffered().use { out ->
            partFiles.forEach { f ->
                f.inputStream().buffered().use { it.copyTo(out) }
            }
        }
        outputFile
    }

    private fun cleanupTempFiles(files: List<File>) {
        files.forEach { if (it.exists()) it.delete() }
    }

    private suspend fun downloadParts(
        pluginId: String,
        base: String,
        tempDir: File,
        partsCount: Int
    ): List<File> = withContext(Dispatchers.IO) {
        if (partsCount <= 0) return@withContext emptyList()

        repositoryUiState.progressMap[pluginId] = 0f

        val partFiles = arrayOfNulls<File>(partsCount)
        val partProgress = FloatArray(partsCount)
        val lock = Any()
        val semaphore = Semaphore(4)

        val partUrls = (1..partsCount).map { i ->
            val num = i.toString().padStart(3, '0')
            "$base/$pluginId/plugin.zip.$num"
        }

        coroutineScope {
            partUrls.mapIndexed { index, url ->
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val destFile = tempDir.resolve("${pluginId.hashCode()}_part${index + 1}.tmp")
                        downloadFileDirect(
                            url = url,
                            destFile = destFile
                        ) { p ->
                            synchronized(lock) {
                                partProgress[index] = p
                                val total = (partProgress.sum() / partsCount) * 0.8f
                                launch(Dispatchers.Main) {
                                    repositoryUiState.progressMap[pluginId] = total
                                }
                            }
                        }
                        partFiles[index] = destFile
                    } finally {
                        semaphore.release()
                    }
                }
            }.awaitAll()
        }

        val completed = partFiles.count { it != null }
        if (completed != partsCount) {
            val missing = (0 until partsCount).filter { partFiles[it] == null }.joinToString(",") { "${it + 1}" }
            throw Exception(
                context.getString(
                    R.string.plugin_repo_parts_incomplete,
                    completed,
                    partsCount,
                    missing
                )
            )
        }

        return@withContext partFiles.map { it!! }
    }

    private suspend fun downloadFileDirect(
        url: String,
        destFile: File,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        println("downloading file ${destFile.name}")
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var output: OutputStream? = null

        try {
            val u = URL(url)
            connection = (u.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
                requestMethod = "GET"
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode} ${connection.responseMessage}")
            }

            val totalSize = connection.contentLengthLong.takeIf { it > 0L } ?: -1L
            input = BufferedInputStream(connection.inputStream)
            output = BufferedOutputStream(FileOutputStream(destFile))

            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytesRead: Int
            var downloaded = 0L
            var lastReport = 0L

            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                downloaded += bytesRead

                if (totalSize > 0) {
                    val progress = downloaded.toFloat() / totalSize.toFloat()
                    if (downloaded - lastReport > 100 * 1024 || progress - (lastReport / totalSize.toFloat()) > 0.01f) {
                        lastReport = downloaded
                        onProgress(progress.coerceIn(0f, 1f))
                    }
                } else onProgress(0f)

            }

            output.flush()
            onProgress(1f)
        } catch (e: Exception) {
            destFile.delete()
            throw e
        } finally {
            input?.close()
            output?.close()
            connection?.disconnect()
        }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch { _snackbarFlow.emit(message) }
    }

    companion object {
        const val REPO_BASE_URL =
            "https://v6.gh-proxy.com/https://github.com/dmzz-yyhyy/LightNovelReader-PluginRepository/blob/main/data"
    }
}
