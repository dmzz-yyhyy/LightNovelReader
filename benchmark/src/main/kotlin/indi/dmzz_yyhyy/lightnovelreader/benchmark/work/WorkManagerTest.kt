package indi.dmzz_yyhyy.lightnovelreader.benchmark.work

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import indi.dmzz_yyhyy.lightnovelreader.benchmark.ui.UiAutomatorTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("SameParameterValue")
@LargeTest
@RunWith(AndroidJUnit4::class)
class WorkManagerTest : UiAutomatorTest() {
    @Test
    fun snapshotExportAndImportRoundTripRunFromSettingsUi() {
        removeOutput(SNAPSHOT_FILE)
        openSettings()
        clickScrolledText("Snapshot User Data")
        clickScrolledText("Export to File")
        saveCreatedDocument(SNAPSHOT_FILE)

        assertOutputFile(SNAPSHOT_FILE)

        // Remove fixture data after the snapshot so a successful import has a
        // user-visible result instead of merely producing a success toast.
        restartApp()
        openBottomNavigation("Bookshelf")
        openBookshelfMenu()
        clickText("Bookshelf Settings")
        clickText("Delete Bookshelf")
        clickText("OK")
        assertTextNotVisible("Benchmark Shelf")

        openBottomNavigation("Settings")
        clickScrolledText("Import User Data")
        selectDocument(SNAPSHOT_FILE)
        assertText("Merge")
        clickText("Merge")
        assertTrue(
            "ImportDataWork did not reach a terminal state",
            device.wait(Until.gone(By.text("Merge")), WORK_TIMEOUT),
        )

        restartApp()
        openBottomNavigation("Bookshelf")
        assertText("Benchmark Shelf")
        assertText("Benchmark Sample Novel")
    }

    @Test
    fun bookshelfExportRunsFromBookshelfUiAndWritesFile() {
        removeOutput(BOOKSHELF_FILE)
        launchApp()
        openBottomNavigation("Bookshelf")
        openBookshelfMenu()
        clickText("Import & Export…")
        clickText("Export to .lnr File")
        saveCreatedDocument(BOOKSHELF_FILE)

        assertOutputFile(BOOKSHELF_FILE)
        assertText("Benchmark Shelf")
    }

    @Test
    fun epubExportRunsFromBookUiAndWritesFile() {
        removeOutput(EPUB_FILE)
        openFixtureBook()
        clickDescription("export")
        assertText("Export as Epub")
        clickText("Export")
        saveCreatedDocument(EPUB_FILE)

        assertOutputFile(EPUB_FILE)
        assertText("Benchmark Sample Novel")
    }

    @Test
    fun cacheBookRunsFromLiveBookUiAndMarksBookCached() {
        launchApp()
        openBottomNavigation("Explore")
        clickDescription("search")
        setFirstTextField(LIVE_BOOK_QUERY)
        // The real source rate-limits searches from the same public IP.
        SystemClock.sleep(6_000)
        device.pressEnter()
        waitForDescription("export", NETWORK_WORK_TIMEOUT)

        scrollToText("Not Cached")
        var started = false
        repeat(5) {
            if (started) return@repeat
            clickClickableText("Not Cached")
            started = device.wait(Until.gone(By.text("Not Cached")), 3_000)
        }
        assertTrue("Cache button never started CacheBookWork", started)
        val cached = device.wait(Until.findObject(By.text("Cached")), NETWORK_WORK_TIMEOUT)
        assertNotNull("CacheBookWork did not make the live book fully cached", cached)
    }

    @Test
    fun openingMainUiRunsAndReschedulesPeriodicUpdateWork() {
        shell("logcat -c")
        launchApp()

        val jobs = shell("dumpsys jobscheduler")
        val service =
            "$TARGET_PACKAGE/androidx.work.impl.background.systemjob.SystemJobService"
        val jobStart = jobs.indexOf(service)
        assertTrue("Opening MainActivity did not schedule WorkManager's system job", jobStart >= 0)
        val jobBlock = jobs.substring(
            jobStart,
            (jobStart + 4_000).coerceAtMost(jobs.length),
        )
        assertTrue(
            "The scheduled system job was not CheckUpdateWork:\n$jobBlock",
            "Trace tag: CheckUpdateWork" in jobBlock,
        )

        // CheckUpdateWork intentionally waits until the activity has stopped.
        // Background the app through the device UI and verify this UI-scheduled
        // first run reaches WorkManager's successful terminal result.
        val appPid = shell("pidof $TARGET_PACKAGE").trim()
        assertTrue("Target process was not running", appPid.isNotBlank())
        device.pressHome()
        val deadline = SystemClock.elapsedRealtime() + UPDATE_WORK_TIMEOUT
        var workLog = ""
        while (SystemClock.elapsedRealtime() < deadline) {
            workLog = shell("logcat -d --pid=$appPid")
            if (
                "Starting work for indi.dmzz_yyhyy.lightnovelreader.data.work.CheckUpdateWork" in
                workLog &&
                "Worker result SUCCESS" in workLog
            ) {
                break
            }
            SystemClock.sleep(500)
        }
        val relevantLog = workLog.takeLast(8_000)
        assertTrue(
            "CheckUpdateWork did not start after opening the main UI:\n$relevantLog",
            "Starting work for indi.dmzz_yyhyy.lightnovelreader.data.work.CheckUpdateWork" in
                    workLog,
        )
        assertTrue(
            "CheckUpdateWork did not finish successfully after the UI was backgrounded:\n$relevantLog",
            "Worker result SUCCESS" in workLog,
        )
    }

