package indi.dmzz_yyhyy.lightnovelreader.benchmark.explore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.waitForStable
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ExploreInteractionScenarios : BenchmarkTestCase() {
    @Test
    fun everySourceTabKeepsSearchAndContentSurfaceAvailable() {
        launchApp()
        openRoot("Explore")
        val tabY = (device.displayHeight * 0.21f).toInt()
        listOf(0.17f, 0.50f, 0.83f).forEach { x ->
            device.click((device.displayWidth * x).toInt(), tabY)
            device.waitForIdle()
            assertForegroundPackage(TARGET_PACKAGE)
            visibleDescription("search")
        }
    }

    @Test
    fun searchInputFilterClearAndBackWork() {
        launchApp()
        openRoot("Explore")
        tapDescription("search")
        visibleDescription("back")
        replaceText(0, "benchmark")
        tapDescription("filter")
        assertForegroundPackage(TARGET_PACKAGE)
        pressBack()
        tapDescription("clear")
        pressBack()
        visibleText("Explore")
    }

    @Test
    fun searchSettingsChangeTheQueryScope() {
        launchApp()
        openRoot("Explore")
        tapDescription("search")
        visibleText("请输入书本名称")

        tapDescription("filter")
        visibleText("Search settings")
        tapClickableText("按作者名搜索")
        pressBack()

        visibleText("请输入作者名称")
        tapDescription("filter")
        tapClickableText("按书名搜索")
        pressBack()
        visibleText("请输入书本名称")
    }

    @Test
    fun searchHistorySupportsSingleDeleteAndClearAll() {
        launchApp()
        openRoot("Explore")
        tapDescription("search")
        submitSearch("automation-history")
        tapClickableText("automation-history")
        tapDescription("clear")
        visibleText("Search History")
        visibleText("automation-history")
        val delete = visibleDescription("delete")
        assertTrue(
            "History delete action kept moving",
            !delete.waitForStable(stableIntervalMs = 250, requireStableScreenshot = false).isTimeout,
        )
        delete.click()
        assertTrue(
            "Deleted history entry remained visible",
            device.wait(Until.gone(By.text("automation-history")), UI_TIMEOUT),
        )

        submitSearch("clear-all-history")
        tapClickableText("clear-all-history")
        tapDescription("clear")
        val clearAll = visibleText("Clear All")
        assertTrue(
            "Clear history action kept moving",
            !clearAll.waitForStable(stableIntervalMs = 250, requireStableScreenshot = false).isTimeout,
        )
        tapClickable(clearAll)
        assertTrue(device.wait(Until.gone(By.text("clear-all-history")), UI_TIMEOUT))
    }
}
