package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderRegressionTestCase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReaderScrollContinuityScenarios : ReaderRegressionTestCase() {
    @Test fun horizontalChapterWindowUpdatesKeepTheVisiblePosition() = verify("Horizontal")
    @Test fun verticalRtlChapterWindowUpdatesKeepTheVisiblePosition() = verify("Vertical, right to left")
    @Test fun verticalLtrChapterWindowUpdatesKeepTheVisiblePosition() = verify("Vertical, left to right")

    private fun verify(direction: String) {
        fixtureAction("PREPARE_READER_LAYOUT", "reader-layout=SUCCEEDED")
        openChapter()
        revealReaderChrome()
        tapDescription("setting")
        expandReaderSettingsSheet()
        tapScrolledText("Writing Direction")
        tapClickableText(direction)
        dismissReaderSettingsSheet()
        hideReaderChrome()

        var entered = false
        for (turn in 0 until 80) {
            if (isCurrentChapter("Benchmark Chapter Two")) {
                entered = true
                break
            }
            pan(direction, forward = true)
        }
        assertTrue("Scrolling did not enter the next chapter", entered)
        val anchor = device.findObjects(By.textStartsWith("Benchmark chapter two progress paragraph"))
            .firstOrNull { it.visibleBounds.width() > 0 && it.visibleBounds.height() > 0 }
        assertTrue("Next chapter entered without visible body", anchor != null)
        val number = Regex("paragraph (\\d+)").find(anchor!!.text)!!.groupValues[1].toInt()
        assertTrue("Crossing the chapter skipped to paragraph $number", number <= 3)
        val text = anchor.text
        val bounds = Rect(anchor.visibleBounds)
        fixtureAction("REEMIT_CHAPTER", "reemit=SUCCEEDED")
        device.waitForIdle()
        val after = visibleText(text).visibleBounds
        assertTrue("Chapter emission moved the visible text: $bounds -> $after",
            abs(bounds.left - after.left) <= 2 && abs(bounds.top - after.top) <= 2)

        var returned = false
        for (turn in 0 until 8) {
            if (isCurrentChapter("Benchmark Chapter One")) {
                returned = true
                break
            }
            pan(direction, forward = false)
        }
        assertTrue("Scrolling back did not return to the previous chapter", returned)
        waitUntil("Returning to the previous chapter did not save its end position") {
            val report = fixtureReport("REPORT_READING_POSITION")
            val progress = Regex("progress=([0-9.]+)").find(report)?.groupValues?.get(1)?.toFloat() ?: -1f
            "chapter=benchmark-chapter-1;" in report && progress > 0.8f
        }
    }

    private fun isCurrentChapter(title: String): Boolean = try {
        val indicator = device.findObject(By.desc("chapter indicator"))
        indicator != null && (indicator.text == title || indicator.findObject(By.text(title)) != null)
    } catch (_: StaleObjectException) {
        false
    }

    private fun pan(direction: String, forward: Boolean) {
        val horizontal = direction == "Horizontal"
        val positive = direction == "Vertical, right to left"
        // A short, slow drag crosses the seam without a fling skipping visible paragraphs.
        val start = if (forward != positive) 3 else 2
        val end = 5 - start
        device.swipe(
            if (horizontal) device.displayWidth / 2 else device.displayWidth * start / 5,
            if (horizontal) device.displayHeight * start / 5 else device.displayHeight / 2,
            if (horizontal) device.displayWidth / 2 else device.displayWidth * end / 5,
            if (horizontal) device.displayHeight * end / 5 else device.displayHeight / 2,
            80,
        )
        device.waitForIdle()
    }
}
