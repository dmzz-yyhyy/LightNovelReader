package indi.dmzz_yyhyy.lightnovelreader.benchmark.reading

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderTestCase
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReadingScenarios : ReaderTestCase() {
    @Test
    fun continueReadingAndRecentBookOpenCorrectDestinations() {
        launchApp()
        visibleText("Continue Reading")
        visibleText("Resume Last Reading")
        visibleTextContaining("Recent Reads")

        tapClickableText("Resume Last Reading")
        assertForegroundPackage(TARGET_PACKAGE)
        revealReaderChrome()
        visibleDescription("menu")
        pressBack()
        pressBack()
        visibleText("Reading")

        tapLastText(PRIMARY_BOOK)
        visibleText("Benchmark Author")
        visibleText("Benchmark Volume")
    }

    @Test
    fun bookManagerTabsSortingAndCacheDetailsAreReachable() {
        launchApp()
        val statistics = visibleDescription("statistics")
        device.click(
            statistics.visibleBounds.centerX() - (device.displayWidth * 0.10f).toInt(),
            statistics.visibleBounds.centerY(),
        )
        device.waitForIdle()

        visibleText("Book Manager")
        visibleText("Downloads")
        visibleText("Local books")
        visibleText("Nothing Here")
        tapText("Local books")
        visibleText(PRIMARY_BOOK)

        tapDescription("sort")
        visibleText("Sort by size")
        visibleText("Sort by last read")
        visibleText("Sort by chapter count")
        tapText("Sort by chapter count")

        tapDescription("info")
        visibleText("Book Cache Details")
        listOf(
            "Book information",
            "Volume index",
            "Chapter information",
            "Chapter content",
            "Reading record",
        ).forEach(::visibleText)
    }
}
