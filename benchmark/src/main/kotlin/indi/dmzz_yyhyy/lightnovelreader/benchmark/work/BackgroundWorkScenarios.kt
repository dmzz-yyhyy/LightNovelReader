package indi.dmzz_yyhyy.lightnovelreader.benchmark.work

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import indi.dmzz_yyhyy.lightnovelreader.benchmark.explore.ExploreNetworkScenarios
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BackgroundWorkScenarios : BenchmarkTestCase() {
    @Test
    fun liveBookCacheRunsFromUiUntilCached() {
        launchApp()
        openRoot("Explore")
        tapDescription("search")
        replaceText(0, ExploreNetworkScenarios.LIVE_QUERY)
        SystemClock.sleep(6_000)
        device.pressEnter()
        visibleDescription("export", NETWORK_WORK_TIMEOUT)
        scrollToText("Not Cached")

        var started = false
        repeat(5) {
            if (!started) {
                tapClickableText("Not Cached")
                started = device.wait(Until.gone(By.text("Not Cached")), 3_000L)
            }
        }
        assertTrue("Cache worker never started", started)
        val cached = device.wait(Until.findObject(By.text("Cached")), NETWORK_WORK_TIMEOUT)
        assertNotNull("Live book was not fully cached", cached)
    }

    @Test
    fun mainUiSchedulesAndCompletesPeriodicUpdateWork() {
        shell("logcat -c")
        launchApp()
        val jobs = shell("dumpsys jobscheduler")
        val service = "$TARGET_PACKAGE/androidx.work.impl.background.systemjob.SystemJobService"
        val start = jobs.indexOf(service)
        assertTrue("WorkManager system job was not scheduled", start >= 0)
        val block = jobs.substring(start, (start + 4_000).coerceAtMost(jobs.length))
        assertTrue("Scheduled job was not CheckUpdateWork:\n$block", "Trace tag: CheckUpdateWork" in block)

        val pid = shell("pidof $TARGET_PACKAGE").trim()
        assertTrue("Target process was not running", pid.isNotBlank())
        device.pressHome()
        var workLog = ""
        waitUntil("CheckUpdateWork did not complete successfully", UPDATE_WORK_TIMEOUT, 500L) {
            workLog = shell("logcat -d --pid=$pid")
            "Starting work for indi.dmzz_yyhyy.lightnovelreader.data.work.CheckUpdateWork" in workLog &&
                "Worker result SUCCESS" in workLog
        }
    }

    companion object {
        private const val UPDATE_WORK_TIMEOUT = 20_000L
        private const val NETWORK_WORK_TIMEOUT = 240_000L
    }
}
