package indi.dmzz_yyhyy.lightnovelreader.benchmark.settings

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class SettingsNavigationScenarios : BenchmarkTestCase() {
    @Test
    fun extensionSourcePluginManagerAndPluginAppsOpen() {
        openSettings()
        tapText("Data Source")
        visibleText("Data Source")
        assertForegroundPackage(TARGET_PACKAGE)
        pressBack()

        tapText("Plugins")
        visibleText("Plugins")
        visibleDescription("install")
        tapDescription("plugin apps")
        visibleText("Plugin apps")
        pressBack()
        pressBack()
        visibleText("Settings")
    }

    @Test
    fun readerStyleFormattingRulesAndConversionAreReachable() {
        openSettings()
        tapText("App Theme")
        listOf("App Theme", "Dynamic Colors", "Light Theme", "Dark Theme")
            .forEach(::visibleText)
        restartApp()
        openRoot("Settings")

        tapClickableText("Text Formatting")
        visibleText("Global Rules")
        tapText("Global Rules")
        SystemClock.sleep(750)
        visibleDescription("add")
        pressBack()
        pressBack()
        tapText("Traditional Chinese Conversion")
        visibleText("Traditional Chinese Conversion")
    }

    @Test
    fun languageSystemPageAndFormatsScreenOpenAndReturn() {
        openSettings()
        tapScrolledText("Language")
        assertForegroundPackage("com.android.settings")
        pressBack()
        assertForegroundPackage(TARGET_PACKAGE)

        openRoot("Settings")
        tapScrolledText("Formats")
        listOf("Formats", "Date Format", "Use Relative Time", "Chinese Characters Variant")
            .forEach(::visibleText)
    }

    @Test
    fun updateMenusAndManualCheckAreReachable() {
        openSettings()
        tapScrolledText("Auto Check for Updates")
        visibleText("Auto Check for Updates")
        tapScrolledText("Test Update Channel")
        visibleText("Beta Version")
        pressBack()
        tapScrolledText("Distribution Platform")
        pressBack()
        tapScrolledText("Check for Updates")
        assertForegroundPackage(TARGET_PACKAGE)
    }

    @Test
    fun snapshotImportStorageAndProxyControlsOpen() {
        openSettings()
        tapScrolledText("Snapshot User Data")
        listOf("Select the data to export", "Local Book Cache", "Bookshelf", "Reading Data")
            .forEach(::visibleText)
        pressBack()

        tapScrolledText("Import User Data")
        assertForegroundPackage(DOCUMENTS_PACKAGE)
        pressBack()
        launchApp()
        openRoot("Settings")
        tapScrolledText("Storage Usage")
        visibleText("Storage Manager")
        visibleTextContaining("Database")
        pressBack()
        tapScrolledText("Auto Proxy")
        visibleText("Auto Proxy")
    }

    @Test
    fun diagnosticsAboutStatisticsAndLicensesOpen() {
        openSettings()
        tapScrolledText("App Logs")
        visibleText("Logs")
        visibleDescription("more")
        pressBack()
        tapScrolledText("Log Level")
        visibleText("Debug")
        pressBack()
        tapScrolledText("LightNovelReader")
        visibleTextContaining("indi.dmzz_yyhyy.lightnovelreader")
        pressBack()
        tapScrolledText("Statistics")
        visibleText("Hold on…")
        pressBack()
        tapScrolledText("Open-source licenses")
        visibleText("Open-source licenses")
        visibleDescription("back")
    }
}
