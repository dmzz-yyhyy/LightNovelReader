package indi.dmzz_yyhyy.lightnovelreader.benchmark.framework

import android.os.SystemClock
import android.view.KeyEvent
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import java.util.regex.Pattern
import kotlin.math.abs

/** Reader-specific probes and gestures kept out of individual scenario files. */
abstract class ReaderTestCase : BenchmarkTestCase() {
    protected fun openChapter(
        chapter: String = "Benchmark Chapter One",
        expectedContent: String = "Benchmark progress paragraph",
        book: String = PRIMARY_BOOK,
    ) {
        openBook(book)
        tapScrolledText(chapter)
        visibleTextContaining(expectedContent)
    }

    protected fun revealReaderChrome() {
        repeat(3) {
            if (device.hasObject(By.desc("setting"))) return
            device.click(device.displayWidth / 2, device.displayHeight / 2)
            device.wait(Until.hasObject(By.desc("setting")), 1_000L)
        }
        visibleDescription("setting")
    }

    protected fun hideReaderChrome() {
        repeat(3) {
            if (!device.hasObject(By.desc("setting"))) return
            device.click(device.displayWidth / 2, device.displayHeight / 2)
            device.wait(Until.gone(By.desc("setting")), 1_000L)
        }
        assertTrue(
            "Reader chrome did not close",
            device.wait(Until.gone(By.desc("setting")), UI_TIMEOUT),
        )
    }

    protected fun expandReaderSettingsSheet() = expandBottomSheet("Reader Settings")

    protected fun expandBottomSheet(titleText: String) {
        val title = visibleText(titleText)
        val initialTop = title.visibleBounds.top
        if (initialTop > device.displayHeight / 4) {
            device.swipe(
                title.visibleBounds.centerX(),
                title.visibleBounds.centerY(),
                title.visibleBounds.centerX(),
                (device.displayHeight * 0.12f).toInt(),
                80,
            )
            waitUntil("'$titleText' sheet did not expand") {
                try {
                    visibleText(titleText, 500L).visibleBounds.top < initialTop
                } catch (_: AssertionError) {
                    false
                }
            }
        }
    }

