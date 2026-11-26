package indi.dmzz_yyhyy.lightnovelreader.benchmark.framework

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.waitForStable
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.util.regex.Pattern

/** Keep failure evidence for reader regression scenarios, including system picker UI. */
abstract class ReaderRegressionTestCase : ReaderTestCase() {
    protected fun openSettingsHome() {
        launchApp()
        var entry = visibleText("Settings")
        while (!entry.isClickable && entry.parent != null) entry = entry.parent
        assertTrue("Settings navigation kept moving", !entry.waitForStable(
            stableIntervalMs = 250,
            requireStableScreenshot = false,
        ).isTimeout)
        entry.click()
        visibleText("Extensions")
    }

    @get:Rule
    val failureEvidence = object : TestWatcher() {
        override fun failed(error: Throwable, description: Description) {
            runCatching {
                Log.e("ReaderRegression", "Foreground package: ${device.currentPackageName}")
                device.findObjects(By.text(Pattern.compile(".+"))).take(60).forEach {
                    Log.e("ReaderRegression", "${it.text.take(160)} @ ${it.visibleBounds}")
                }
                val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
                    ?: return@runCatching
                val directory = File(output).apply { mkdirs() }
                val name = "${description.testClass.simpleName}-${description.methodName}"
                device.takeScreenshot(File(directory, "$name.png"))
                device.dumpWindowHierarchy(File(directory, "$name.xml"))
            }.onFailure { Log.e("ReaderRegression", "Could not capture failure evidence", it) }
        }
    }

    protected fun dismissReaderSettingsSheet() {
        repeat(3) {
            if (!device.hasObject(By.text("Reader Settings"))) return
            pressBack()
            device.wait(androidx.test.uiautomator.Until.gone(By.text("Reader Settings")), 1_000L)
        }
        assertTextNotVisible("Reader Settings")
    }
}
