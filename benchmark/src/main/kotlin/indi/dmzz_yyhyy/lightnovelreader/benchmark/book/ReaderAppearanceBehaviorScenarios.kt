package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReaderAppearanceBehaviorScenarios : ReaderTestCase() {
    @Test
    fun indicatorSettingsChangeReaderOutputAndSurviveRestart() {
        openChapter()
        listOf("battery indicator", "time indicator", "chapter indicator", "progress indicator")
            .forEach(::visibleDescription)

        openReaderSettings()
        tapScrolledText("Battery Display Mode")
        tapText("Hidden")
        awaitUserData(BATTERY_PATH, "hidden")
        disableAppearanceSwitch("Time Indicator", TIME_PATH)
        disableAppearanceSwitch("Chapter Indicator", CHAPTER_PATH)
        disableAppearanceSwitch("Progress Indicator", PROGRESS_PATH)
        closeReaderSettings()
        hideReaderChrome()
        listOf("battery indicator", "time indicator", "chapter indicator", "progress indicator")
            .forEach(::assertDescriptionNotVisible)

        restartApp()
        tapText("Resume Last Reading")
        listOf("battery indicator", "time indicator", "chapter indicator", "progress indicator")
            .forEach(::assertDescriptionNotVisible)

        openReaderSettings()
        tapScrolledText("Battery Display Mode")
        tapText("Classic")
        awaitUserData(BATTERY_PATH, "classic")
        closeReaderSettings()
        hideReaderChrome()
        visibleDescription("battery indicator")
        listOf("time indicator", "chapter indicator", "progress indicator")
            .forEach(::assertDescriptionNotVisible)

        restartApp()
        tapText("Resume Last Reading")
        hideReaderChrome()
        visibleDescription("battery indicator")
        listOf("time indicator", "chapter indicator", "progress indicator")
            .forEach(::assertDescriptionNotVisible)
        assertEquals("classic", userData(BATTERY_PATH))
    }

    @Test
    fun keepAwakeAndStatusBarSwitchesChangeTheWindow() {
        openChapter()
        openReaderSettings()
        tapSwitchForText("Keep Screen On")
        awaitUserData(KEEP_AWAKE_PATH, "true")
        closeReaderSettings()
        hideReaderChrome()
        waitUntil("Reader window never acquired KEEP_SCREEN_ON") {
            "KEEP_SCREEN_ON" in shell("dumpsys window windows")
        }

        openReaderSettings()
        tapSwitchForText("Hide Status Bar")
        awaitUserData(HIDE_STATUS_PATH, "false")
        closeReaderSettings()
        hideReaderChrome()
        waitUntil("Status bar did not become visible") {
            device.hasObject(By.res("com.android.systemui", "clock"))
        }

        openReaderSettings()
        tapSwitchForText("Keep Screen On")
        tapSwitchForText("Hide Status Bar")
        awaitUserData(KEEP_AWAKE_PATH, "false")
        awaitUserData(HIDE_STATUS_PATH, "true")
        closeReaderSettings()
        hideReaderChrome()
        waitUntil("Reader window retained KEEP_SCREEN_ON after disabling it") {
            "KEEP_SCREEN_ON" !in shell("dumpsys window windows")
        }
        assertFalse(device.hasObject(By.res("com.android.systemui", "clock")))
    }

    private fun openReaderSettings() {
        revealReaderChrome()
        tapDescription("setting")
        expandReaderSettingsSheet()
    }

    private fun closeReaderSettings() {
        repeat(2) {
            if (!device.hasObject(By.text("Reader Settings"))) return
            pressBack()
        }
        assertTextNotVisible("Reader Settings")
    }

    private fun disableAppearanceSwitch(label: String, path: String) {
        scrollToText(label)
        assertSwitchForText(label, checked = true)
        tapSwitchForText(label)
        awaitUserData(path, "false")
    }

    companion object {
        private const val KEEP_AWAKE_PATH = "reader.keep_screen_on"
        private const val HIDE_STATUS_PATH = "reader.enable_hide_status_bar"
        private const val BATTERY_PATH = "reader.battery_indicator_display_mode"
        private const val TIME_PATH = "reader.enable_time_indicator"
        private const val CHAPTER_PATH = "reader.enable_chapter_title_indicator"
        private const val PROGRESS_PATH = "reader.enable_reading_chapter_progress_indicator"
    }
}