    protected fun scrollReader(times: Int) {
        repeat(times) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.78f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.28f).toInt(),
                35,
            )
            device.waitForIdle()
            SystemClock.sleep(250)
        }
        SystemClock.sleep(750)
    }

    protected fun centeredText(prefix: String): String {
        repeat(40) {
            currentCenteredText(prefix)?.let { return it }
            SystemClock.sleep(250)
        }
        error("No visible reader text starts with '$prefix'")
    }

    protected fun waitForCenteredText(prefix: String, expected: String): String {
        repeat(40) {
            if (currentCenteredText(prefix) == expected) return expected
            SystemClock.sleep(250)
        }
        return centeredText(prefix)
    }

    private fun currentCenteredText(prefix: String): String? {
        val center = device.displayHeight / 2
        return try {
            device.findObjects(By.textStartsWith(prefix))
                .mapNotNull { node ->
                    val bounds = node.visibleBounds
                    if (bounds.height() <= 0) null else node.text to abs(bounds.centerY() - center)
                }
                .minByOrNull { it.second }
                ?.first
        } catch (_: StaleObjectException) {
            null
        }
    }

    protected fun savedProgress(): Float {
        val output = shell(
            "am broadcast -W -n $TARGET_PACKAGE/.benchmark.BenchmarkFixtureReceiver " +
                "-a $TARGET_PACKAGE.benchmark.REPORT_PROGRESS"
        )
        return Regex("progress=([0-9.-]+)").find(output)
            ?.groupValues?.get(1)?.toFloatOrNull()
            ?: error("Progress was missing from: $output")
    }

    protected fun waitForProgressAfter(previous: Float): Float {
        repeat(40) {
            savedProgress().takeIf { it > previous }?.let { return it }
            SystemClock.sleep(250)
        }
        return savedProgress()
    }

    protected fun enablePageMode(
        volumeKeys: Boolean = false,
        tapZones: Boolean = false,
        noAnimation: Boolean = false,
    ) {
        revealReaderChrome()
        tapDescription("setting")
        expandReaderSettingsSheet()
        tapText("Controls")
        ensureReaderSwitchEnabled("Page Turn Mode", FLIP_PAGE_PATH)
        if (volumeKeys) {
            ensureReaderSwitchEnabled("Volume Key Navigation", VOLUME_KEY_FLIP_PATH)
        }
        if (tapZones) {
            ensureReaderSwitchEnabled("Tap to Turn Pages", TAP_TO_FLIP_PATH)
        }
        if (noAnimation) {
            scrollToText("Page Turn Animation")
            tapText("Page Turn Animation")
            tapText("None")
        }
        if (device.hasObject(By.text("Reader Settings"))) pressBack()
        repeat(3) {
            if (device.wait(Until.gone(By.text("Reader Settings")), 1_000L)) return@repeat
            pressBack()
        }
        assertTrue(
            "Reader settings did not close",
            device.wait(Until.gone(By.text("Reader Settings")), UI_TIMEOUT),
        )
        hideReaderChrome()
        SystemClock.sleep(750)
        waitForPagerReady()
    }

    private fun ensureReaderSwitchEnabled(label: String, userDataPath: String) {
        val wasEnabled = userData(userDataPath) == "true"
        scrollToText(label)
        if (!wasEnabled) tapSwitchForText(label)
        awaitUserData(userDataPath, "true")
    }

    protected fun swipePage(forward: Boolean, steps: Int = 4, settleMs: Long = 350L) {
        device.swipe(
            if (forward) device.displayWidth * 5 / 6 else device.displayWidth / 6,
            device.displayHeight / 2,
            if (forward) device.displayWidth / 6 else device.displayWidth * 5 / 6,
            device.displayHeight / 2,
            steps,
        )
        if (settleMs >= 100) device.waitForIdle()
        SystemClock.sleep(settleMs)
    }

    protected fun tapPage(forward: Boolean) {
        device.click(
            if (forward) device.displayWidth * 5 / 6 else device.displayWidth / 6,
            device.displayHeight / 2,
        )
    }

    protected fun pressVolume(forward: Boolean) {
        device.pressKeyCode(if (forward) KeyEvent.KEYCODE_VOLUME_DOWN else KeyEvent.KEYCODE_VOLUME_UP)
        SystemClock.sleep(500)
    }

    protected fun flipPage(): String {
        repeat(40) {
            device.findObjects(By.res(Pattern.compile(".*flip-page-.*")))
                .mapNotNull { it.resourceName }
                .firstOrNull { !it.endsWith("-pending") }
                ?.let { return it }
            SystemClock.sleep(250)
        }
        error("No resolved flip page tag is visible")
    }

    protected fun flipChapter(): String {
        repeat(40) {
            device.findObjects(By.res(Pattern.compile(".*flip-chapter-.*")))
                .mapNotNull { it.resourceName }.firstOrNull()?.let { return it }
            SystemClock.sleep(250)
        }
        error("No active flip chapter tag is visible")
    }

    protected fun waitForPage(expected: String): String {
        repeat(40) {
            flipPage().takeIf { it == expected }?.let { return it }
            SystemClock.sleep(250)
        }
        return flipPage()
    }

    protected fun waitForDifferentPage(previous: String): String {
        repeat(40) {
            flipPage().takeIf { it != previous }?.let { return it }
            SystemClock.sleep(250)
        }
        return flipPage()
    }

    protected fun waitForChapter(chapterId: String): String {
        val suffix = "flip-chapter-$chapterId"
        repeat(40) {
            flipChapter().takeIf { it.endsWith(suffix) }?.let { return it }
            SystemClock.sleep(250)
        }
        return flipChapter()
    }

    protected fun turnUntilChapter(chapterId: String, forward: Boolean, maxTurns: Int = 40) {
        val suffix = "flip-chapter-$chapterId"
        repeat(maxTurns) {
            if (flipChapter().endsWith(suffix)) return
            swipePage(forward)
        }
        error("Pager did not reach $chapterId: ${flipState()}")
    }

    protected fun reachBoundary(forward: Boolean, maxTurns: Int = 30): String {
        var previous = flipPage()
        repeat(maxTurns) {
            swipePage(forward)
            val current = flipPage()
            if (current == previous) return current
            previous = current
        }
        error("Pager did not reach the ${if (forward) "last" else "first"} page")
    }

    protected fun waitForPagerIdle(timeout: Long = 5_000L) {
        waitUntil("Flip pager stayed locked: ${flipState()}", timeout, 50L) {
            val state = flipState()
            state.contains("animating=false") && state.contains("direction=0")
        }
    }

    protected fun waitForPagerReady(timeout: Long = 15_000L) {
        waitUntil("Flip pager did not paginate: ${flipState()}", timeout, 100L) {
            val state = flipState()
            val pageCount = Regex("count=(\\d+)").find(state)
                ?.groupValues?.get(1)?.toIntOrNull() ?: 0
            pageCount >= 2 &&
                state.contains("animating=false") &&
                state.contains("direction=0")
        }
    }

    protected fun flipState(): String =
        device.findObjects(By.desc(Pattern.compile("flip-state-.*")))
            .mapNotNull { it.contentDescription }.firstOrNull() ?: "missing"

    companion object {
        private const val FLIP_PAGE_PATH = "reader.is_using_flip_page"
        private const val TAP_TO_FLIP_PATH = "reader.is_using_click_flip_page"
        private const val VOLUME_KEY_FLIP_PATH = "reader.is_using_volume_key_flip"
    }
}
