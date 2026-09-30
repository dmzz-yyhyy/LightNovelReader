package indi.dmzz_yyhyy.lightnovelreader.benchmark.framework

import android.os.SystemClock
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/** DocumentsUI interactions used by real export/import worker scenarios. */
abstract class DocumentsTestCase : BenchmarkTestCase() {
    protected fun saveDocument(fileName: String) {
        waitForDocumentsUi()
        val field = device.wait(
            Until.findObject(By.res("android", "title").clazz("android.widget.EditText")),
            UI_TIMEOUT,
        )
        assertNotNull("DocumentsUI filename field was not visible", field)
        field.text = fileName
        val save = device.wait(Until.findObject(By.res("android", "button1")), UI_TIMEOUT)
        assertNotNull("DocumentsUI save button was not visible", save)
        save.click()
        device.waitForIdle()
        if (device.hasObject(By.pkg(DOCUMENTS_PACKAGE))) {
            device.findObject(By.res("android", "button1"))?.click()
        }
        assertTrue(
            "App did not return after choosing a file destination",
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), UI_TIMEOUT),
        )
    }

    protected fun selectDocument(fileName: String) {
        waitForDocumentsUi()
        val displayName = fileName.substringBeforeLast('.')
        val list = device.findObject(By.res(DOCUMENTS_PACKAGE, "dir_list"))
        repeat(20) {
            if (list?.scroll(Direction.DOWN, 0.8f) != true) return@repeat
            device.waitForIdle()
        }
        var file = device.findObject(By.textContains(displayName))
        repeat(20) {
            if (file != null) return@repeat
            list?.scroll(Direction.UP, 0.8f)
            device.waitForIdle()
            file = device.findObject(By.textContains(displayName))
        }
        assertNotNull("Exported file was not visible: $fileName", file)
        file.click()
        device.waitForIdle()
        if (device.hasObject(By.pkg(DOCUMENTS_PACKAGE))) {
            device.findObject(By.res("android", "button1"))?.click()
        }
        assertTrue(
            "App did not receive the selected document",
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), UI_TIMEOUT),
        )
    }

    protected fun deleteDocument(fileName: String) {
        shell("rm -f /sdcard/Documents/$fileName")
    }

    protected fun assertDocumentHasData(fileName: String, timeout: Long = 45_000L) {
        var bytes = 0L
        val deadline = SystemClock.elapsedRealtime() + timeout
        while (SystemClock.elapsedRealtime() < deadline) {
            bytes = shell("stat -c %s /sdcard/Documents/$fileName")
                .trim().toLongOrNull() ?: 0L
            if (bytes > 0L) break
            SystemClock.sleep(250)
        }
        assertTrue("No data was written to $fileName", bytes > 0L)
    }

    private fun waitForDocumentsUi() {
        assertTrue(
            "DocumentsUI did not become visible",
            device.wait(Until.hasObject(By.pkg(DOCUMENTS_PACKAGE).depth(0)), UI_TIMEOUT),
        )
        assertForegroundPackage(DOCUMENTS_PACKAGE)
    }
}
