package indi.dmzz_yyhyy.lightnovelreader.benchmark.settings

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@LargeTest
@RunWith(AndroidJUnit4::class)
class DisplaySettingsScenarios : BenchmarkTestCase() {
    @Test
    fun traditionalConversionChangesReaderTextAfterRestart() {
        openSettings()
        scrollToText("Traditional Chinese Conversion")
        assertSwitchForText("Traditional Chinese Conversion", checked = false)
        tapSwitchForText("Traditional Chinese Conversion")
        awaitUserData(TRADITIONAL_CONVERSION_PATH, "true")

        restartApp()
        assertEquals("true", userData(TRADITIONAL_CONVERSION_PATH))
        tapClickableText("Resume Last Reading")
        visibleTextContaining("漢語閱讀測試")
        assertTextNotVisible("汉语阅读测试")
    }

    @Test
    fun everyDateFormatControlPersistsAndUpdatesItsExample() {
        openSettings()
        tapScrolledText("Formats")
        tapText("Date Order")
        tapText("Day-Month-Year")
        awaitUserData(DATE_ORDER_PATH, "dmy")
        visibleTextContaining("10/3/2026")

        assertSwitchForText("Show Year", checked = true)
        tapSwitchForText("Show Year")
        awaitUserData(DATE_SHOW_YEAR_PATH, "false")
        visibleTextContaining("10/3")

        tapText("Date Format")
        tapText("Written")
        awaitUserData(DATE_STYLE_PATH, "written")
        visibleTextContaining("March 10")

        assertSwitchForText("Use Relative Time", checked = true)
        tapSwitchForText("Use Relative Time")
        awaitUserData(RELATIVE_TIME_PATH, "false")

        tapText("Chinese Characters Variant")
        tapText("繁體中文 (台灣)")
        awaitUserData(APP_LOCALE_PATH, "zh-TW")

        restartApp()
        assertEquals("dmy", userData(DATE_ORDER_PATH))
        assertEquals("false", userData(DATE_SHOW_YEAR_PATH))
        assertEquals("written", userData(DATE_STYLE_PATH))
        assertEquals("false", userData(RELATIVE_TIME_PATH))
        assertEquals("zh-TW", userData(APP_LOCALE_PATH))
        openRoot("Settings")
        tapScrolledText("Formats")
        visibleTextContaining("March 10")
        assertSwitchForText("Show Year", checked = false)
        assertSwitchForText("Use Relative Time", checked = false)
    }

    @Test
    fun appThemeSelectionsPersistAndDarkModeChangesRenderedColors() {
        openSettings()
        tapText("App Theme")
        tapText("Light Theme")
        tapText("Designer")
        awaitUserData(LIGHT_THEME_PATH, "light_designer")
        scrollToText("Light Theme")
        tapLastText("Dark Theme")
        tapText("Obsidian")
        awaitUserData(DARK_THEME_PATH, "dark_obsidian")

        tapDescription("Override Disabled")
        awaitUserData(DARK_MODE_PATH, "Disabled")
        val region = Rect(0, 220, device.displayWidth, 520)
        val before = averageScreenColor(region)
        tapDescription("Override Enabled")
        awaitUserData(DARK_MODE_PATH, "Enabled")
        val after = averageScreenColor(region)
        val colorDistance = abs(before.first - after.first) +
            abs(before.second - after.second) + abs(before.third - after.third)
        assertTrue("Dark mode did not change the rendered theme: $before -> $after", colorDistance > 30)

        scrollToText("Dynamic Colors")
        assertSwitchForText("Dynamic Colors", checked = false)
        tapSwitchForText("Dynamic Colors")
        awaitUserData(DYNAMIC_COLOR_PATH, "true")

        restartApp()
        assertEquals("light_designer", userData(LIGHT_THEME_PATH))
        assertEquals("dark_obsidian", userData(DARK_THEME_PATH))
        assertEquals("Enabled", userData(DARK_MODE_PATH))
        assertEquals("true", userData(DYNAMIC_COLOR_PATH))
        openRoot("Settings")
        tapText("App Theme")
        assertTrue(visibleDescription("Override Enabled").isChecked)
        scrollToText("Dynamic Colors")
        assertSwitchForText("Dynamic Colors", checked = true)
    }

    companion object {
        private const val TRADITIONAL_CONVERSION_PATH =
            "reader.enable_simplified_traditional_transform"
        private const val DATE_STYLE_PATH = "settings.display.date_style"
        private const val DATE_SHOW_YEAR_PATH = "settings.display.date_show_year"
        private const val DATE_ORDER_PATH = "settings.display.date_order"
        private const val RELATIVE_TIME_PATH = "settings.display.relative_time_style"
        private const val APP_LOCALE_PATH = "settings.display.app_locale"
        private const val DARK_MODE_PATH = "settings.display.dark_mode"
        private const val DYNAMIC_COLOR_PATH = "settings.display.dynamic_color"
        private const val LIGHT_THEME_PATH = "settings.display.light_theme_name"
        private const val DARK_THEME_PATH = "settings.display.dark_theme_name"
    }
}
