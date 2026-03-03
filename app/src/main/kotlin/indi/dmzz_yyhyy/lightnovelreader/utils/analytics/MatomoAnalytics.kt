package indi.dmzz_yyhyy.lightnovelreader.utils.analytics

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.update
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper
import java.util.Locale
import kotlin.time.Duration.Companion.minutes

@Singleton
class MatomoAnalytics @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userDataRepository: UserDataRepository
) {
    private var tracker: Tracker? = null
    private var isStatisticsEnabled = false
    private var heartbeatJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val SERVER = "eNpb85aBtYRBO6OkpKDYSl8_MS8xp7IkM7lYL7m0KDO_tDi1qFgvvyhdPzexJD83X68gowAA2mUS-w"
        private const val SITE_ID = 4

        private const val DIMENSION_APP_INF = 1
        private const val DIMENSION_DEV_INF = 2
    }

    fun initialize() {
        isStatisticsEnabled = if (BuildConfig.DEBUG) false
        else userDataRepository.booleanUserData(UserDataPath.Settings.App.Statistics.path)
            .getOrDefault(true)

        if (!isStatisticsEnabled) return
        val server = update(SERVER).toString()

        tracker = TrackerBuilder
            .createDefault(server, SITE_ID)
            .setApplicationBaseUrl("LightNovelReader")
            .build(Matomo.getInstance(context))
            .apply {
                setSessionTimeout(30 * 60 * 1000)
                isOptOut = false
            }
    }

    private fun buildAppInfo(): String {
        val version = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        val locale = Locale.getDefault().toLanguageTag()
        val buildType = if (BuildConfig.DEBUG) "debug" else "release"
        return "$version, $locale, $buildType"
    }

    private fun buildDeviceInfo(): String {
        return "${Build.BRAND} ${Build.MODEL} | Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    fun trackAppLaunch() {
        if (!isStatisticsEnabled) return

        tracker?.let { t ->
            TrackHelper.track()
                .screen("/launch")
                .title("App Launch")
                .dimension(DIMENSION_APP_INF, buildAppInfo())
                .dimension(DIMENSION_DEV_INF, buildDeviceInfo())
                .with(t)
        }

        startHeartbeat()
    }

    private fun startHeartbeat() {
        if (heartbeatJob?.isActive == true) return
        if (!isStatisticsEnabled) return

        heartbeatJob = coroutineScope.launch {
            while (isActive) {
                delay(30.minutes)
                sendHeartbeat()
            }
        }
    }

    private fun sendHeartbeat() {
        tracker?.let { t ->
            TrackHelper.track()
                .screen("/ping")
                .title("Heartbeat")
                .with(t)
        }
    }

    fun onAppForeground() {
        if (!isStatisticsEnabled) return
        startHeartbeat()
    }

    fun onAppBackground() {
        if (!isStatisticsEnabled) return
        heartbeatJob?.cancel()
        heartbeatJob = null
        tracker?.dispatch()
    }
}