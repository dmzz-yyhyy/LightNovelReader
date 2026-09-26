package indi.dmzz_yyhyy.lightnovelreader.benchmark.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NavigationScenarios : BenchmarkTestCase() {
    @Test
    fun everyRootDestinationIsReachable() {
        launchApp()
        visibleText("Reading")

        openRoot("Bookshelf")
        visibleText("Benchmark Shelf")
        openRoot("Explore")
        visibleDescription("search")
        openRoot("Settings")
        visibleText("Extensions")
        visibleText("Reading")
        visibleText("Display")
    }

    @Test
    fun statisticsOverviewReturnsToReading() {
        launchApp()
        tapDescription("statistics")
        visibleText("Statistics")
        visibleTextContaining("1")
        device.swipe(
            device.displayWidth / 2,
            (device.displayHeight * 0.75f).toInt(),
            device.displayWidth / 2,
            (device.displayHeight * 0.30f).toInt(),
            20,
        )
        assertForegroundPackage(TARGET_PACKAGE)
        pressBack()
        visibleText("Reading")
    }

    @Test
    fun statisticsDetailSwitchesEveryTimeScale() {
        launchApp()
        tapDescription("statistics")
        visibleText("Calendar")
        tapText("Detail")

        listOf("Weekly", "Monthly", "Yearly").forEach { scale ->
            tapText(scale)
            visibleText("Reading Time")
        }
        pressBack()
        visibleText("Statistics")
    }
}
