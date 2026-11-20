package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderRegressionTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReaderWritingDirectionScenarios : ReaderRegressionTestCase() {
    @Test fun horizontalScrollAndPagesRenderAndRestore() = exerciseDirection("Horizontal", "Horizontal")

    @Test fun verticalRightToLeftScrollAndPagesRenderAndRestore() =
        exerciseDirection("VerticalRTL", "Vertical, right to left")

    @Test fun verticalLeftToRightScrollAndPagesRenderAndRestore() =
        exerciseDirection("VerticalLTR", "Vertical, left to right")

    private fun exerciseDirection(mode: String, label: String) {
        fixtureAction("PREPARE_READER_LAYOUT", "reader-layout=SUCCEEDED")
        openChapter()
        revealReaderChrome()
        tapDescription("setting")
        expandReaderSettingsSheet()
        tapScrolledText("Writing Direction")
        tapClickableText(label)
        awaitUserData("reader.write_mode", mode)
        dismissReaderSettingsSheet()
        hideReaderChrome()
        assertHeadingOrder(mode)
        val before = savedProgress()
        scrollForward(mode)
        val progress = waitForProgressAfter(before)
        assertTrue("$mode scroll did not advance progress", progress > before)

        restartApp()
        tapText("Resume Last Reading")
        visibleTextContaining("Benchmark progress paragraph")
        assertEquals(mode, userData("reader.write_mode"))
        assertEquals(progress, savedProgress())

        enablePageMode(tapZones = true, noAnimation = true)
        val page = flipPage()
        clickPage(mode, forward = true)
        val advanced = waitForDifferentPage(page)
        assertNotEquals("$mode page did not turn", page, advanced)
        assertVisibleBody()
        restartApp()
        tapText("Resume Last Reading")
        assertEquals(advanced, waitForPage(advanced))
        assertEquals(mode, userData("reader.write_mode"))
        clickPage(mode, forward = false)
        assertEquals(page, waitForPage(page))

        // Check every page to the next chapter, including the formerly blank terminal page.
        var last = flipPage()
        var turns = 0
        while (flipChapter().endsWith("flip-chapter-benchmark-chapter-1") && turns++ < 80) {
            assertVisibleBody()
            last = flipPage()
            clickPage(mode, forward = true)
            assertNotEquals("$mode paging stalled before the next chapter", last, waitForDifferentPage(last))
        }
        assertTrue("$mode did not enter the next chapter", flipChapter().endsWith("flip-chapter-benchmark-chapter-2"))
        visibleText("Benchmark Chapter Two")
        visibleTextContaining("Benchmark chapter two start marker")
        clickPage(mode, forward = false)
        assertEquals("$mode boundary return changed the previous page", last, waitForPage(last))
        visibleTextContaining("Benchmark chapter one end marker")
    }

    private fun assertHeadingOrder(mode: String) {
        val heading = visibleText("Benchmark Chapter One").visibleBounds
        val body = visibleTextContaining("Benchmark progress paragraph 1.").visibleBounds
        assertTrue("Chapter heading is outside the reader", heading.width() > 0 && heading.height() > 0)
        when (mode) {
            "Horizontal" -> assertTrue("Horizontal heading should precede body vertically", heading.centerY() < body.centerY())
            "VerticalRTL" -> assertTrue("RTL heading should precede body on the right", heading.centerX() > body.centerX())
            else -> assertTrue("LTR heading should precede body on the left", heading.centerX() < body.centerX())
        }
    }

    private fun assertVisibleBody() {
        waitUntil("Resolved page contains no chapter text: ${flipState()}") {
            device.findObjects(By.textContains("Benchmark")).any {
                val bounds = it.visibleBounds
                bounds.width() > 0 && bounds.height() > 0 && bounds.top < device.displayHeight * 4 / 5
            }
        }
    }

    private fun scrollForward(mode: String) {
        if (mode == "Horizontal") scrollReader(1) else {
            val rtl = mode == "VerticalRTL"
            device.swipe(
                device.displayWidth * (if (rtl) 1 else 4) / 5,
                device.displayHeight / 2,
                device.displayWidth * (if (rtl) 4 else 1) / 5,
                device.displayHeight / 2,
                35,
            )
            device.waitForIdle()
        }
    }

    private fun clickPage(mode: String, forward: Boolean) {
        tapPage(forward = if (mode == "VerticalRTL") !forward else forward)
    }
}
