package indi.dmzz_yyhyy.lightnovelreader.benchmark.system

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.ui.UiAutomatorTest
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ExternalIntentTest : UiAutomatorTest() {
    @Test
    fun pluginStoreDeepLinkIsHandledInsideApp() {
        launchApp()
        shell(
            "am start -W -a android.intent.action.VIEW " +
                    "-d 'lightnovelreader://install_plugin?id=benchmark.invalid' " +
                    TARGET_PACKAGE
        )
        assertForegroundPackage(TARGET_PACKAGE)
    }

    @Test
    fun arbitraryPluginFileIntentOpensInstallerFlow() {
        launchApp()
        shell(
            "am start -W -a android.intent.action.VIEW " +
                    "-d 'content://benchmark.invalid/plugin.apk' " +
                    "-t application/vnd.android.package-archive $TARGET_PACKAGE"
        )
        assertForegroundPackage(TARGET_PACKAGE)
    }
}
