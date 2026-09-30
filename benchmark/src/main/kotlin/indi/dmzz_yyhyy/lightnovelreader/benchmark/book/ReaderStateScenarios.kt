package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReaderStateScenarios : ReaderTestCase() {
    @Test
    fun appearanceToggleSurvivesReaderReentry() {
        openChapter()
        revealReaderChrome()
        tapDescription("setting")
        tapSwitchForText("Keep Screen On")
        assertSwitchForText("Keep Screen On", checked = true)
        pressBack()
        pressBack()

        tapScrolledText("Benchmark Chapter One")
        visibleTextContaining("Benchmark progress paragraph")
        revealReaderChrome()
        tapDescription("setting")
        assertSwitchForText("Keep Screen On", checked = true)
    }

    @Test
    fun scrollingPositionSurvivesProcessRestart() {
        openChapter()
        scrollReader(1)
        val expected = centeredText("Benchmark progress paragraph")
        restartApp()
        tapText("Resume Last Reading")
        visibleTextContaining("Benchmark progress paragraph")
        assertEquals(expected, waitForCenteredText("Benchmark progress paragraph", expected))
    }

    @Test
    fun pagePositionSurvivesProcessRestartAndRemainsInteractive() {
        openChapter()
        enablePageMode(tapZones = true)
        val initialProgress = savedProgress()
        val initialPage = flipPage()
        tapPage(forward = true)
        val expectedPage = waitForDifferentPage(initialPage)
        assertNotEquals(initialPage, expectedPage)
        val expectedProgress = waitForProgressAfter(initialProgress)
        assertTrue(
            "Progress did not advance: $initialProgress -> $expectedProgress",
            expectedProgress > initialProgress,
        )
        restartApp()
        tapText("Resume Last Reading")
        visibleTextContaining("Benchmark progress paragraph")
        assertEquals(expectedPage, waitForPage(expectedPage))
        assertEquals(expectedProgress, savedProgress())

        tapPage(forward = true)
        assertNotEquals(expectedPage, waitForDifferentPage(expectedPage))
    }

    @Test
    fun sameChapterReentryUsesLatestPageAfterRepeatedEmission() {
        openChapter()
        enablePageMode(tapZones = true)
        val initialProgress = savedProgress()
        val initialPage = flipPage()
        tapPage(forward = true)
        val savedPage = waitForDifferentPage(initialPage)
        assertNotEquals(initialPage, savedPage)
        val saved = waitForProgressAfter(initialProgress)
        assertTrue("Progress did not advance: $initialProgress -> $saved", saved > initialProgress)

        repeat(2) {
            pressBack()
            visibleText(PRIMARY_BOOK)
            tapScrolledText("Benchmark Chapter One")
            visibleTextContaining("Benchmark progress paragraph")
            assertEquals(savedPage, waitForPage(savedPage))
            assertEquals(saved, savedProgress())
        }

        tapPage(forward = true)
        val advancedPage = waitForDifferentPage(savedPage)
        val advanced = waitForProgressAfter(saved)
        fixtureAction("REEMIT_CHAPTER", "reemit=SUCCEEDED")
        SystemClock.sleep(750)
        assertEquals(advancedPage, flipPage())
        assertEquals(advanced, savedProgress())
        tapPage(forward = true)
        assertNotEquals(advancedPage, waitForDifferentPage(advancedPage))
    }

    @Test
    fun volumeKeysNavigatePagesInBothDirections() {
        openChapter()
        enablePageMode(volumeKeys = true)
        val first = flipPage()
        pressVolume(forward = true)
        val second = waitForDifferentPage(first)
        assertNotEquals(first, second)
        pressVolume(forward = false)
        assertEquals(first, waitForPage(first))
    }

    @Test
    fun readerTextExposesSystemCopyAction() {
        openChapter()
        visibleTextContaining("Benchmark progress paragraph").longClick()
        assertTrue(
            device.wait(Until.hasObject(By.text(Pattern.compile("Copy|复制"))), UI_TIMEOUT),
        )
    }

    @Test
    fun selectionGestureInsideTapZoneDoesNotTurnPage() {
        openChapter()
        enablePageMode(tapZones = true, noAnimation = true)
        val page = flipPage()
        val paragraph = visibleTextContaining("Benchmark progress paragraph")
        val bounds = paragraph.visibleBounds
        val x = minOf(device.displayWidth * 5 / 6, bounds.right - 4)
        shell("input swipe $x ${bounds.centerY()} $x ${bounds.centerY()} 800")
        assertTrue(
            device.wait(Until.hasObject(By.text(Pattern.compile("Copy|复制"))), UI_TIMEOUT),
        )
        assertEquals(page, waitForPage(page))
    }

    @Test
    fun booksKeepIndependentLastChapterAndProgress() {
        openChapter()
        scrollReader(2)
        val firstAnchor = centeredText("Benchmark progress paragraph")

        restartApp()
        navigateToBook(SECONDARY_BOOK)
        tapScrolledText("Second Book Chapter Two")
        visibleTextContaining("Second book chapter two progress paragraph")
        scrollReader(2)
        val secondAnchor = centeredText("Second book chapter two progress paragraph")

        restartApp()
        navigateToBook(PRIMARY_BOOK)
        tapScrolledText("Benchmark Chapter One")
        assertEquals(firstAnchor, waitForCenteredText("Benchmark progress paragraph", firstAnchor))

        restartApp()
        navigateToBook(SECONDARY_BOOK)
        tapScrolledText("Second Book Chapter Two")
        assertEquals(
            secondAnchor,
            waitForCenteredText("Second book chapter two progress paragraph", secondAnchor),
        )
    }
}
