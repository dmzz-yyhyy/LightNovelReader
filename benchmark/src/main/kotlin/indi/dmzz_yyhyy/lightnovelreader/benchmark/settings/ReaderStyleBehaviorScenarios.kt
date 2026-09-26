package indi.dmzz_yyhyy.lightnovelreader.benchmark.settings

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReaderStyleBehaviorScenarios : BenchmarkTestCase() {
    @Test
    fun backgroundModeAndColorsPersistAndUpdatePreview() {
        openReaderStyle()
        scrollToText("Background Image")
        tapSwitchForText("Background Image")
        awaitUserData(BACKGROUND_IMAGE_PATH, "true")
        visibleText("Background Display Mode")
        assertTextNotVisible("Background Color")
        tapText("Background Display Mode")
        tapText("Loop")
        awaitUserData(BACKGROUND_MODE_PATH, "loop")

        restartApp()
        openRoot("Settings")
        tapScrolledText("Reader Style")
        scrollToText("Background Image")
        assertSwitchForText("Background Image", checked = true)
        visibleText("Loop")
        tapSwitchForText("Background Image")
        awaitUserData(BACKGROUND_IMAGE_PATH, "false")

        val preview = scrollToText(PREVIEW_TEXT)
        val beforeBackground = previewAverage(preview.visibleBounds)
        tapScrolledText("Background Color")
        tapDescription("color option 2")
        tapText("Apply")
        val (backgroundPath, backgroundValue) = awaitStoredValue(
            BACKGROUND_COLOR_PATH,
            BACKGROUND_DARK_COLOR_PATH,
        )
        val afterBackground = previewAverage(scrollToText(PREVIEW_TEXT).visibleBounds)
        assertNotEquals("Background color did not change the preview", beforeBackground, afterBackground)

        tapScrolledText("Text Color")
        tapDescription("color option 2")
        tapText("Apply")
        val (textPath, textValue) = awaitStoredValue(TEXT_COLOR_PATH, TEXT_DARK_COLOR_PATH)
        val afterText = previewAverage(scrollToText(PREVIEW_TEXT).visibleBounds)
        assertNotEquals("Text color did not change the preview", afterBackground, afterText)

        restartApp()
        assertEquals(backgroundValue, userData(backgroundPath))
        assertEquals(textValue, userData(textPath))
    }

    @Test
    fun typographySlidersPersistAndFontSizeChangesPreviewLayout() {
        openReaderStyle()
        val initialHeight = scrollToText(PREVIEW_TEXT).visibleBounds.height()
        val values = linkedMapOf(
            FONT_WEIGHT_PATH to Pair("Font Weight", 0.85f),
            FONT_SIZE_PATH to Pair("Font Size", 0.85f),
            LINE_HEIGHT_PATH to Pair("Line Height", 0.80f),
            PARAGRAPH_SPACING_PATH to Pair("Paragraph Spacing", 0.75f),
            TEXT_INDENT_PATH to Pair("Text Indent", 0.75f),
        )
        val stored = values.mapValues { (path, setting) ->
            setSliderForText(setting.first, setting.second)
            awaitStoredValue(path)
        }
        val changedHeight = scrollToText(PREVIEW_TEXT).visibleBounds.height()
        assertTrue(
            "Font size did not change preview layout: $initialHeight -> $changedHeight",
            changedHeight > initialHeight,
        )

        restartApp()
        stored.forEach { (path, value) -> assertEquals(value, userData(path)) }
    }

    private fun openReaderStyle() {
        openSettings()
        tapScrolledText("Reader Style")
        visibleText("Reader Style")
    }

    private fun previewAverage(textBounds: Rect): Triple<Int, Int, Int> = averageScreenColor(
        Rect(
            0,
            (textBounds.top - 24).coerceAtLeast(0),
            device.displayWidth,
            (textBounds.bottom + 24).coerceAtMost(device.displayHeight),
        )
    )

    private fun awaitStoredValue(path: String): String {
        var value = "<null>"
        waitUntil("User data '$path' was not stored") {
            value = userData(path)
            value != "<null>"
        }
        return value
    }

    private fun awaitStoredValue(vararg paths: String): Pair<String, String> {
        var result: Pair<String, String>? = null
        waitUntil("None of ${paths.toList()} was stored") {
            result = paths.firstNotNullOfOrNull { path ->
                userData(path).takeUnless { it == "<null>" }?.let { path to it }
            }
            result != null
        }
        return result ?: error("No color value was stored")
    }

    companion object {
        private const val PREVIEW_TEXT = "LNR是一款作者为了能在每个深夜得到安慰所开发出的软件。"
        private const val BACKGROUND_IMAGE_PATH = "reader.enable_background_image"
        private const val BACKGROUND_MODE_PATH = "reader.background_image_display_mode"
        private const val BACKGROUND_COLOR_PATH = "reader.background_color"
        private const val BACKGROUND_DARK_COLOR_PATH = "reader.background_dark_color"
        private const val TEXT_COLOR_PATH = "reader.text_color"
        private const val TEXT_DARK_COLOR_PATH = "reader.text_dark_color"
        private const val FONT_WEIGHT_PATH = "reader.font_weigh"
        private const val FONT_SIZE_PATH = "reader.font_size"
        private const val LINE_HEIGHT_PATH = "reader.line_height_new"
        private const val PARAGRAPH_SPACING_PATH = "reader.spacing_after_paragraph"
        private const val TEXT_INDENT_PATH = "reader.first_line_text_indent"
    }
}
