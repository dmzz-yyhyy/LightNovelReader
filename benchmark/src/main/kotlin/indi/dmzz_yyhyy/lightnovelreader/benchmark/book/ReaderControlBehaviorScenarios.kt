package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReaderControlBehaviorScenarios : ReaderTestCase() {
    @Test
    fun backPreventionModesChangeNavigationBehavior() {
        openChapter()
        openControlSettings()
        tapText("Back Prevention")
        tapText("Double Press")
        awaitUserData(BACK_MODE_PATH, "double_press")
        closeReaderSettings()
        hideReaderChrome()

        pressBack()
        visibleText("Press back again to exit reading")
        assertForegroundPackage(TARGET_PACKAGE)
        pressBack()
        visibleText(PRIMARY_BOOK)

        tapScrolledText("Benchmark Chapter One")
        visibleTextContaining("Benchmark progress paragraph")
        openControlSettings()
        tapText("Back Prevention")
        tapText("Fully Blocked")
        awaitUserData(BACK_MODE_PATH, "blocked")
        pressBack()
        hideReaderChrome()
        pressBack()
        visibleTextContaining("Benchmark progress paragraph")
        assertForegroundPackage(TARGET_PACKAGE)

        restartApp()
        assertEquals("blocked", userData(BACK_MODE_PATH))
        tapText("Resume Last Reading")
        pressBack()
        visibleTextContaining("Benchmark progress paragraph")
    }

    @Test
    fun manualMarginsMoveContentAndEveryValueSurvivesRestart() {
        openChapter()
        val initialLeft = visibleTextContaining("Benchmark progress paragraph").visibleBounds.left
        revealReaderChrome()
        tapDescription("setting")
        expandReaderSettingsSheet()
        tapText("Margins")
        tapSwitchForText("Auto Margin Adjustment")
        awaitUserData(AUTO_MARGIN_PATH, "false")
        waitUntil("Manual margin controls did not appear after disabling automatic margins") {
            device.hasObject(By.desc("Top Margin slider"))
        }
        bringReaderSettingIntoView("Top Margin")

        val settings = linkedMapOf(
            LEFT_MARGIN_PATH to Pair("Left Margin", 0.45f),
            RIGHT_MARGIN_PATH to Pair("Right Margin", 0.45f),
            TOP_MARGIN_PATH to Pair("Top Margin", 0.25f),
            BOTTOM_MARGIN_PATH to Pair("Bottom Margin", 0.25f),
        )
        val initialValues = settings.keys.associateWith(::userData)
        val stored = mutableMapOf<String, String>()
        settings.entries.take(2).forEach { (path, setting) ->
            bringReaderSettingIntoView(setting.first)
            setSliderForText(setting.first, setting.second)
            stored[path] = awaitChangedValue(path, initialValues.getValue(path))
        }
        closeReaderSettings()
        hideReaderChrome()
        val changedLeft = visibleTextContaining("Benchmark progress paragraph").visibleBounds.left
        assertTrue("Left margin did not move reader content: $initialLeft -> $changedLeft", changedLeft > initialLeft + 30)

        revealReaderChrome()
        tapDescription("setting")
        expandReaderSettingsSheet()
        tapText("Margins")
        settings.entries.drop(2).forEach { (path, setting) ->
            bringReaderSettingIntoView(setting.first)
            setSliderForText(setting.first, setting.second)
            stored[path] = awaitChangedValue(path, initialValues.getValue(path))
        }
        closeReaderSettings()

        restartApp()
        stored.forEach { (path, value) -> assertEquals(value, userData(path)) }
        openRoot("Bookshelf")
        tapScrolledText(PRIMARY_BOOK)
        tapScrolledText("Benchmark Chapter One")
        val restoredLeft = visibleTextContaining("Benchmark progress paragraph").visibleBounds.left
        assertTrue("Manual margin was not applied after restart", restoredLeft > initialLeft + 30)
    }

    private fun openControlSettings() {
        revealReaderChrome()
        tapDescription("setting")
        visibleText("Reader Settings")
        tapText("Controls")
    }

    private fun closeReaderSettings() {
        repeat(2) {
            if (!device.hasObject(By.text("Reader Settings"))) return
            pressBack()
        }
        assertTextNotVisible("Reader Settings")
    }

    private fun bringReaderSettingIntoView(label: String) {
        val bounds = visibleText(label).visibleBounds
        val targetY = (device.displayHeight * 0.70f).toInt()
        if (bounds.centerY() > targetY) {
            device.swipe(bounds.centerX(), bounds.centerY(), bounds.centerX(), targetY, 60)
            device.waitForIdle()
        }
    }

    private fun awaitChangedValue(path: String, previousValue: String): String {
        var value = previousValue
        waitUntil("User data '$path' did not change from '$previousValue'") {
            value = userData(path)
            value != previousValue
        }
        return value
    }

    companion object {
        private const val BACK_MODE_PATH = "reader.back_block_mode"
        private const val AUTO_MARGIN_PATH = "reader.auto_padding"
        private const val TOP_MARGIN_PATH = "reader.top_padding"
        private const val BOTTOM_MARGIN_PATH = "reader.bottom_padding"
        private const val LEFT_MARGIN_PATH = "reader.left_padding"
        private const val RIGHT_MARGIN_PATH = "reader.right_padding"
    }
}
