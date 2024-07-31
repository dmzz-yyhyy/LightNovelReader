package indi.dmzz_yyhyy.lightnovelreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.BuildConfig
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import dagger.hilt.android.AndroidEntryPoint
import indi.dmzz_yyhyy.lightnovelreader.ui.LightNovelReaderApp
import indi.dmzz_yyhyy.lightnovelreader.utils.LightNovelReaderTheme
import indi.dmzz_yyhyy.lightnovelreader.utils.update


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            AppCenter.start(
                application,
                update("eNpb85aBtYRBJc3c3MTYwshAN808JVnXxNIiTTfJ2DBFNzXZ1MDYKMkgxcwsBQAG3Aux").toString(),
                Analytics::class.java,
                Crashes::class.java
            )
        }
        installSplashScreen()
        setContent {
            LightNovelReaderTheme {
                LightNovelReaderApp()
            }
        }
    }
}