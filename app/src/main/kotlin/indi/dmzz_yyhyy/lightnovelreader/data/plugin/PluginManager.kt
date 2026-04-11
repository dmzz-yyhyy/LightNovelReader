package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation.NavGraphBuilder
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.runCatching
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.DexClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.utils.AnnotationScanner
import indi.dmzz_yyhyy.lightnovelreader.utils.getApkSignatures
import indi.dmzz_yyhyy.lightnovelreader.utils.isSignatureMatch
import io.nightfish.lightnovelreader.api.ApiCompat
import io.nightfish.lightnovelreader.api.PluginContext
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.plugin.PluginConstants
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.set

@Singleton
class PluginManager @Inject constructor(
    @field:ApplicationContext private val appContext: Context,
    private val webBookDataSourceManager: WebBookDataSourceManager,
    private val pluginInjector: PluginInjector,
    userDataRepository: UserDataRepository
) {
    companion object {
        const val TAG = "PluginManager"
    }

    private val mutableAllPluginMetadataList = mutableStateListOf<PluginMetadata>()
    val allPluginList: List<PluginMetadata> get() = mutableAllPluginMetadataList
    private val mutableLoadedPluginMap = mutableMapOf<String, LightNovelReaderPlugin>()
    val loadedPluginMap: Map<String, LightNovelReaderPlugin> get() = mutableLoadedPluginMap
    private val mutableErrorPluginMap = mutableMapOf<String, String>()
    val errorPluginMap: Map<String, String> get() = mutableErrorPluginMap

    private val enabledPluginsUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)

    val pluginsDir: File = appContext.dataDir.resolve("plugins")
    val pluginsTempDir: File = appContext.cacheDir.resolve("plugins_tmp")
    var appPluginInfos: List<PluginAppInfo> = emptyList()
       private set
    fun getPluginDir(name: String): File = pluginsDir.resolve(name)
    fun getPluginDataDir(pluginDir: File) = pluginDir.resolve("data")
    fun getPluginFile(pluginDir: File): File = pluginDir.resolve("plugin")
    fun getPluginAssetDir(pluginDir: File): File = pluginDir.resolve("asset")
    fun getPluginLibsDir(pluginDir: File): File = pluginDir.resolve("libs")
    private fun getPluginInstallLock(pluginDir: File) = pluginDir.resolve("lock")
    private fun getPluginLoadError(pluginDir: File) = pluginDir.resolve("error")
    private fun getPluginMetadataFile(pluginDir: File): File = pluginDir.resolve("metadata.json")

    private fun deletePluginWithoutData(pluginDir: File) {
        pluginDir.listFiles {
            it.name != "data"
        }?.forEach {
            it.deleteRecursively()
        }
    }

    fun unloadPlugin(packageName: String) {
        loadedPluginMap[packageName]?.onUnload()
        mutableLoadedPluginMap.remove(packageName)
        webBookDataSourceManager.unloadWebDataSourcesFromClassLoader(packageName)
        val enabledPlugins = enabledPluginsUserData.getOrDefault(emptyList()).toMutableList()
        if (packageName in enabledPlugins) {
            enabledPlugins -= packageName
            enabledPluginsUserData.asynchronousSet(enabledPlugins)
        }
    }

    fun initAllAppPlugin(): List<PluginAppInfo> {
        val intent = Intent(PluginConstants.DISCOVERY_ACTION)
        val receivers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.packageManager.queryBroadcastReceivers(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            appContext.packageManager.queryBroadcastReceivers(intent, PackageManager.MATCH_ALL)
        }

        return receivers.mapNotNull { resolveInfo ->
            val appInfo = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null.also {
                Log.e(TAG, "failed to get app info")
            }
            if (appInfo.packageName == appContext.packageName) return@mapNotNull null

            val packageName = appInfo.packageName
            val apkPath = appInfo.sourceDir ?: return@mapNotNull null.also {
                Log.e(TAG, "failed to get apk file path")
            }

            val appLabel = runCatching { appInfo.loadLabel(appContext.packageManager).toString() }
                .let {
                    if (it.isErr) packageName
                    else it.unwrap()
                }

            val versionName = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appContext.packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0L)
                    ).versionName
                } else {
                    @Suppress("DEPRECATION")
                    appContext.packageManager.getPackageInfo(packageName, 0).versionName
                }
            }.let {
                if (it.isErr) ""
                else it.unwrap() ?: ""
            }

            runCatching {
                val apkFile = File(apkPath)
                apkFile.inputStream().buffered().use { inputStream ->
                    val tempDir = pluginsTempDir.also { it.mkdirs() }
                    val tempFile = File(tempDir, "install_${System.currentTimeMillis()}.apk")
                    tempFile.outputStream().buffered().use {
                        inputStream.copyTo(it)
                    }
                }
                var error: InstallState.Error? = null
                runBlocking(Dispatchers.IO) {
                    installPlugin(apkFile).collect {
                        when (it) {
                            is InstallState.Error -> error = it
                            is InstallState.Completed -> Log.i(TAG, "App plugin successfully installed (package=$packageName)")
                            else -> {}
                        }
                    }
                }
                if (error != null) {
                    Log.e(TAG, "Failed to install app plugin")
                    error.result.printStackTrace()
                    return@mapNotNull null
                }
            }.let {
                if (it.isErr) ""
                else it.unwrap()
            }
            return@mapNotNull PluginAppInfo(
                packageName = packageName,
                name =appLabel,
                versionName = versionName
            )
        }
    }

    fun initAllPlugin() {
        webBookDataSourceManager.loadWebDataSourceFromClass(
            Wenku8Api::class.java,
            pluginInjector
        )
        appPluginInfos = initAllAppPlugin()
        val enabledPlugins = enabledPluginsUserData.getOrDefault(emptyList())
        val pluginDirs = pluginsDir.listFiles() ?: return
        for (dir in pluginDirs) {
            if (getPluginInstallLock(dir).exists()) continue
            val metadataFile = getPluginMetadataFile(dir)
            if (!metadataFile.exists()) {
                Log.w(TAG, "metadata.json not found in ${dir.name}, skipping")
                continue
            }
            mutableAllPluginMetadataList.removeAll {
                it.packageName == dir.name
            }
            val metadata = try {
                metadataFile.inputStream().use {
                    Json.decodeFromString<PluginMetadata>(it.readBytes().decodeToString())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse metadata for ${dir.name}", e)
                continue
            }.let { metadata ->
                if (metadata.packageName in appPluginInfos.map { it.packageName }) {
                    metadata.copy(
                        source = PluginSource.InstalledApp
                    )
                } else metadata
            }
            metadata.also(mutableAllPluginMetadataList::add)
            if (enabledPlugins.contains(dir.name) && ApiCompat.isSupported(metadata.apiVersion)) {
                val pluginMetadataResult = loadPlugin(dir.name)
                if (pluginMetadataResult.isErr) {
                    Log.e(TAG, "failed to load plugin ${dir.name}")
                    pluginMetadataResult.unwrapError().printStackTrace()
                }
            }
        }
    }

    private fun extractAssetFromApk(apk: File, targetDir: File) = runCatching {
        ZipFile(apk).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (!entry.isDirectory && entry.name.startsWith("assets/")) {
                    zip.getInputStream(entry).buffered().use { input ->
                        val out = targetDir.resolve(entry.name.removePrefix("assets/"))
                        out.parentFile?.mkdirs()
                        out.outputStream().buffered().use { input.copyTo(it) }
                    }
                }
            }
        }
    }

    private fun extractLibFromApk(apk: File, targetDir: File) = runCatching {
        val tempDir = targetDir.resolve("temp").also { it.mkdir() }
        val packageInfo = appContext.packageManager.getPackageArchiveInfo(apk.path, 0)
        packageInfo?.applicationInfo?.let {
            ZipFile(apk.path).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (
                        entry.name.startsWith("lib/") &&
                        !entry.isDirectory &&
                        !entry.name.endsWith("libandroidx.graphics.path.so")
                    ) {
                        val out = tempDir.resolve(entry.name.removePrefix("lib/"))
                        out.parentFile?.mkdirs()
                        zip.getInputStream(entry).buffered().use { input ->
                            out.outputStream().buffered().use { input.copyTo(it) }
                        }
                    }
                }
            }
        }
        val abiList = Build.SUPPORTED_ABIS
        for (abi in abiList.reversed()) {
            val abiDir = tempDir.resolve(abi)
            if (!abiDir.exists()) continue
            abiDir.listFiles()?.forEach { file ->
                val outputFile = targetDir.resolve(file.name)
                outputFile.parentFile?.mkdirs()
                if (!outputFile.exists()) outputFile.createNewFile()
                outputFile.outputStream().buffered().use {
                    file.inputStream().buffered().copyTo(it)
                }
            }
        }
        tempDir.deleteRecursively()
        return@runCatching
    }

    fun installPlugin(
        plugin: File
    ): Flow<InstallState> = flow {
        emit(InstallState.Start.ParsePackageInfo)
        val packageInfo = appContext.packageManager.getPackageArchiveInfo(
            plugin.path,
            PackageManager.GET_PERMISSIONS
        )
        if (packageInfo == null) {
            emit(InstallState.Error(Error("Failed to get package info from APK file: ${plugin.name}")))
            return@flow
        }
        val packageName = packageInfo.packageName

        emit(InstallState.Start.Clean)
        val pluginDir = getPluginDir(packageName)
        val lock = getPluginInstallLock(pluginDir)
        if (lock.exists()) {
            deletePluginWithoutData(pluginDir)
        }
        loadedPluginMap[packageName]?.let {
            unloadPlugin(packageName)
        }

        emit(InstallState.Start.ParsePluginMetadata)
        val pluginMetadataResult = getPluginMetadata(plugin, packageName)
        if (pluginMetadataResult.isErr) {
            emit(InstallState.Error(pluginMetadataResult.unwrapError()))
            return@flow
        }
        val newPluginMetadata = pluginMetadataResult.unwrap()
        emit(
            InstallState.Info(
                name = newPluginMetadata.name,
                packageName = newPluginMetadata.packageName,
                versionName = newPluginMetadata.versionName
            )
        )

        emit(InstallState.Start.CheckPluginInstallLegality)

        val metadataFile = getPluginMetadataFile(pluginDir)
        val currentPluginApk = getPluginFile(pluginDir)
        if (currentPluginApk.exists()) {
            val checkResult = runCatching {
                metadataFile
                    .inputStream()
                    .use {
                        Json.decodeFromString<PluginMetadata>(it.readBytes().decodeToString())
                    }
                    .also { println(it) }
            }.andThen { currentPluginMetadata ->
                if (newPluginMetadata.packageName in appPluginInfos.map { it.packageName }) {
                    return@andThen Err(PluginInstallError.AppPluginExist())
                }
                if (!ApiCompat.isSupported(newPluginMetadata.apiVersion)) {
                    return@andThen Err(PluginInstallError.PluginNotSupport(currentPluginMetadata.apiVersion))
                }
                if (currentPluginMetadata.version > newPluginMetadata.version) {
                    return@andThen Err(PluginInstallError.CurrentPluginVersionTooHighError())
                }
                if (!isSignatureMatch(
                        getApkSignatures(currentPluginApk),
                        getApkSignatures(plugin)
                    )
                ) {
                    return@andThen Err(PluginInstallError.PluginSignatureNotMatchError())
                }
                return@andThen Ok(Unit)
            }
            if (checkResult.isErr) {
                emit(InstallState.Error(checkResult.unwrapError()))
                return@flow
            }
        }

        emit(InstallState.Start.WritePluginMetadataToFile)
        runCatching {
            deletePluginWithoutData(pluginDir)
            pluginDir.mkdirs()
            lock.createNewFile()
        }.let {
            if (it.isErr) {
                emit(InstallState.Error(it.unwrapError()))
                return@flow
            }
        }
        val writeMetadataResult = runCatching {
            getPluginMetadataFile(pluginDir)
                .outputStream()
                .use {
                    it.write(Json.encodeToString<PluginMetadata>(newPluginMetadata).toByteArray())
                }
        }
        if (writeMetadataResult.isErr) {
            emit(InstallState.Error(writeMetadataResult.unwrapError()))
            return@flow
        }

        emit(InstallState.Start.CopyPlugin)
        val extractLibFromApkResult = extractLibFromApk(plugin, getPluginLibsDir(pluginDir))
        if (extractLibFromApkResult.isErr) {
            emit(InstallState.Error(extractLibFromApkResult.unwrapError()))
            return@flow
        }
        val extractAssetFromApkResult = extractAssetFromApk(plugin, getPluginAssetDir(pluginDir))
        if (extractAssetFromApkResult.isErr) {
            emit(InstallState.Error(extractAssetFromApkResult.unwrapError()))
            return@flow
        }
        val copyMainApkResult = runCatching {
            val target = getPluginFile(pluginDir)
            plugin.inputStream().buffered().use { inputStream ->
                target.outputStream().buffered().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        if (copyMainApkResult.isErr) {
            emit(InstallState.Error(copyMainApkResult.unwrapError()))
            return@flow
        }
        lock.delete()
        mutableAllPluginMetadataList.removeAll { it.packageName == packageName }
        mutableAllPluginMetadataList.add(newPluginMetadata)
        emit(InstallState.Completed(packageName))
    }
        .flowOn(Dispatchers.IO)

    fun markPluginError(packageName: String, message: String) {
        val pluginDir = getPluginDir(packageName)
            .also { it.mkdirs() }
        val error = getPluginLoadError(pluginDir)
        error.outputStream().buffered().use {
            it.write(message.toByteArray())
        }
        mutableErrorPluginMap[packageName] = message
    }

    fun getPluginError(packageName: String) {
        getPluginLoadError(getPluginDir(packageName))
    }

    fun loadPlugin(
        pluginPackage: String
    ): Result<PluginMetadata, Throwable> {
        val pluginDir = getPluginDir(pluginPackage)
        val packageInfo = appContext.packageManager.getPackageArchiveInfo(
            getPluginFile(pluginDir).absolutePath,
            PackageManager.GET_PERMISSIONS
        ) ?: run {
            val error = Error("Failed to get package info for plugin: $pluginPackage")
            Log.e(TAG, "loadPlugin($pluginPackage): ${error.message}", error)
            return Err(error)
        }
        val plugin = getPluginFile(pluginDir)
        return runCatching {
            plugin.setReadOnly()
        }.andThen {
            getPluginMetadataAndPluginClass(packageInfo.packageName)
        }.andThen {
            mutableErrorPluginMap.remove(pluginPackage)
            val pluginClazz = it.second
            val pluginContext = PluginContext(
                dataDir = getPluginDataDir(pluginDir),
                pluginFile = plugin,
                assetDir = getPluginAssetDir(pluginDir)
            )
            val instance = pluginInjector.providePlugin(
                pluginClazz,
                pluginContext
            )
                ?: return@andThen Err(Error("Failed to create instance of plugin class: ${pluginClazz.name}"))
            instance.onLoad()

            val classLoader = instance.javaClass.classLoader
            if (classLoader !is DexClassLoader) return@andThen Err(Error("Failed to get DexClassLoader from plugin instance, got: ${classLoader?.javaClass?.name}"))
            runCatching {
                webBookDataSourceManager.loadWebDataSourcesFromClassLoader(
                    classLoader,
                    pluginInjector,
                    pluginPackage
                )
            }.let { result ->
                if (result.isErr) {
                    val throwable = result.unwrapError()
                    markPluginError(pluginPackage, throwable.message.toString())
                    unloadPlugin(pluginPackage)
                    return@andThen Err(throwable)
                }
            }

            mutableLoadedPluginMap[pluginPackage] = instance
            return@andThen Ok(it.first)
        }.also {
            if (it.isErr) {
                val error = it.unwrapError()
                Log.e(
                    TAG,
                    "loadPlugin($pluginPackage) failed: ${error.message ?: error.toString()}",
                    error
                )
                markPluginError(pluginPackage, error.toString())
            }
        }
    }

    fun deletePlugin(packageName: String) {
        unloadPlugin(packageName)
        getPluginDir(packageName).deleteRecursively()
        mutableAllPluginMetadataList.removeAll { it.packageName == packageName }
    }

    private fun getPluginMetadata(file: File, packageName: String): Result<PluginMetadata, Throwable> =
        runCatching {
            if (file.canWrite() && !file.setReadOnly()) error("Failed to set read-only plugin file")
            DexClassLoader(
                file.absolutePath,
                null,
                null,
                this.javaClass.classLoader
            )
        }.andThen {
            AnnotationScanner.findAnnotatedClasses(
                classLoader = it,
                annotationClass = Plugin::class.java,
                scanPackage = packageName
            )
        }.andThen {
            runCatching {
                val plugin = it
                    .first(LightNovelReaderPlugin::class.java::isAssignableFrom)
                    .getAnnotation(Plugin::class.java)
                    ?: return@andThen Err(Error("Failed to get plugin annotation from the plugin class"))
                PluginMetadata.parse(plugin, packageName, getApkSignatures(file)?.isNotEmpty() == true)
            }
        }

    private fun getPluginMetadataAndPluginClass(packageName: String): Result<Pair<PluginMetadata, Class<*>>, Throwable> =
        runCatching {
            val pluginDir = getPluginDir(packageName)
            DexClassLoader(
                getPluginFile(pluginDir).absolutePath,
                null,
                getPluginLibsDir(pluginDir).absolutePath,
                this.javaClass.classLoader
            )
        }.andThen {
            AnnotationScanner.findAnnotatedClasses(
                classLoader = it,
                annotationClass = Plugin::class.java,
                scanPackage = packageName
            )
        }.andThen {
            runCatching {
                val clazz = it.first(LightNovelReaderPlugin::class.java::isAssignableFrom)
                val plugin = clazz.getAnnotation(Plugin::class.java)
                    ?: return@andThen Err(Error("Failed to get plugin annotation from the plugin class"))
                Pair(
                    PluginMetadata.parse(
                        plugin,
                        packageName,
                        getApkSignatures(getPluginFile(getPluginDir(packageName)))?.isNotEmpty() == true
                    ),
                    clazz
                )
            }
        }

    @Composable
    fun PluginContent(packageName: String, paddingValues: PaddingValues) {
        loadedPluginMap[packageName]?.PageContent(paddingValues)
    }

    fun NavGraphBuilder.onBuildNavHost() {
        loadedPluginMap.values.forEach {
            with(it) {
                onBuildNavHost()
            }
        }
    }
}
