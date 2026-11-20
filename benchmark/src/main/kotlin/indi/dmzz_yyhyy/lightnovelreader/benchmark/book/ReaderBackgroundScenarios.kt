package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.waitForStable
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderRegressionTestCase
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

@LargeTest
@SdkSuppress(minSdkVersion = 29)
@RunWith(AndroidJUnit4::class)
class ReaderBackgroundScenarios : ReaderRegressionTestCase() {
    @After
    fun deleteFixtureImages() {
        fixtureAction("DELETE_BACKGROUND_IMAGES", "background-images=DELETED")
    }

    @Test
    fun importedBackgroundIsVisibleInPreviewScrollAndPagesAfterRestart() {
        fixtureAction("PREPARE_BACKGROUND_IMAGES", "background-images=SUCCEEDED")
        openSettingsHome()
        tapText("App Theme")
        tapDescription("Override Disabled")
        awaitUserData("settings.display.dark_mode", "Disabled")
        pressBack()
        tapScrolledText("Reader Style")
        tapSwitchForText("Background Image")
        awaitUserData("reader.enable_background_image", "true")
        selectBackground("Choose Light Background", "lnr-background-light")
        selectBackground("Choose Dark Background", "lnr-background-dark")
        val light = userData("reader.background_image_uri")
        val dark = userData("reader.background_dark_image_uri")
        assertTrue("Light image was not copied into app storage: $light", light.startsWith("file:"))
        assertTrue("Dark image was not copied into app storage: $dark", dark.startsWith("file:"))

        val preview = scrollToText(PREVIEW_TEXT)
        assertBackground(preview.visibleBounds)
        restartApp()
        openChapter()
        assertBackground(readerBounds())
        scrollReader(1)
        assertBackground(readerBounds())
        enablePageMode(tapZones = true)
        assertBackground(readerBounds())
        val first = flipPage()
        tapPage(forward = true)
        waitForDifferentPage(first)
        assertBackground(readerBounds())
        restartApp()
        tapText("Resume Last Reading")
        waitForPagerReady()
        assertBackground(readerBounds())

        restartApp()
        openSettingsHome()
        tapText("App Theme")
        tapDescription("Override Enabled")
        awaitUserData("settings.display.dark_mode", "Enabled")
        pressBack()
        tapScrolledText("Reader Style")
        assertBackground(scrollToText(PREVIEW_TEXT).visibleBounds, DARK_COLOR)
        restartApp()
        openChapter()
        waitForPagerReady()
        assertBackground(readerBounds(), DARK_COLOR)
    }

    @Test
    fun replacementCancelAndLoopModeUseTheSelectedImage() {
        fixtureAction("PREPARE_BACKGROUND_IMAGES", "background-images=SUCCEEDED")
        openSettingsHome()
        tapText("App Theme")
        tapDescription("Override Disabled")
        awaitUserData("settings.display.dark_mode", "Disabled")
        pressBack()
        tapScrolledText("Reader Style")
        tapSwitchForText("Background Image")
        awaitUserData("reader.enable_background_image", "true")
        selectBackground("Choose Light Background", "lnr-background-light")
        val saved = userData("reader.background_image_uri")
        openImagePicker("Choose Light Background")
        pressBack()
        assertForegroundPackage(TARGET_PACKAGE)
        assertEquals("Canceling selection replaced the imported image", saved, userData("reader.background_image_uri"))
        selectBackground("Choose Light Background", "lnr-background-dark")
        assertBackground(scrollToText(PREVIEW_TEXT).visibleBounds, DARK_COLOR)
        // Return to the top to select loop mode; scrollToText only scrolls forward.
        restartApp()
        openSettingsHome()
        tapScrolledText("Reader Style")
        tapScrolledText("Background Display Mode")
        tapClickableText("Loop")
        awaitUserData("reader.background_image_display_mode", "loop")
        restartApp()
        openChapter()
        assertBackground(readerBounds(), DARK_COLOR)
        scrollReader(1)
        assertBackground(readerBounds(), DARK_COLOR)
        enablePageMode(tapZones = true)
        assertBackground(readerBounds(), DARK_COLOR)
    }

    private fun selectBackground(label: String, fileName: String) {
        val path = if (label == "Choose Light Background") "reader.background_image_uri" else "reader.background_dark_image_uri"
        val before = userData(path)
        openImagePicker(label)
        clickPickerItem(By.descStartsWith(fileName))
        assertTrue("Image picker did not return", device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), UI_TIMEOUT))
        assertForegroundPackage(TARGET_PACKAGE)
        waitUntil("Selected image was not saved") { userData(path).let { it.startsWith("file:") && it != before } }
    }

    private fun openImagePicker(label: String) {
        tapScrolledText(label)
        waitUntil("Image picker did not open") {
            device.hasObject(By.pkg(documentsPackage)) || device.hasObject(By.desc("More"))
        }
        if (!device.hasObject(By.pkg(documentsPackage))) {
            tapClickable(visibleDescription("More"))
            tapClickable(visibleTextContaining("Browse"))
        }
        assertTrue("Image picker did not open", device.wait(Until.hasObject(By.pkg(documentsPackage)), UI_TIMEOUT))
        clickPickerItem(By.desc("Show roots"))
        clickPickerItem(By.res("android", "title").text("Recent"))
    }

    private fun clickPickerItem(selector: BySelector) {
        repeat(3) {
            try {
                val target = device.wait(Until.findObject(selector), UI_TIMEOUT)
                    ?: error("Picker item was not available: $selector")
                assertTrue("Picker item kept moving: $selector", !target.waitForStable(
                    stableIntervalMs = 250,
                    requireStableScreenshot = false,
                ).isTimeout)
                target.click()
                device.waitForIdle()
                return
            } catch (_: StaleObjectException) {
                // DocumentsUI replaces toolbar/list nodes when the provider finishes loading.
            }
        }
        error("Picker item kept changing: $selector")
    }

    private fun readerBounds() = Rect(0, device.displayHeight / 5, device.displayWidth, device.displayHeight * 4 / 5)

    private fun assertBackground(bounds: Rect, expected: Int = LIGHT_COLOR) {
        waitUntil("Imported image did not cover the reader/preview") {
            val file = File(InstrumentationRegistry.getInstrumentation().context.cacheDir, "reader-background.png")
            check(device.takeScreenshot(file))
            val bitmap = checkNotNull(BitmapFactory.decodeFile(file.path))
            try {
                var matching = 0
                var total = 0
                for (y in bounds.top.coerceAtLeast(0) until bounds.bottom.coerceAtMost(bitmap.height) step 8) {
                    for (x in bounds.left.coerceAtLeast(0) until bounds.right.coerceAtMost(bitmap.width) step 8) {
                        val pixel = bitmap.getPixel(x, y)
                        if (abs(Color.red(pixel) - Color.red(expected)) < 12 &&
                                abs(Color.green(pixel) - Color.green(expected)) < 12 &&
                                abs(Color.blue(pixel) - Color.blue(expected)) < 12
                        ) matching++
                        total++
                    }
                }
                total > 0 && matching.toFloat() / total > 0.65f
            } finally {
                bitmap.recycle()
                file.delete()
            }
        }
    }

    companion object {
        private const val PREVIEW_TEXT = "LNR是一款作者为了能在每个深夜得到安慰所开发出的软件。"
        private val LIGHT_COLOR = Color.rgb(24, 188, 104)
        private val DARK_COLOR = Color.rgb(164, 48, 196)
    }
}
