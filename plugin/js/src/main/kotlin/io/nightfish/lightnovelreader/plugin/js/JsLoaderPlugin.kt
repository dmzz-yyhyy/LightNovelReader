package io.nightfish.lightnovelreader.plugin.js

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.V8Host
import io.nightfish.lightnovelreader.api.PluginContext
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi
import io.nightfish.lightnovelreader.api.web.WebDataSourceItem
import io.nightfish.lightnovelreader.plugin.js.api.book.JsBookInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.io.File
import java.time.LocalDateTime
import java.util.zip.ZipFile

@Suppress("unused")
@Plugin(
    version = 1000,
    name = "JsLoader",
    versionName = "0.0.1",
    author = "NightFish",
    description = "用于加载外部 JavaScript 数据源的插件",
    updateUrl = "https://gh-proxy.com/https://github.com/dmzz-yyhyy/LightNovelReader-PluginRepository/blob/main/data/io.nightfish.lightnovelreader.plugin.js",
    apiVersion = 1
)
class JsLoaderPlugin(
    val appContext: Context,
    val pluginContext: PluginContext,
    val userDataRepositoryApi: UserDataRepositoryApi,
    val dataSourceManagerApi: WebBookDataSourceManagerApi
) : LightNovelReaderPlugin {
    val scope = CoroutineScope(Dispatchers.IO)
    var jsRuntime: NodeRuntime? = null
    companion object {
        private const val TAG = "JsLoaderPlugin"
    }

    val jsWebDataSourceDir = pluginContext.dataDir.resolve("js_web_data_source")

    override fun onLoad() {
        Log.i(TAG, "JsLoaderPlugin is loaded")
        this.jsRuntime = V8Host.getNodeInstance().createV8Runtime()
        this.jsRuntime?.globalObject?.apply {
            set("BookInformation", JsBookInformation::class.java)
            set("LocalDateTime", LocalDateTime::class.java)
        }
        Log.i(TAG, "JsRuntime is loaded")
        ZipFile(
            pluginContext.getAsset("node_modules.zip")
        ).unzip(pluginContext.dataDir.resolve("node_modules"))
        jsWebDataSourceDir
            .listFiles { it.isDirectory }
            ?.forEach(::loadJsWebDataSource)
    }

    override fun onUnload() {
        scope.cancel("plugin unloaded")
        Log.i(TAG, "JsLoaderPlugin is unloaded")
        jsRuntime?.close()
        Log.i(TAG, "JsRuntime is unloaded")
    }

    fun loadJsWebDataSource(dir: File) {
        val packageInfo = dir.resolve("package.json").also {
            if (!it.exists() || it.isDirectory) {
                Log.e(TAG, "package.json is not found")
            }
        }.inputStream().use {
            it.bufferedReader().readText()
        }.let {
            Json.decodeFromString<PackageInfo>(it)
        }
        val webBookDataSource = LazyLoadWebDataSource(packageInfo.id) {
            EmptyWebDataSource
        }
        dataSourceManagerApi.registerWebDataSource(webBookDataSource, WebDataSourceItem(
            id = packageInfo.id,
            name = packageInfo.name,
            provider = packageInfo.provider
        ))
    }

    private fun ZipFile.unzip(targetDirectory: File) {
        if (targetDirectory.isFile) return
        for (entry in entries()) {
            val entryFile = File(targetDirectory, entry.name)

            if (!entryFile.canonicalPath.startsWith(targetDirectory.canonicalPath)) {
                throw SecurityException("zip entry is out of dir")
            }

            if (entry.isDirectory) {
                entryFile.mkdirs()
            } else {
                entryFile.parentFile?.mkdirs()
                getInputStream(entry).buffered().use {
                    it.copyTo(entryFile.outputStream())
                }
            }
        }
    }

    fun installWebDataSource(uri: Uri): Boolean {
        try {
            val tempDir = appContext.cacheDir.resolve("temp_js_data_source_zip")
                .also { it.mkdirs() }
            val tempZip = tempDir.resolve("data.zip")
            appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedInputStream(inputStream).use {
                    it.copyTo(tempZip.outputStream())
                }
            } ?: return false
            ZipFile(tempZip).unzip(tempDir)
            tempZip.delete()
            val packageInfo = tempDir.resolve("package.json").also {
                if (!it.exists() || it.isDirectory) {
                    Log.e(TAG, "package.json is not found")
                    return false
                }
            }.inputStream().use {
                it.bufferedReader().readText()
            }.let {
                Json.decodeFromString<PackageInfo>(it)
            }
            tempDir.resolve(packageInfo.webDataSourcePath).also {
                if (!it.exists() || it.isDirectory) {
                    Log.e(TAG, "main js file is not found")
                    return false
                }
            }
            val targetDir = jsWebDataSourceDir.resolve(packageInfo.id.toString())
            tempDir.listFiles()?.forEach { file ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(
                        targetDir
                            .resolve(file.canonicalPath.replace(tempDir.canonicalPath, "").replaceFirst("/", ""))
                            .also {
                                it.parentFile?.mkdirs()
                                it.createNewFile()
                            }
                            .outputStream()
                    )
                }
            }
            tempDir.deleteRecursively()
            loadJsWebDataSource(jsWebDataSourceDir.resolve(packageInfo.id.toString()))
            return true
        } catch (e: Exception) {
            Log.e(TAG, "error to install js data source")
            e.printStackTrace()
            return false
        }
    }

    fun selectDataFile(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
        }
        launcher.launch(Intent.createChooser(intent, "选择数据源文件"))
    }

    @Composable
    fun uriLauncher(block: (Uri) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.data?.let { uri ->
                    block(uri)
                }
            }
        }
    }

    @Composable
    override fun PageContent(paddingValues: PaddingValues) {
        val importDataSourceLauncher = uriLauncher {
            scope.launch {
                if (!installWebDataSource(it))
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(appContext, "加载数据源失败, 请检查数据源合法性", Toast.LENGTH_LONG).show()
                    }
            }
        }
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .clip(RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SettingsClickableEntry(
                modifier = Modifier.background(colorScheme.surfaceContainer),
                title = "安装JS数据源",
                description = "从外部安装js数据源",
                onClick = {
                    selectDataFile(importDataSourceLauncher)
                }
            )
        }
    }
}

class PluginDiscoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {}
}