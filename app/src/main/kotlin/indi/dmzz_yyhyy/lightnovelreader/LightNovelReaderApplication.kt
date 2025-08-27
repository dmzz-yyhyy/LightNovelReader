package indi.dmzz_yyhyy.lightnovelreader

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionInitializer
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogLevel
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryInitializer
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import io.nightfish.potatoautoproxy.ProxyPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class LightNovelReaderApplication : Application(), Configuration.Provider {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var loggerRepository: LoggerRepository
    @Inject lateinit var userDataRepository: UserDataRepository
    @Inject lateinit var extensionInitializer: ExtensionInitializer
    @Inject lateinit var repositoryInitializer: RepositoryInitializer
    @Inject lateinit var webBookDataSourceProvider: Provider<WebBookDataSource>

    override val workManagerConfiguration: Configuration
        get()  =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        coroutineScope.launch(Dispatchers.IO) {
            loggerRepository.logLevel = LogLevel.from(userDataRepository.stringUserData(UserDataPath.Settings.Data.LogLevel.path).getOrDefault("none"))
            loggerRepository.startLogging()
        }
        coroutineScope.launch(Dispatchers.IO) {
            ProxyPool.enable = userDataRepository.booleanUserData(UserDataPath.Settings.Data.IsUseProxy.path).getOrDefault(false)
        }
        
                // Initialize repositories and extensions
        coroutineScope.launch(Dispatchers.IO) {
            try {
                println("LightNovelReaderApplication: Initializing repositories...")
                repositoryInitializer.initializeRepositories()
                
                println("LightNovelReaderApplication: Initializing extensions...")
                extensionInitializer.initializeExtensions()
                println("LightNovelReaderApplication: Extension initialization completed")
                
                // Check if the delegating data source should switch to an extension
                val webBookDataSource = webBookDataSourceProvider.get()
                if (webBookDataSource is indi.dmzz_yyhyy.lightnovelreader.data.web.DelegatingWebDataSource) {
                    (webBookDataSource as indi.dmzz_yyhyy.lightnovelreader.data.web.DelegatingWebDataSource).checkAndSwitchToPreferred()
                    println("LightNovelReaderApplication: Checked data source preference after extension loading")
                }
                
                // Check if user wants to use an extension but couldn't during startup
                checkPendingExtensionSwitch()
            } catch (e: Exception) {
                println("LightNovelReaderApplication: Error during initialization: ${e.message}")
                e.printStackTrace()
            }
        }
        
        WorkManager.getInstance(this).cancelAllWork()
    }
    
    private suspend fun checkPendingExtensionSwitch() {
        try {
            val webDataSourcesId = userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).get()
            val builtInIds = listOf("wenku8".hashCode(), "ZaiComic".hashCode())
            
            // If user preference is not a built-in source, it might be an extension
            if (!builtInIds.contains(webDataSourcesId)) {
                println("LightNovelReaderApplication: User prefers extension with ID $webDataSourcesId, checking if available now...")
                // Note: We don't force a restart here, just log for now
                // The next time they open data source dialog, the extension will be available
            }
        } catch (e: Exception) {
            println("LightNovelReaderApplication: Error checking pending extension switch: ${e.message}")
        }
    }
}