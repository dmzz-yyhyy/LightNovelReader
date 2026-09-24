package indi.dmzz_yyhyy.lightnovelreader.benchmark.ui

import android.graphics.Rect
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before

@Suppress("unused", "SameParameterValue")
abstract class UiAutomatorTest {
    protected val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun resetTargetApp() {
        device.pressHome()
        shell("pm clear $TARGET_PACKAGE")
        shell("cmd locale set-app-locales $TARGET_PACKAGE --user 0 --locales en-US")
        shell("pm grant $TARGET_PACKAGE android.permission.POST_NOTIFICATIONS")
        shell(
            "am broadcast -W -n $TARGET_PACKAGE/.benchmark.BenchmarkFixtureReceiver " +
                    "-a $SEED_ACTION"
        )
    }

    protected fun launchApp() {
        shell("am start -W -n $TARGET_PACKAGE/.MainActivity")
        assertTrue(
            "Target app did not become visible",
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), TIMEOUT),
        )
        device.waitForIdle()
    }

    protected fun restartApp() {
        shell("am force-stop $TARGET_PACKAGE")
        launchApp()
    }

    protected fun shell(command: String): String = device.executeShellCommand(command)

    protected fun assertText(text: String): UiObject2 {
        val object2 = device.wait(Until.findObject(By.text(text)), TIMEOUT)
        assertNotNull("Expected text was not visible: $text", object2)
        return object2
    }

    protected fun assertTextContains(text: String): UiObject2 {
        val object2 = device.wait(Until.findObject(By.textContains(text)), TIMEOUT)
        assertNotNull("Expected text fragment was not visible: $text", object2)
        return object2
    }

    protected fun assertTextNotVisible(text: String, timeout: Long = 1_500L) {
        assertFalse(
            "Text should not be visible: $text",
            device.wait(Until.hasObject(By.text(text)), timeout),
        )
    }

    protected fun assertDescription(description: String): UiObject2 {
        val object2 = device.wait(Until.findObject(By.desc(description)), TIMEOUT)
        assertNotNull("Expected content description was not visible: $description", object2)
        return object2
    }

    protected fun clickText(text: String) {
        clickCenter(assertText(text))
        device.waitForIdle()
    }

    protected fun clickTextContains(text: String) {
        clickCenter(assertTextContains(text))
        device.waitForIdle()
    }

    protected fun clickLastText(text: String) {
        val objects = device.wait(Until.findObjects(By.text(text)), TIMEOUT)
        assertTrue("Expected text was not visible: $text", objects.isNotEmpty())
        clickCenter(objects.last())
        device.waitForIdle()
    }

    protected fun clickDescription(description: String, index: Int = 0) {
        val objects = device.wait(Until.findObjects(By.desc(description)), TIMEOUT)
        assertTrue(
            "Expected content description was not visible: $description",
            objects != null && objects.size > index,
        )
        clickCenter(objects[index])
        device.waitForIdle()
    }

    protected fun longClickText(text: String) {
        assertText(text).longClick()
        device.waitForIdle()
    }

    protected fun clickMenuItemAfter(text: String) {
        var item = assertText(text)
        while (!item.isClickable && item.parent != null) {
            item = item.parent
        }
        val bounds = item.visibleBounds
        device.click(bounds.centerX(), bounds.bottom + bounds.height() / 2)
        device.waitForIdle()
    }

    protected fun clickCenter(object2: UiObject2) {
        val bounds: Rect = object2.visibleBounds
        device.click(bounds.centerX(), bounds.centerY())
    }

    protected fun clickClickableText(text: String) {
        var target = assertText(text)
        while (!target.isClickable && target.parent != null) {
            target = target.parent
        }
        if (target.isClickable) target.click() else clickCenter(target)
        device.waitForIdle()
    }

    protected fun scrollToText(text: String, attempts: Int = 12): UiObject2 {
        repeat(attempts) {
            try {
                device.findObject(By.text(text))?.let {
                    val bounds = it.visibleBounds
                    val safeTop = (device.displayHeight * 0.12f).toInt()
                    val safeBottom = (device.displayHeight * 0.88f).toInt()
                    if (bounds.centerY() in safeTop..safeBottom) return assertText(text)
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                // Compose may replace semantics nodes during a scroll animation.
            }
            val scrollTargets = device.findObjects(By.scrollable(true))
                .mapNotNull { candidate ->
                    try {
                        candidate to candidate.visibleBounds.height()
                    } catch (_: androidx.test.uiautomator.StaleObjectException) {
                        null
                    }
                }
                .sortedByDescending { it.second }
            val scrolled = scrollTargets.any { (candidate, _) ->
                try {
                    candidate.scroll(Direction.UP, 0.35f)
                } catch (_: androidx.test.uiautomator.StaleObjectException) {
                    false
                }
            }
            if (!scrolled) {
                device.swipe(
                    device.displayWidth / 2,
                    (device.displayHeight * 0.78).toInt(),
                    device.displayWidth / 2,
                    (device.displayHeight * 0.32).toInt(),
                    40,
                )
            }
            device.waitForIdle()
        }
        return assertText(text)
    }

    protected fun clickScrolledText(text: String) {
        var target = scrollToText(text)
        SystemClock.sleep(1_000)
        // The same title can be exposed both by a book-cover placeholder and by
        // the surrounding card. On compact layouts the first matching text node
        // is often the non-clickable cover label, so activate its clickable
        // ancestor instead of tapping the raw text bounds.
        target = assertText(text)
        while (!target.isClickable && target.parent != null) {
            target = target.parent
        }
        if (target.isClickable) target.click() else clickCenter(target)
        device.waitForIdle()
    }

    protected fun openBottomNavigation(label: String) {
        clickText(label)
        assertText(label)
    }

    protected fun pressBack() {
        device.pressBack()
        device.waitForIdle()
    }

    protected fun assertForegroundPackage(packageName: String) {
        assertEquals(packageName, device.currentPackageName)
    }

    protected fun setFirstTextField(value: String) {
        setTextField(0, value)
    }

    protected fun setTextField(index: Int, value: String) {
        val fields = device.wait(
            Until.findObjects(By.clazz("android.widget.EditText")),
            TIMEOUT,
        )
        assertTrue("Editable text field $index was not visible", fields.size > index)
        fields[index].text = value
        device.waitForIdle()
    }

    protected fun assertFirstSwitchChecked(expected: Boolean) {
        assertCheckableChecked(0, expected)
    }

    protected fun assertCheckableChecked(index: Int, expected: Boolean) {
        val checkables = device.wait(
            Until.findObjects(By.checkable(true)),
            TIMEOUT,
        )
        assertTrue("Checkable control $index was not visible", checkables.size > index)
        assertEquals(
            "Unexpected checkable state at index $index",
            expected,
            checkables[index].isChecked
        )
    }

    protected fun clickFirstCheckable() {
        val checkable = device.wait(Until.findObject(By.checkable(true)), TIMEOUT)
        assertNotNull("No checkable control was visible", checkable)
        clickCenter(checkable)
        device.waitForIdle()
    }

    companion object {
        const val TARGET_PACKAGE = "indi.dmzz_yyhyy.lightnovelreader"
        const val SEED_ACTION = "$TARGET_PACKAGE.benchmark.SEED"
        const val TIMEOUT = 10_000L
    }
}
