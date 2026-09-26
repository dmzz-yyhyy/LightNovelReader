package indi.dmzz_yyhyy.lightnovelreader.benchmark.settings

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class SettingsMutationScenarios : BenchmarkTestCase() {
    @Test
    fun globalFormattingRuleCompletesCrudAndRegexValidation() {
        openSettings()
        tapText("Text Formatting")
        tapText("Global Rules")
        SystemClock.sleep(300)
        tapDescription("add")
        visibleText("Edit Rule")
        replaceText(0, "Automation Rule")
        replaceText(1, "Benchmark")
        replaceText(2, "Verified")
        tapText("Save rule")

        pressBack()
        tapText("Global Rules")
        visibleText("Automation Rule")
        visibleText("Benchmark")
        visibleText("Verified")
        assertCheckable(checked = true)

        restartApp()
        visibleText("Verified Sample Novel")
        openPrimaryChapter("Verified Sample Novel", "Verified Chapter One")
        visibleTextContaining("Verified progress paragraph")
        assertTextNotVisible("Benchmark progress paragraph")

        restartApp()
        openRoot("Settings")
        tapText("Text Formatting")
        tapText("Global Rules")
        visibleText("Automation Rule")
        SystemClock.sleep(750)
        tapClickableText("Automation Rule")
        visibleText("Edit Rule")
        tapSwitchForText("Use regex matching")
        awaitSwitchForText("Use regex matching", checked = true)
        replaceText(1, "[")
        visibleText("Edit Rule")
        tapText("Save rule")
        visibleText("Edit Rule")
        awaitFixtureReport(
            action = "REPORT_FORMATTING_RULES",
            expectedFragment = "regex=false;match=Benchmark;replacement=Verified;enabled=true",
        )
        replaceText(1, "Benchmark")
        tapText("Save rule")
        awaitFixtureReport(
            action = "REPORT_FORMATTING_RULES",
            expectedFragment = "regex=true;match=Benchmark;replacement=Verified;enabled=true",
        )

        restartApp()
        openPrimaryChapter("Verified Sample Novel", "Verified Chapter One")
        visibleTextContaining("Verified progress paragraph")
        assertTextNotVisible("Benchmark progress paragraph")

        restartApp()
        openRoot("Settings")
        tapText("Text Formatting")
        tapText("Global Rules")
        visibleText("Automation Rule")
        SystemClock.sleep(750)
        tapClickableText("Automation Rule")
        visibleText("Edit Rule")
        tapText("Delete rule")
        restartApp()
        openPrimaryChapter(PRIMARY_BOOK, "Benchmark Chapter One")
        visibleTextContaining("Benchmark progress paragraph")
    }

    @Test
    fun snapshotDialogShowsEveryCategoryAndFileDestination() {
        openSettings()
        tapScrolledText("Snapshot User Data")
        listOf("Local Book Cache", "Bookshelf", "Reading Data", "Settings")
            .forEach(::visibleText)
        scrollToText("Share")
        tapScrolledText("Export to File")
        assertForegroundPackage(DOCUMENTS_PACKAGE)
    }

    @Test
    fun updatePlatformChannelAndAutoCheckPersist() {
        openSettings()
        tapScrolledText("Distribution Platform")
        visibleText("GitHub")
        visibleText("LightNovelReader API")
        tapClickableText("GitHub")
        awaitUserData(UPDATE_PLATFORM_PATH, "GitHub")
        tapScrolledText("Test Update Channel")
        listOf("None (Stable)", "Beta Version", "Alpha Version (Unstable)")
            .forEach(::visibleText)
        tapClickableText("Alpha Version (Unstable)")
        awaitUserData(UPDATE_CHANNEL_PATH, "CI")
        scrollToText("Auto Check for Updates")
        if (!isSwitchCheckedForText("Auto Check for Updates")) {
            tapSwitchForText("Auto Check for Updates")
            awaitUserData(AUTO_CHECK_PATH, "true")
        }
        tapSwitchForText("Auto Check for Updates")
        awaitUserData(AUTO_CHECK_PATH, "false")

        restartApp()
        assertEquals("GitHub", userData(UPDATE_PLATFORM_PATH))
        assertEquals("CI", userData(UPDATE_CHANNEL_PATH))
        assertEquals("false", userData(AUTO_CHECK_PATH))
        openRoot("Settings")
        scrollToText("Auto Check for Updates")
        awaitSwitchForText("Auto Check for Updates", checked = false)
        tapSwitchForText("Auto Check for Updates")
        awaitUserData(AUTO_CHECK_PATH, "true")
    }

    @Test
    fun logMenusAndStatisticsDisableConfirmationWork() {
        openSettings()
        tapScrolledText("Log Level")
        tapClickableText("Debug")
        awaitUserData(LOG_LEVEL_PATH, "debug")
        tapScrolledText("App Logs")
        tapDescription("more")
        listOf("Clear Temporary Logs", "Auto-scroll", "Word Wrap").forEach(::visibleText)
        pressBack()
        pressBack()
        tapScrolledText("Statistics")
        visibleText("Hold on…")
        visibleText("Turn off")
        tapText("Turn off")
        visibleText("Settings")
        awaitUserData(STATISTICS_PATH, "false")

        restartApp()
        assertEquals("debug", userData(LOG_LEVEL_PATH))
        assertEquals("false", userData(STATISTICS_PATH))
        openRoot("Settings")
        tapScrolledText("Log Level")
        visibleText("Debug")
    }

    @Test
    fun autoProxyPersistsAndRestoresItsSwitchState() {
        openSettings()
        scrollToText("Auto Proxy")
        assertSwitchForText("Auto Proxy", checked = false)
        tapSwitchForText("Auto Proxy")
        awaitUserData(AUTO_PROXY_PATH, "true")

        restartApp()
        assertEquals("true", userData(AUTO_PROXY_PATH))
        openRoot("Settings")
        scrollToText("Auto Proxy")
        awaitSwitchForText("Auto Proxy", checked = true)
        tapSwitchForText("Auto Proxy")
        awaitUserData(AUTO_PROXY_PATH, "false")
    }

    private fun openPrimaryChapter(bookTitle: String, chapterTitle: String) {
        openRoot("Bookshelf")
        tapScrolledText(bookTitle)
        tapScrolledText(chapterTitle)
    }

    companion object {
        private const val UPDATE_PLATFORM_PATH = "settings.app.update_platform"
        private const val UPDATE_CHANNEL_PATH = "settings.app.update_channel"
        private const val AUTO_CHECK_PATH = "settings.app.auto_check_update"
        private const val LOG_LEVEL_PATH = "settings.data.log_level"
        private const val STATISTICS_PATH = "settings.app.statistics"
        private const val AUTO_PROXY_PATH = "settings.data.is_use_proxy"
    }
}
