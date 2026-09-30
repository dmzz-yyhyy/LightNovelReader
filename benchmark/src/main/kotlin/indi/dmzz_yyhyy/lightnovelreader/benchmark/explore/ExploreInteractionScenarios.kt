package indi.dmzz_yyhyy.lightnovelreader.benchmark.explore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
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
    fun searchHistorySupportsSingleDeleteAndClearAll() {
        launchApp()
        openRoot("Explore")
        tapDescription("search")
        replaceText(0, "automation-history")
        device.pressEnter()
        device.waitForIdle()
        tap(device.findObject(By.clazz("android.widget.EditText")))
        visibleText("Search History")
        visibleText("automation-history")
        tapDescription("delete")
        tapDescription("clear")
        assertTextNotVisible("automation-history")

        replaceText(0, "clear-all-history")
        device.pressEnter()
        device.waitForIdle()
        tap(device.findObject(By.clazz("android.widget.EditText")))
        visibleText("Clear All")
        tapText("Clear All")
        tapDescription("clear")
        assertTrue(device.wait(Until.gone(By.text("clear-all-history")), UI_TIMEOUT))
    }
}
