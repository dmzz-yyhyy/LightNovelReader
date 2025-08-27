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
import io.nightfish.potatoautoproxy.ProxyPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LightNovelReaderApplication : Application(), Configuration.Provider {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var loggerRepository: LoggerRepository
    @Inject lateinit var userDataRepository: UserDataRepository
    @Inject lateinit var extensionInitializer: ExtensionInitializer
    @Inject lateinit var repositoryInitializer: RepositoryInitializer

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
            } catch (e: Exception) {
                println("LightNovelReaderApplication: Error during initialization: ${e.message}")
                e.printStackTrace()
            }
        }
        
        WorkManager.getInstance(this).cancelAllWork()
    }
}