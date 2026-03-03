package indi.dmzz_yyhyy.lightnovelreader

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogLevel
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.CxHttpInit
import indi.dmzz_yyhyy.lightnovelreader.utils.analytics.MatomoAnalytics
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.potatoautoproxy.ProxyPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject

@HiltAndroidApp
class LightNovelReaderApplication : Application(), Configuration.Provider {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    var isAppStopped = false
        private set
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var loggerRepository: LoggerRepository
    @Inject lateinit var userDataRepository: UserDataRepository
    @Inject lateinit var pluginManager: PluginManager
    @Inject lateinit var matomoAnalytics: MatomoAnalytics

    override val workManagerConfiguration: Configuration
        get()  =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @ExperimentalSerializationApi
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            System.setProperty("kotlinx.coroutines.debug", "on")
        }
        CxHttpInit.init()
        matomoAnalytics.initialize()
        matomoAnalytics.trackAppLaunch()

        var activityCount = 0
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                if (activityCount == 0) {
                    matomoAnalytics.onAppForeground()
                }
                activityCount++
                isAppStopped = false
            }

            override fun onActivityStopped(activity: Activity) {
                activityCount--
                if (activityCount == 0) {
                    matomoAnalytics.onAppBackground()
                }
                isAppStopped = activityCount == 0
            }
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        pluginManager.initAllPlugin()
        coroutineScope.launch(Dispatchers.IO) {
            loggerRepository.logLevel = LogLevel.from(userDataRepository.stringUserData(UserDataPath.Settings.Data.LogLevel.path).getOrDefault("none"))
            loggerRepository.startLogging()
        }
        coroutineScope.launch(Dispatchers.IO) {
            ProxyPool.enable = userDataRepository.booleanUserData(UserDataPath.Settings.Data.IsUseProxy.path).getOrDefault(false)
        }
        WorkManager.getInstance(this).cancelAllWork()
    }
}