package indi.dmzz_yyhyy.lightnovelreader.benchmark.framework

import android.graphics.Rect
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before

/** Stable, accessibility-first operations shared by all end-to-end scenarios. */
@Suppress("SameParameterValue")
abstract class BenchmarkTestCase {
    protected val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun prepareDeterministicAppState() {
        device.pressHome()
        shell("pm clear $TARGET_PACKAGE")
        shell("cmd locale set-app-locales $TARGET_PACKAGE --user 0 --locales en-US")
        shell("pm grant $TARGET_PACKAGE android.permission.POST_NOTIFICATIONS")
        fixtureAction("SEED", "seed=SUCCEEDED")
    }

    protected fun launchApp() {
        shell("am start -W -n $TARGET_PACKAGE/.MainActivity")
        assertTrue(
            "Target app did not become visible",
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), UI_TIMEOUT),
        )
        device.waitForIdle()
    }

    protected fun restartApp() {
        shell("am force-stop $TARGET_PACKAGE")
        launchApp()
    }

    protected fun fixtureAction(action: String, expectedResult: String): String {
        val output = fixtureReport(action)
        assertTrue("Fixture action $action failed: $output", expectedResult in output)
        return output
    }

    protected fun fixtureReport(action: String, arguments: String = ""): String = shell(
        "am broadcast -W -n $TARGET_PACKAGE/.benchmark.BenchmarkFixtureReceiver " +
            "-a $TARGET_PACKAGE.benchmark.$action $arguments"
    )

    protected fun awaitFixtureReport(
        action: String,
        arguments: String = "",
        expectedFragment: String,
    ): String {
        var output = ""
        waitUntil("Fixture report $action did not contain '$expectedFragment': $output") {
            output = fixtureReport(action, arguments)
            expectedFragment in output
        }
        return output
    }

    protected fun userData(path: String): String {
        val output = shell(
            "am broadcast -W -n $TARGET_PACKAGE/.benchmark.BenchmarkFixtureReceiver " +
                "-a $TARGET_PACKAGE.benchmark.REPORT_USER_DATA --es path $path"
        )
        return Regex("value=([^;\"\\r\\n]+)").find(output)?.groupValues?.get(1)
            ?: error("User data value was missing from: $output")
    }

    protected fun awaitUserData(path: String, expected: String) {
        waitUntil("User data '$path' did not become '$expected'") {
            userData(path) == expected
        }
    }

    protected fun shell(command: String): String = device.executeShellCommand(command)

    protected fun visibleText(text: String, timeout: Long = UI_TIMEOUT): UiObject2 {
        val node = device.wait(Until.findObject(By.text(text)), timeout)
        assertNotNull("Expected text was not visible: $text", node)
        return node
    }

    protected fun visibleTextContaining(text: String, timeout: Long = UI_TIMEOUT): UiObject2 {
        val node = device.wait(Until.findObject(By.textContains(text)), timeout)
        assertNotNull("Expected text fragment was not visible: $text", node)
        return node
    }

    protected fun assertTextNotVisible(text: String, timeout: Long = 1_500L) {
        assertFalse(
            "Text should not be visible: $text",
            device.wait(Until.hasObject(By.text(text)), timeout),
        )
    }

    protected fun visibleDescription(description: String, timeout: Long = UI_TIMEOUT): UiObject2 {
        val node = device.wait(Until.findObject(By.desc(description)), timeout)
        assertNotNull("Expected content description was not visible: $description", node)
        return node
    }

    protected fun assertDescriptionNotVisible(description: String, timeout: Long = 1_500L) {
        assertFalse(
            "Content description should not be visible: $description",
            device.wait(Until.hasObject(By.desc(description)), timeout),
        )
    }

    protected fun allDescriptions(description: String, timeout: Long = UI_TIMEOUT): List<UiObject2> {
        val nodes = device.wait(Until.findObjects(By.desc(description)), timeout).orEmpty()
        assertTrue("No node had content description '$description'", nodes.isNotEmpty())
        return nodes
    }

    protected fun tapText(text: String) {
        repeat(3) {
            try {
                tap(visibleText(text))
                return
            } catch (_: StaleObjectException) {
                SystemClock.sleep(150)
            }
        }
        tap(visibleText(text))
    }

    protected fun tapTextContaining(text: String) = tap(visibleTextContaining(text))

    protected fun tapLastText(text: String) {
        val nodes = device.wait(Until.findObjects(By.text(text)), UI_TIMEOUT).orEmpty()
        assertTrue("Expected text was not visible: $text", nodes.isNotEmpty())
        tap(nodes.last())
    }

    protected fun tapDescription(description: String, index: Int = 0) {
        val nodes = device.wait(Until.findObjects(By.desc(description)), UI_TIMEOUT).orEmpty()
        assertTrue("Missing description '$description' at index $index", nodes.size > index)
        tap(nodes[index])
    }

    protected fun longPressText(text: String) {
        visibleText(text).longClick()
        device.waitForIdle()
    }

    protected fun tap(node: UiObject2) {
        val bounds: Rect = node.visibleBounds
        device.click(bounds.centerX(), bounds.centerY())
        device.waitForIdle()
    }

    protected fun tapClickableText(text: String) = tapClickable(visibleText(text))

    protected fun tapClickable(node: UiObject2) {
        var target = node
        while (!target.isClickable && target.parent != null) target = target.parent
        if (target.isClickable) target.click() else tap(target)
        device.waitForIdle()
    }

    protected fun tapMenuItemBelow(text: String) {
        var node = visibleText(text)
        while (!node.isClickable && node.parent != null) node = node.parent
        val bounds = node.visibleBounds
        device.click(bounds.centerX(), bounds.bottom + bounds.height() / 2)
        device.waitForIdle()
    }

    protected fun scrollToText(text: String, attempts: Int = 12): UiObject2 {
        repeat(attempts) {
            try {
                device.findObject(By.text(text))?.let { node ->
                    val centerY = node.visibleBounds.centerY()
                    val safeTop = (device.displayHeight * 0.12f).toInt()
                    val safeBottom = (device.displayHeight * 0.88f).toInt()
                    if (centerY in safeTop..safeBottom) return node
                }
            } catch (_: StaleObjectException) {
                // Compose can replace a semantics node while a list is moving.
            }

            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.78f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.32f).toInt(),
                40,
            )
            device.waitForIdle()
        }
        return visibleText(text)
    }

    protected fun tapScrolledText(text: String) {
        repeat(3) { attempt ->
            try {
                scrollToText(text)
                SystemClock.sleep(300)
                tapClickable(visibleText(text))
                return
            } catch (_: StaleObjectException) {
                if (attempt == 2) throw AssertionError("Text kept changing while tapping: $text")
                SystemClock.sleep(250)
            }
        }
    }

    protected fun openRoot(label: String) {
        tapText(label)
        visibleText(label)
    }

    protected fun openSettings() {
        launchApp()
        openRoot("Settings")
    }

    protected fun openBookshelf() {
        launchApp()
        openRoot("Bookshelf")
        visibleText("Benchmark Shelf")
    }

    protected fun openBook(title: String = PRIMARY_BOOK) {
        launchApp()
        navigateToBook(title)
    }

    protected fun navigateToBook(title: String) {
        openRoot("Bookshelf")
        tapScrolledText(title)
        visibleText(title)
    }

    protected fun openBookshelfOverflow() {
        device.click(
            (device.displayWidth * 0.94f).toInt(),
            (device.displayHeight * 0.075f).toInt(),
        )
        device.waitForIdle()
    }

    protected fun pressBack() {
        device.pressBack()
        device.waitForIdle()
    }

    protected fun assertForegroundPackage(packageName: String) {
        waitUntil("Expected foreground package '$packageName', found '${device.currentPackageName}'") {
            device.currentPackageName == packageName
        }
    }

    protected fun replaceText(index: Int, value: String) {
        val fields = device.wait(
            Until.findObjects(By.clazz("android.widget.EditText")),
            UI_TIMEOUT,
        ).orEmpty()
        assertTrue("Editable text field $index was not visible", fields.size > index)
        fields[index].text = value
        device.waitForIdle()
    }

    protected fun setSliderForText(text: String, fraction: Float) {
        if (!device.hasObject(By.desc("$text slider"))) scrollToText(text)
        var slider: UiObject2? = null
        val safeTop = (device.displayHeight * 0.18f).toInt()
        val safeBottom = (device.displayHeight * 0.78f).toInt()
        for (attempt in 0 until 8) {
            slider = device.wait(Until.findObject(By.desc("$text slider")), 600L)
            val bounds = try {
                slider?.visibleBounds
            } catch (_: StaleObjectException) {
                slider = null
                null
            }
            if (bounds != null && bounds.height() > 0 && bounds.centerY() in safeTop..safeBottom) {
                break
            }
            val direction = when {
                bounds == null -> Direction.UP
                bounds.centerY() > safeBottom -> Direction.UP
                else -> Direction.DOWN
            }
            val startY = if (direction == Direction.UP) 0.65f else 0.35f
            val endY = 0.50f
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * startY).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * endY).toInt(),
                24,
            )
            device.waitForIdle()
        }
        assertNotNull("No slider belonged to '$text'", slider)
        val bounds = try {
            slider!!.visibleBounds
        } catch (_: StaleObjectException) {
            device.findObject(By.desc("$text slider"))?.visibleBounds
        }
        assertNotNull("Slider for '$text' became stale", bounds)
        val stableBounds = bounds!!
        assertTrue(
            "Slider for '$text' was not visible",
            stableBounds.width() > 0 && stableBounds.height() > 0,
        )
        val safeFraction = fraction.coerceIn(0.05f, 0.95f)
        val targetX = stableBounds.left + (stableBounds.width() * safeFraction).toInt()
        val startFraction = if (safeFraction >= 0.5f) 0.15f else 0.85f
        device.swipe(
            stableBounds.left + (stableBounds.width() * startFraction).toInt(),
            stableBounds.centerY(),
            targetX,
            stableBounds.centerY(),
            24,
        )
        device.waitForIdle()
    }

    protected fun averageScreenColor(region: Rect): Triple<Int, Int, Int> {
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val left = region.left.coerceIn(0, screenshot.width - 1)
        val top = region.top.coerceIn(0, screenshot.height - 1)
        val right = region.right.coerceIn(left + 1, screenshot.width)
        val bottom = region.bottom.coerceIn(top + 1, screenshot.height)
        var red = 0L
        var green = 0L
        var blue = 0L
        var samples = 0L
        for (y in top until bottom step 8) {
            for (x in left until right step 8) {
                val pixel = screenshot.getPixel(x, y)
                red += android.graphics.Color.red(pixel)
                green += android.graphics.Color.green(pixel)
                blue += android.graphics.Color.blue(pixel)
                samples++
            }
        }
        screenshot.recycle()
        assertTrue("The screenshot region had no pixels", samples > 0)
        return Triple((red / samples).toInt(), (green / samples).toInt(), (blue / samples).toInt())
    }

    protected fun assertCheckable(index: Int = 0, checked: Boolean) {
        val controls = device.wait(Until.findObjects(By.checkable(true)), UI_TIMEOUT).orEmpty()
        assertTrue("Checkable control $index was not visible", controls.size > index)
        assertEquals("Unexpected checkable state at index $index", checked, controls[index].isChecked)
    }

    protected fun assertSwitchForText(text: String, checked: Boolean) {
        val control = findRelatedCheckable(text)
        assertEquals("Unexpected switch state for '$text'", checked, control.isChecked)
    }

    protected fun awaitSwitchForText(text: String, checked: Boolean) {
        waitUntil("Switch '$text' did not become checked=$checked") {
            try {
                findRelatedCheckable(text).isChecked == checked
            } catch (_: StaleObjectException) {
                false
            }
        }
    }

    protected fun isSwitchCheckedForText(text: String): Boolean = findRelatedCheckable(text).isChecked

    protected fun tapSwitchForText(text: String) {
        SystemClock.sleep(350)
        tapClickableText(text)
    }

    private fun findRelatedCheckable(text: String): UiObject2 {
        var container = visibleText(text)
        val labelCenter = container.visibleBounds.centerY()
        device.findObjects(By.clazz("android.widget.Switch"))
            .minByOrNull { kotlin.math.abs(it.visibleBounds.centerY() - labelCenter) }
            ?.takeIf {
                kotlin.math.abs(it.visibleBounds.centerY() - labelCenter) < device.displayHeight / 6
            }
            ?.let { return it }
        repeat(6) {
            if (container.isCheckable) return container
            container.children.firstOrNull { it.isCheckable }?.let { return it }
            container = container.parent ?: return@repeat
        }
        container.findObject(By.checkable(true))?.let { return it }
        error("No checkable control belongs to '$text'")
    }

    protected fun tapFirstCheckable() {
        val control = device.wait(Until.findObject(By.checkable(true)), UI_TIMEOUT)
        assertNotNull("No checkable control was visible", control)
        tap(control)
    }

    protected fun waitUntil(
        message: String,
        timeout: Long = UI_TIMEOUT,
        interval: Long = 250L,
        condition: () -> Boolean,
    ) {
        val deadline = SystemClock.elapsedRealtime() + timeout
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return
            SystemClock.sleep(interval)
        }
        assertTrue(message, condition())
    }

    companion object {
        const val TARGET_PACKAGE = "indi.dmzz_yyhyy.lightnovelreader"
        const val DOCUMENTS_PACKAGE = "com.android.documentsui"
        const val PRIMARY_BOOK = "Benchmark Sample Novel"
        const val SECONDARY_BOOK = "Second Benchmark Novel"
        const val UI_TIMEOUT = 10_000L
        const val NETWORK_TIMEOUT = 45_000L
    }
}