    private fun openSettings() {
        launchApp()
        openBottomNavigation("Settings")
    }

    private fun openFixtureBook() {
        launchApp()
        openBottomNavigation("Bookshelf")
        clickText("Benchmark Sample Novel")
        assertText("Benchmark Sample Novel")
    }

    private fun openBookshelfMenu() {
        device.click(
            (device.displayWidth * 0.94).toInt(),
            (device.displayHeight * 0.075).toInt(),
        )
        device.waitForIdle()
    }

    private fun saveCreatedDocument(fileName: String) {
        assertForegroundPackage(DOCUMENTS_PACKAGE)
        val nameField = device.wait(
            Until.findObject(By.res("android", "title").clazz("android.widget.EditText")),
            TIMEOUT,
        )
        assertNotNull("DocumentsUI filename field was not visible", nameField)
        nameField.text = fileName

        val save = device.wait(Until.findObject(By.res("android", "button1")), TIMEOUT)
        assertNotNull("DocumentsUI save button was not visible", save)
        save.click()
        device.waitForIdle()

        // DocumentsUI can still ask for replacement if media indexing has not
        // observed the exact-file cleanup yet.
        if (device.hasObject(By.pkg(DOCUMENTS_PACKAGE))) {
            device.findObject(By.res("android", "button1"))?.click()
        }
        assertTrue(
            "App did not return after selecting a document destination",
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), TIMEOUT),
        )
    }

    private fun selectDocument(fileName: String) {
        assertForegroundPackage(DOCUMENTS_PACKAGE)
        // Some DocumentsUI builds hide known extensions in their accessibility
        // text even though the selected URI still points at the complete name.
        val displayName = fileName.substringBeforeLast('.')
        val fileList = device.findObject(By.res(DOCUMENTS_PACKAGE, "dir_list"))
        // DocumentsUI remembers the previous directory scroll position. Return
        // to the beginning before searching so an alphabetically early fixture
        // is not left above the viewport.
        var scrollAttempts = 0
        while (scrollAttempts++ < 20 && fileList?.scroll(Direction.DOWN, 0.8f) == true) {
            device.waitForIdle()
        }
        var file = device.findObject(By.textContains(displayName))
        scrollAttempts = 0
        while (file == null && scrollAttempts++ < 20) {
            fileList?.scroll(Direction.UP, 0.8f)
            device.waitForIdle()
            file = device.findObject(By.textContains(displayName))
        }
        val visibleTexts = device.findObjects(By.clazz("android.widget.TextView"))
            .map { it.text }
            .filter { it.isNotBlank() }
        assertNotNull(
            "Exported file was not visible in DocumentsUI: $fileName; visible=$visibleTexts",
            file,
        )
        file.click()
        device.waitForIdle()
        if (device.hasObject(By.pkg(DOCUMENTS_PACKAGE))) {
            device.findObject(By.res("android", "button1"))?.click()
        }
        assertTrue(
            "App did not receive the selected document",
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), TIMEOUT),
        )
    }

    private fun waitForDescription(description: String, timeout: Long) {
        val item = device.wait(Until.findObject(By.desc(description)), timeout)
        assertNotNull("Description '$description' did not appear", item)
    }

    private fun removeOutput(fileName: String) {
        shell("rm -f /sdcard/Documents/$fileName")
    }

    private fun assertOutputFile(fileName: String) {
        val deadline = SystemClock.elapsedRealtime() + WORK_TIMEOUT
        var size = 0L
        while (SystemClock.elapsedRealtime() < deadline) {
            size = shell("stat -c %s /sdcard/Documents/$fileName")
                .trim()
                .toLongOrNull() ?: 0L
            if (size > 0L) break
            SystemClock.sleep(250)
        }
        assertTrue("UI-triggered worker produced no data in $fileName", size > 0L)
    }

    companion object {
        private const val DOCUMENTS_PACKAGE = "com.android.documentsui"
        private const val SNAPSHOT_FILE = "BenchmarkUiSnapshot.lnr"
        private const val BOOKSHELF_FILE = "BenchmarkUiBookshelf.lnr"
        private const val EPUB_FILE = "BenchmarkUiNovel.epub"
        private const val LIVE_BOOK_QUERY = "奇招百出的维多利亚"
        private const val WORK_TIMEOUT = 45_000L
        private const val UPDATE_WORK_TIMEOUT = 20_000L
        private const val NETWORK_WORK_TIMEOUT = 240_000L
    }
}
