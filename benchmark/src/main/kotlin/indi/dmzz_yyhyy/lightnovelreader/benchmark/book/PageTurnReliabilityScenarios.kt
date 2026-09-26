package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PageTurnReliabilityScenarios : ReaderTestCase() {
    @Test
    fun animatedPagingCrossesChapterBoundaryBothWays() {
        openChapter()
        enablePageMode()
        turnUntilChapter("benchmark-chapter-2", forward = true)
        waitForPagerIdle()
        visibleTextContaining("Benchmark chapter two start marker")
        turnUntilChapter("benchmark-chapter-1", forward = false)
        waitForPagerIdle()
        visibleTextContaining("Benchmark chapter one end marker")
    }

    @Test
    fun repeatedRoundTripsAndBookEdgesAreStable() {
        openChapter()
        enablePageMode()
        waitForChapter("benchmark-chapter-1")
        val firstPage = reachBoundary(forward = false)
        repeat(3) {
            swipePage(forward = false)
            assertEquals(firstPage, flipPage())
        }

        turnUntilChapter("benchmark-chapter-2", forward = true)
        repeat(3) {
            turnUntilChapter("benchmark-chapter-1", forward = false)
            visibleTextContaining("Benchmark chapter one end marker")
            swipePage(forward = true)
            waitForChapter("benchmark-chapter-2")
            visibleTextContaining("Benchmark chapter two start marker")
        }

        val lastPage = reachBoundary(forward = true)
        repeat(3) {
            swipePage(forward = true)
            assertEquals(lastPage, flipPage())
        }
        swipePage(forward = false)
        assertNotEquals(lastPage, flipPage())
        swipePage(forward = true)
        assertEquals(lastPage, waitForPage(lastPage))
    }

    @Test
    fun rapidAnimatedSwipesRecoverAcrossChapters() {
        openChapter()
        enablePageMode()
        reachBoundary(forward = false)
        repeat(24) { swipePage(forward = true, steps = 2, settleMs = 25) }
        waitForPagerIdle()
        val last = reachBoundary(forward = true)
        assertTrue(flipChapter().endsWith("flip-chapter-benchmark-chapter-2"))

        repeat(24) { swipePage(forward = false, steps = 2, settleMs = 25) }
        waitForPagerIdle()
        val first = reachBoundary(forward = false)
        assertTrue(flipChapter().endsWith("flip-chapter-benchmark-chapter-1"))
        assertNotEquals(last, first)
        swipePage(forward = true)
        assertNotEquals(first, waitForDifferentPage(first))
    }

    @Test
    fun noAnimationPagingKeepsChapterBoundaryInteractive() {
        openChapter()
        enablePageMode(noAnimation = true)
        turnUntilChapter("benchmark-chapter-2", forward = true)
        visibleTextContaining("Benchmark chapter two start marker")
        turnUntilChapter("benchmark-chapter-1", forward = false)
        visibleTextContaining("Benchmark chapter one end marker")
        val lastChapterOnePage = flipPage()
        swipePage(forward = true)
        waitForChapter("benchmark-chapter-2")
        swipePage(forward = false)
        waitForChapter("benchmark-chapter-1")
        assertEquals(lastChapterOnePage, waitForPage(lastChapterOnePage))
    }

    @Test
    fun rapidNoAnimationTapsCrossOneChapterAndRecover() {
        openChapter()
        enablePageMode(tapZones = true, noAnimation = true)
        repeat(24) {
            tapPage(forward = true)
            SystemClock.sleep(20)
        }
        waitForChapter("benchmark-chapter-2")
        waitForPagerIdle()
        val last = reachBoundary(forward = true)
        tapPage(forward = false)
        assertNotEquals(last, waitForDifferentPage(last))
    }

    @Test
    fun rapidNoAnimationTapsCrossManyChaptersAndRecover() {
        fixtureAction("EXTEND_RAPID_CHAPTER_CHAIN", "rapid-chapters=SUCCEEDED")
        openChapter(
            chapter = "Benchmark Chapter Two",
            expectedContent = "Benchmark chapter two progress paragraph",
        )
        enablePageMode(tapZones = true, noAnimation = true)
        repeat(320) {
            tapPage(forward = true)
            SystemClock.sleep(20)
        }
        val number = Regex("benchmark-chapter-(\\d+)$")
            .find(flipChapter())?.groupValues?.get(1)?.toIntOrNull() ?: -1
        waitForPagerIdle()
        assertTrue("Expected to cross eight chapters, reached ${flipState()}", number >= 10)
        val last = reachBoundary(forward = true)
        tapPage(forward = false)
        assertNotEquals(last, waitForDifferentPage(last))
    }

    @Test
    fun rapidNoAnimationVolumeInputDoesNotLockAtBookEnd() {
        openChapter()
        enablePageMode(volumeKeys = true, noAnimation = true)
        repeat(24) { pressVolume(forward = true) }
        waitForPagerIdle()
        turnUntilChapter("benchmark-chapter-2", forward = true)
        val last = reachBoundary(forward = true)
        pressVolume(forward = false)
        assertNotEquals(last, waitForDifferentPage(last))
        assertForegroundPackage(TARGET_PACKAGE)
    }
}
