package indi.dmzz_yyhyy.lightnovelreader.benchmark.explore

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@LargeTest
@RunWith(AndroidJUnit4::class)
class ExploreNetworkScenarios : BenchmarkTestCase() {
    @Test
    fun liveHomepageLoadsBooksAndOpensDetail() {
        launchApp()
        openRoot("Explore")
        tap(waitForBookCard(BookCardLayout.COMPACT))
        visibleDescription("export")
        visibleDescription("formatting")
        visibleDescription("more")
    }

    @Test
    fun exactSearchRequestsSourceAndOpensResult() {
        launchApp()
        openRoot("Explore")
        tapDescription("search")
        replaceText(0, LIVE_QUERY)
        SystemClock.sleep(6_000)
        shell("logcat -c")
        device.pressEnter()
        device.waitForIdle()
        visibleText(LIVE_QUERY)
        allDescriptions("export", NETWORK_TIMEOUT)

        val log = shell("logcat -d")
        assertTrue("Search endpoint was not requested", "searchtype=articlename" in log)
        assertTrue("Search response was not received", "Ktor Client: FROM:" in log)
        visibleDescription("formatting")
    }

    @Test
    fun expandedPageLoadsFiltersRefreshesAndPages() {
        launchApp()
        openRoot("Explore")
        device.click(device.displayWidth / 2, (device.displayHeight * 0.21f).toInt())
        device.waitForIdle()
        tap(allDescriptions("expand", NETWORK_TIMEOUT).first())
        visibleDescription("back")
        val filters = device.wait(Until.findObjects(By.checkable(true)), NETWORK_TIMEOUT).orEmpty()
        assertTrue("Expanded page filters did not load", filters.isNotEmpty())
        waitForBookCard(BookCardLayout.FULL_WIDTH)
        tap(device.findObjects(By.checkable(true)).first())
        waitForBookCard(BookCardLayout.FULL_WIDTH)

        repeat(5) {
            val scroller = device.findObjects(By.scrollable(true))
                .maxByOrNull { it.visibleBounds.height() }
            if (scroller != null) {
                scroller.scroll(Direction.DOWN, 0.9f)
            } else {
                device.swipe(
                    device.displayWidth / 2,
                    (device.displayHeight * 0.8f).toInt(),
                    device.displayWidth / 2,
                    (device.displayHeight * 0.2f).toInt(),
                    30,
                )
            }
            device.waitForIdle()
        }
        assertForegroundPackage(TARGET_PACKAGE)
        visibleDescription("back")
    }

    private fun waitForBookCard(
        layout: BookCardLayout,
        timeout: Long = NETWORK_TIMEOUT,
    ): UiObject2 {
        var result: UiObject2? = null
        waitUntil("No loaded book card became visible", timeout, 250L) {
            result = device.findObjects(By.clickable(true)).firstOrNull { node ->
                try {
                    val bounds = node.visibleBounds
                    val widthFraction = bounds.width().toFloat() / device.displayWidth
                    val heightFraction = bounds.height().toFloat() / device.displayHeight
                    val matchesCardShape = when (layout) {
                        BookCardLayout.COMPACT ->
                            widthFraction in 0.15f..0.45f && heightFraction in 0.15f..0.50f

                        BookCardLayout.FULL_WIDTH ->
                            widthFraction >= 0.70f && heightFraction in 0.10f..0.40f
                    }
                    !node.isCheckable &&
                        matchesCardShape &&
                        bounds.centerY() in
                        (device.displayHeight * 0.15f).toInt()..(device.displayHeight * 0.85f).toInt() &&
                        node.findObject(By.text(Pattern.compile(".+"))) != null
                } catch (_: StaleObjectException) {
                    false
                }
            }
            result != null
        }
        return result ?: error("No loaded book card became visible")
    }

    private enum class BookCardLayout {
        COMPACT,
        FULL_WIDTH,
    }

    companion object {
        const val LIVE_QUERY = "奇招百出的维多利亚"
    }
}
