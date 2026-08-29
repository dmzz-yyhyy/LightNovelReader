package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until
import indi.dmzz_yyhyy.lightnovelreader.benchmark.ui.UiAutomatorTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern
import kotlin.math.abs

@LargeTest
@RunWith(AndroidJUnit4::class)
class BookAndReaderTest : UiAutomatorTest() {
    private fun localizedText(english: String, simplifiedChinese: String): String =
        if (java.util.Locale.getDefault().language == "zh") simplifiedChinese else english

    private fun openBookDetails(title: String = "Benchmark Sample Novel") {
        launchApp()
        navigateToBookDetails(title)
    }

    private fun navigateToBookDetails(title: String) {
        openBottomNavigation(localizedText("Bookshelf", "书架"))
        clickScrolledText(title)
        assertText(title)
    }

    private fun swipeReaderDown(times: Int) {
        repeat(times) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.78).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.28).toInt(),
                35,
            )
            device.waitForIdle()
            SystemClock.sleep(250)
        }
        // Progress persistence is debounced in both reader modes.
        SystemClock.sleep(1_000)
    }

    private fun currentCenteredVisibleText(prefix: String): String? {
        val centerY = device.displayHeight / 2
        return try {
            device.findObjects(By.textStartsWith(prefix))
                .mapNotNull { object2 ->
                    val bounds = object2.visibleBounds
                    if (bounds.height() <= 0) null
                    else object2.text to abs(bounds.centerY() - centerY)
                }
                .minByOrNull { it.second }
                ?.first
        } catch (_: StaleObjectException) {
            null
        }
    }

    private fun centeredVisibleText(prefix: String): String {
        repeat(40) {
            currentCenteredVisibleText(prefix)?.let { return it }
            SystemClock.sleep(250)
        }
        assertTrue("Expected a visible progress marker starting with: $prefix", false)
        error("unreachable")
    }

    private fun waitForCenteredVisibleText(prefix: String, expected: String): String {
        repeat(40) {
            if (currentCenteredVisibleText(prefix) == expected) return expected
            SystemClock.sleep(250)
        }
        return centeredVisibleText(prefix)
    }

    private fun persistedChapterProgress(): Float {
        val output = shell(
            "am broadcast -W -n $TARGET_PACKAGE/.benchmark.BenchmarkFixtureReceiver " +
                "-a $TARGET_PACKAGE.benchmark.REPORT_PROGRESS"
        )
        return Regex("progress=([0-9.-]+)").find(output)
            ?.groupValues
            ?.get(1)
            ?.toFloatOrNull()
            ?: error("Progress result was missing from: $output")
    }

    private fun waitForProgressAfter(previous: Float): Float {
        repeat(40) {
            val progress = persistedChapterProgress()
            if (progress > previous) return progress
            SystemClock.sleep(250)
        }
        return persistedChapterProgress()
    }

    private fun currentFlipPageTag(): String {
        repeat(40) {
            val tag = device.findObjects(By.res(Pattern.compile(".*flip-page-.*")))
                .mapNotNull { it.resourceName }
                .firstOrNull { !it.endsWith("-pending") }
            if (tag != null) return tag
            SystemClock.sleep(250)
        }
        error("No resolved flip-page state tag was visible")
    }

    private fun waitForDifferentFlipPage(previous: String): String {
        repeat(40) {
            val current = currentFlipPageTag()
            if (current != previous) return current
            SystemClock.sleep(250)
        }
        return currentFlipPageTag()
    }

    private fun waitForFlipPage(expected: String): String {
        repeat(40) {
            val current = currentFlipPageTag()
            if (current == expected) return current
            SystemClock.sleep(250)
        }
        return currentFlipPageTag()
    }

    private fun currentFlipChapterTag(): String {
        repeat(40) {
            device.findObjects(By.res(Pattern.compile(".*flip-chapter-.*")))
                .mapNotNull { it.resourceName }
                .firstOrNull()
                ?.let { return it }
            SystemClock.sleep(250)
        }
        error("No active flip chapter state tag was visible")
    }

    private fun waitForFlipChapter(chapterId: String): String {
        val expectedSuffix = "flip-chapter-$chapterId"
        repeat(40) {
            val current = currentFlipChapterTag()
            if (current.endsWith(expectedSuffix)) return current
            SystemClock.sleep(250)
        }
        return currentFlipChapterTag()
    }

    private fun turnPagesUntilChapter(
        chapterId: String,
        forward: Boolean,
        maxTurns: Int = 40,
    ) {
        val expectedSuffix = "flip-chapter-$chapterId"
        repeat(maxTurns) {
            if (currentFlipChapterTag().endsWith(expectedSuffix)) return
            swipePage(forward)
        }
        error(
            "Pager did not reach chapter $chapterId; " +
                "chapter=${currentFlipChapterTag()} page=${currentFlipPageTag()} " +
                "state=${currentFlipStateDescription()}"
        )
    }

    private fun currentFlipStateDescription(): String =
        device.findObjects(By.desc(Pattern.compile("flip-state-.*")))
            .mapNotNull { it.contentDescription }
            .firstOrNull()
            ?: "missing"

    private fun waitForFlipPagerIdle(timeoutMs: Long = 5_000L) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        var lastState = currentFlipStateDescription()
        while (SystemClock.uptimeMillis() < deadline) {
            lastState = currentFlipStateDescription()
            if (lastState.contains("animating=false") && lastState.contains("direction=0")) {
                SystemClock.sleep(100)
                val confirmed = currentFlipStateDescription()
                if (confirmed.contains("animating=false") && confirmed.contains("direction=0")) {
                    return
                }
                lastState = confirmed
            }
            SystemClock.sleep(50)
        }
        assertTrue("Flip pager input lock did not recover: $lastState", false)
    }

    private fun swipePage(
        forward: Boolean,
        steps: Int = 20,
        settleMs: Long = 350,
    ) {
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

    private fun reachPageBoundary(forward: Boolean, maxTurns: Int = 30): String {
        var previous = currentFlipPageTag()
        repeat(maxTurns) {
            swipePage(forward)
            val current = currentFlipPageTag()
            if (current == previous) return current
            previous = current
        }
        error("Flip pager did not reach its ${if (forward) "end" else "start"} boundary")
    }

    private fun enablePageTurnMode(
        enableVolumeKeys: Boolean = false,
        enableTapToTurn: Boolean = false,
        disableAnimation: Boolean = false,
    ) {
        device.click(device.displayWidth / 2, device.displayHeight / 2)
        clickDescription("setting")
        clickText(localizedText("Controls", "操作"))
        clickText(localizedText("Page Turn Mode", "翻页模式"))
        scrollToText(localizedText("Volume Key Navigation", "音量键翻页"))
        if (enableVolumeKeys) {
            clickText(localizedText("Volume Key Navigation", "音量键翻页"))
        }
        if (enableTapToTurn) {
            scrollToText(localizedText("Tap to Turn Pages", "点击翻页"))
            clickText(localizedText("Tap to Turn Pages", "点击翻页"))
        }
        if (disableAnimation) {
            scrollToText(localizedText("Page Turn Animation", "翻页动画"))
            clickText(localizedText("Page Turn Animation", "翻页动画"))
            clickText(localizedText("None", "无"))
        }
        pressBack()
        val settingsTitle = localizedText("Reader Settings", "阅读设置")
        repeat(3) {
            if (device.wait(Until.gone(By.text(settingsTitle)), 1_000L)) return@repeat
            // A settings-list scroll or an open option popup can consume a back event on some
            // Android 12 vendor builds. Retry only while the settings sheet is still present.
            pressBack()
        }
        assertTrue(
            "Reader settings did not close",
            device.wait(Until.gone(By.text(settingsTitle)), TIMEOUT),
        )
        device.click(device.displayWidth / 2, device.displayHeight / 2)
        device.waitForIdle()
        SystemClock.sleep(1_000)
    }

    private fun turnPagesUntilText(
        targetText: String,
        forward: Boolean,
        maxTurns: Int = 40,
    ) {
        repeat(maxTurns) {
            if (device.hasObject(By.textContains(targetText))) return
            device.swipe(
                if (forward) device.displayWidth * 5 / 6 else device.displayWidth / 6,
                device.displayHeight / 2,
                if (forward) device.displayWidth / 6 else device.displayWidth * 5 / 6,
                device.displayHeight / 2,
                20,
            )
            if (device.wait(Until.hasObject(By.textContains(targetText)), 1_000L)) return
            // Keep the input cadence below the page animation/chapter-load boundary so the
            // test cannot repeatedly cancel the transition it is trying to observe.
            SystemClock.sleep(250)
        }
        assertTextContains(targetText)
    }

    @Test
    fun detailActionsOpenExportFormattingAndMoreMenus() {
        openBookDetails()

        clickDescription("export")
        assertText("Export as Epub")
        pressBack()

        clickDescription("formatting")
        assertText("Book Rules")
        pressBack()

        clickDescription("more")
        assertText("Mark as read…")
        pressBack()
    }

    @Test
    fun chapterSelectionAndReaderControlsWork() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")

        device.click(device.displayWidth / 2, device.displayHeight / 2)
        assertDescription("menu")
        assertDescription("setting")
        assertDescription("mark")

        clickDescription("menu")
        assertText("Select Chapter")
        assertText("Benchmark Chapter One")
        clickCenter(scrollToText("Benchmark Bonus Volume"))
        clickCenter(scrollToText("Benchmark Chapter Two"))
        assertForegroundPackage(TARGET_PACKAGE)
    }

    @Test
    fun readerSettingsExposeAllGroupsAndPageModes() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        device.click(device.displayWidth / 2, device.displayHeight / 2)
        clickDescription("setting")

        assertText("Reader Settings")
        assertText("Appearance")
        assertText("Controls")
        assertText("Margins")
        assertText("Keep Screen On")
        assertText("Hide Status Bar")
        assertText("Theme Settings…")

        clickText("Controls")
        assertText("Page Turn Mode")
        assertText("Back Prevention")
    }

    @Test
    fun scrollingAndVolumeNavigationKeepReaderResponsive() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")

        device.swipe(
            device.displayWidth / 2,
            (device.displayHeight * 0.75).toInt(),
            device.displayWidth / 2,
            (device.displayHeight * 0.25).toInt(),
            30,
        )
        device.pressKeyCode(android.view.KeyEvent.KEYCODE_VOLUME_DOWN)
        device.pressKeyCode(android.view.KeyEvent.KEYCODE_VOLUME_UP)
        assertTextContains("Benchmark progress paragraph")
    }

    @Test
    fun bookInformationSheetShowsEveryMetadataGroup() {
        openBookDetails()
        clickScrolledText("Info")

        assertText("Title")
        assertText("Benchmark Sample Novel")
        assertText("ID")
        assertText("9999999")
        assertText("Author")
        assertText("Benchmark Author")
        scrollToText("Stats")
        assertTextContains("12")
        assertTextContains("2 chapters")
    }

    @Test
    fun epubExportOptionsSupportVolumeAndSelectionBranches() {
        openBookDetails()
        clickDescription("export")

        assertText("Export as Epub")
        assertText("Include images")
        assertText("Export by volumes")
        clickText("Export by volumes")
        assertText("Benchmark Volume")
        assertText("Benchmark Bonus Volume")
        clickText("Benchmark Volume")
        assertText("Select All")
        clickText("Select All")
        assertTextNotVisible("Select All")
    }

    @Test
    fun markReadDialogSupportsRangeSelectionAndConfirmation() {
        openBookDetails()
        clickDescription("more")
        clickText("Mark as read…")

        assertText("全部章节")
        assertText("选择范围")
        clickText("选择范围")
        assertText("Benchmark Chapter One")
        assertText("Benchmark Chapter Two")
        clickScrolledText("Benchmark Chapter One")
        clickText("Benchmark Chapter Two")
        assertText("标记已选 2 章为已读")
        clickText("标记已选 2 章为已读")

        assertText("Benchmark Sample Novel")
        device.waitForIdle(2_000)
        restartApp()
        openBottomNavigation("Bookshelf")
        clickText("Benchmark Sample Novel")
        scrollToText("Finished Reading")
    }

    @Test
    fun markReadDialogCanBeCancelledWithoutChangingProgress() {
        openBookDetails()
        clickDescription("more")
        clickText("Mark as read…")
        clickText("取消")

        assertText("Benchmark Sample Novel")
        clickDescription("more")
        clickText("Mark as read…")
        assertText("标记全部为已读")
    }

    @Test
    fun readerModeSwitchesExposeConditionalControlsAndMargins() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        device.click(device.displayWidth / 2, device.displayHeight / 2)
        clickDescription("setting")

        clickText("Controls")
        assertText("Page Turn Mode")
        assertText("Switch between scrolling mode and page turn mode")
        clickText("Page Turn Mode")
        assertText("Volume Key Navigation")
        scrollToText("Tap to Turn Pages")
        assertText("Page Turn Animation")

        clickText("Margins")
        assertText("Auto Margin Adjustment")
        clickText("Auto Margin Adjustment")
        assertText("Top Margin")
        assertText("Bottom Margin")
        scrollToText("Right Margin")
        assertText("Left Margin")
    }

    @Test
    fun pageTurnModeSeamlesslyTransitionsBetweenChaptersInBothDirections() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")

        device.click(device.displayWidth / 2, device.displayHeight / 2)
        clickDescription("setting")
        clickText("Controls")
        clickText("Page Turn Mode")
        assertText("Volume Key Navigation")

        pressBack()
        device.wait(Until.gone(By.text("Reader Settings")), TIMEOUT)
        device.click(device.displayWidth / 2, device.displayHeight / 2)
        device.waitForIdle()

        turnPagesUntilText("Benchmark chapter two start marker", forward = true)
        assertTextContains("Benchmark chapter two start marker")
        assertForegroundPackage(TARGET_PACKAGE)

        turnPagesUntilText("Benchmark chapter one end marker", forward = false)
        assertTextContains("Benchmark chapter one end marker")
        assertForegroundPackage(TARGET_PACKAGE)
    }

    @Test
    fun pageTurnModeHandlesRepeatedSeamlessRoundTripsAndBookBoundaries() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        enablePageTurnMode()

        waitForFlipChapter("benchmark-chapter-1")
        val firstPage = reachPageBoundary(forward = false)
        repeat(3) {
            swipePage(forward = false)
            assertEquals("Swiping before the first book page must be a no-op", firstPage, currentFlipPageTag())
            assertTrue(currentFlipChapterTag().endsWith("flip-chapter-benchmark-chapter-1"))
        }

        turnPagesUntilChapter("benchmark-chapter-2", forward = true)
        waitForFlipChapter("benchmark-chapter-2")
        assertTextContains("Benchmark chapter two start marker")
        repeat(3) {
            turnPagesUntilChapter("benchmark-chapter-1", forward = false)
            waitForFlipChapter("benchmark-chapter-1")
            assertTextContains("Benchmark chapter one end marker")

            swipePage(forward = true)
            waitForFlipChapter("benchmark-chapter-2")
            assertTextContains("Benchmark chapter two start marker")
        }

        val lastPage = reachPageBoundary(forward = true)
        repeat(3) {
            swipePage(forward = true)
            assertEquals("Swiping after the final book page must be a no-op", lastPage, currentFlipPageTag())
            assertTrue(currentFlipChapterTag().endsWith("flip-chapter-benchmark-chapter-2"))
        }

        swipePage(forward = false)
        val pageBeforeLast = currentFlipPageTag()
        assertNotEquals(lastPage, pageBeforeLast)
        swipePage(forward = true)
        assertEquals("The final page must remain reachable after leaving it", lastPage, waitForFlipPage(lastPage))
    }

    @Test
    fun pageTurnModeSurvivesRapidContinuousTurnsAcrossChapters() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        enablePageTurnMode()
        reachPageBoundary(forward = false)

        repeat(24) { swipePage(forward = true, steps = 2, settleMs = 25) }
        waitForFlipPagerIdle()
        val finalBookPage = reachPageBoundary(forward = true)
        assertTrue(
            "Rapid forward turns stopped before chapter two: ${currentFlipStateDescription()}",
            currentFlipChapterTag().endsWith("flip-chapter-benchmark-chapter-2"),
        )
        assertForegroundPackage(TARGET_PACKAGE)

        repeat(24) { swipePage(forward = false, steps = 2, settleMs = 25) }
        waitForFlipPagerIdle()
        val firstBookPage = reachPageBoundary(forward = false)
        assertTrue(
            "Rapid backward turns stopped before chapter one: ${currentFlipStateDescription()}",
            currentFlipChapterTag().endsWith("flip-chapter-benchmark-chapter-1"),
        )
        assertNotEquals(finalBookPage, firstBookPage)
        assertForegroundPackage(TARGET_PACKAGE)

        swipePage(forward = true)
        assertNotEquals(
            "Pager must remain interactive after rapid cross-chapter input",
            firstBookPage,
            waitForDifferentFlipPage(firstBookPage),
        )
    }

    @Test
    fun pageTurnModeWithoutAnimationKeepsSeamlessChapterBoundariesInteractive() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        enablePageTurnMode(disableAnimation = true)

        turnPagesUntilChapter("benchmark-chapter-2", forward = true)
        waitForFlipChapter("benchmark-chapter-2")
        assertTextContains("Benchmark chapter two start marker")

        turnPagesUntilChapter("benchmark-chapter-1", forward = false)
        waitForFlipChapter("benchmark-chapter-1")
        assertTextContains("Benchmark chapter one end marker")

        val chapterOneLastPage = currentFlipPageTag()
        swipePage(forward = true)
        waitForFlipChapter("benchmark-chapter-2")
        swipePage(forward = false)
        waitForFlipChapter("benchmark-chapter-1")
        assertEquals(chapterOneLastPage, waitForFlipPage(chapterOneLastPage))
    }

    @Test
    fun pageTurnModeWithoutAnimationSurvivesRapidTapTurnsAcrossChapter() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        enablePageTurnMode(enableTapToTurn = true, disableAnimation = true)

        repeat(24) {
            device.click(device.displayWidth * 5 / 6, device.displayHeight / 2)
            SystemClock.sleep(20)
        }
        waitForFlipChapter("benchmark-chapter-2")
        waitForFlipPagerIdle()
        assertTrue(
            "Rapid no-animation taps must cross the seamless chapter boundary: " +
                currentFlipStateDescription(),
            currentFlipChapterTag().endsWith("flip-chapter-benchmark-chapter-2"),
        )

        val finalPage = reachPageBoundary(forward = true)
        device.click(device.displayWidth / 6, device.displayHeight / 2)
        assertNotEquals(
            "The pager must still accept taps after the rapid seamless transition",
            finalPage,
            waitForDifferentFlipPage(finalPage),
        )
    }

    @Test
    fun pageTurnModeWithoutAnimationSurvivesRapidTapTurnsAcrossManyChapters() {
        val fixtureResult = shell(
            "am broadcast -W -n $TARGET_PACKAGE/.benchmark.BenchmarkFixtureReceiver " +
                "-a $TARGET_PACKAGE.benchmark.EXTEND_RAPID_CHAPTER_CHAIN"
        )
        assertTrue(
            "Failed to extend the rapid-turn chapter chain: $fixtureResult",
            fixtureResult.contains("rapid-chapters=SUCCEEDED"),
        )

        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        enablePageTurnMode(enableTapToTurn = true, disableAnimation = true)

        // Send a zero-delay burst larger than Channel.BUFFERED's default capacity. The pager
        // must preserve the input while chapters 2-6 are measured and installed.
        repeat(180) {
            device.click(device.displayWidth * 5 / 6, device.displayHeight / 2)
        }

        val reachedChapter = waitForFlipChapter("benchmark-chapter-6")
        SystemClock.sleep(1_000)
        waitForFlipPagerIdle()
        assertTrue(
            "Rapid taps must cross five seamless chapter boundaries: " +
                currentFlipStateDescription(),
            reachedChapter.endsWith("flip-chapter-benchmark-chapter-6") &&
                currentFlipChapterTag().endsWith("flip-chapter-benchmark-chapter-6"),
        )

        val lastPage = reachPageBoundary(forward = true)
        device.click(device.displayWidth / 6, device.displayHeight / 2)
        assertNotEquals(
            "The pager must still accept reverse taps after crossing several chapters",
            lastPage,
            waitForDifferentFlipPage(lastPage),
        )
    }

    @Test
    fun readerAppearanceTogglesPersistAcrossReaderReentry() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        device.click(device.displayWidth / 2, device.displayHeight / 2)
        clickDescription("setting")
        assertText("Keep Screen On")
        clickText("Keep Screen On")
        assertFirstSwitchChecked(true)

        pressBack()
        pressBack()
        clickScrolledText("Benchmark Chapter One")
        device.click(device.displayWidth / 2, device.displayHeight / 2)
        clickDescription("setting")
        assertFirstSwitchChecked(true)
    }

    @Test
    fun scrollingModeRestoresTheSameComponentAfterProcessRestart() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")

        swipeReaderDown(times = 1)
        val expectedAnchor = centeredVisibleText("Benchmark progress paragraph")

        restartApp()
        clickText(localizedText("Resume Last Reading", "继续上次阅读"))
        assertTextContains("Benchmark progress paragraph")
        SystemClock.sleep(1_000)

        assertEquals(
            "Scrolling mode should restore the same component near the viewport center",
            expectedAnchor,
            waitForCenteredVisibleText("Benchmark progress paragraph", expectedAnchor),
        )
    }

    @Test
    fun pageTurnModeRestoresTheSameComponentAfterProcessRestart() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        enablePageTurnMode()

        repeat(1) {
            device.swipe(
                device.displayWidth * 5 / 6,
                device.displayHeight / 2,
                device.displayWidth / 6,
                device.displayHeight / 2,
                20,
            )
            SystemClock.sleep(500)
        }
        SystemClock.sleep(1_000)
        val expectedAnchor = centeredVisibleText("Benchmark progress paragraph")

        restartApp()
        clickText(localizedText("Resume Last Reading", "继续上次阅读"))
        assertTextContains("Benchmark progress paragraph")

        assertEquals(
            "Page-turn mode should restore the page containing the saved component hash",
            expectedAnchor,
            waitForCenteredVisibleText("Benchmark progress paragraph", expectedAnchor),
        )

        device.swipe(
            device.displayWidth * 5 / 6,
            device.displayHeight / 2,
            device.displayWidth / 6,
            device.displayHeight / 2,
            20,
        )
        SystemClock.sleep(1_000)
        assertNotEquals(
            "The pager must remain interactive after restoring saved progress",
            expectedAnchor,
            centeredVisibleText("Benchmark progress paragraph"),
        )
    }

    @Test
    fun pageTurnModeCanTurnAfterReturningToDirectoryAndReenteringSameChapter() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        enablePageTurnMode()
        val initialProgress = persistedChapterProgress()
        val initialPage = currentFlipPageTag()

        device.swipe(
            device.displayWidth * 5 / 6,
            device.displayHeight / 2,
            device.displayWidth / 6,
            device.displayHeight / 2,
            20,
        )
        val savedPage = waitForDifferentFlipPage(initialPage)
        val savedProgress = waitForProgressAfter(initialProgress)
        assertTrue(
            "The setup must persist a page after the initially restored page",
            savedProgress > initialProgress,
        )

        pressBack()
        assertText("Benchmark Sample Novel")
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        assertEquals(
            "The restored picture and pagerState must reference the same saved page",
            savedPage,
            waitForFlipPage(savedPage),
        )
        SystemClock.sleep(500)
        assertEquals(savedProgress, persistedChapterProgress())

        pressBack()
        assertText("Benchmark Sample Novel")
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        assertEquals(savedPage, waitForFlipPage(savedPage))
        SystemClock.sleep(500)
        assertEquals(savedProgress, persistedChapterProgress())

        device.swipe(
            device.displayWidth * 5 / 6,
            device.displayHeight / 2,
            device.displayWidth / 6,
            device.displayHeight / 2,
            20,
        )
        val advancedPage = waitForDifferentFlipPage(savedPage)
        val advancedProgress = waitForProgressAfter(savedProgress)
        assertTrue(
            "The reused pager must remain interactive after same-chapter restoration",
            advancedProgress > savedProgress,
        )

        shell(
            "am broadcast -W -n $TARGET_PACKAGE/.benchmark.BenchmarkFixtureReceiver " +
                "-a $TARGET_PACKAGE.benchmark.REEMIT_CHAPTER"
        )
        SystemClock.sleep(1_000)
        assertEquals(advancedPage, currentFlipPageTag())
        assertEquals(
            "A repeated chapter Flow emission must not replay the original restore target",
            advancedProgress,
            persistedChapterProgress(),
        )

        device.swipe(
            device.displayWidth * 5 / 6,
            device.displayHeight / 2,
            device.displayWidth / 6,
            device.displayHeight / 2,
            20,
        )
        val finalPage = waitForDifferentFlipPage(advancedPage)
        val finalProgress = waitForProgressAfter(advancedProgress)
        assertTrue(
            "The pager must keep advancing after the repeated chapter emission",
            finalProgress > advancedProgress,
        )
        assertNotEquals(advancedPage, finalPage)
    }

    @Test
    fun pageTurnModeRespondsToVolumeKeys() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        enablePageTurnMode(enableVolumeKeys = true)

        val initialPage = currentFlipPageTag()
        device.pressKeyCode(android.view.KeyEvent.KEYCODE_VOLUME_DOWN)
        val nextPage = waitForDifferentFlipPage(initialPage)
        assertNotEquals(initialPage, nextPage)

        device.pressKeyCode(android.view.KeyEvent.KEYCODE_VOLUME_UP)
        assertEquals(initialPage, waitForFlipPage(initialPage))
    }

    @Test
    fun pageTurnModeWithoutAnimationRemainsInteractiveAfterRapidVolumeInput() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        assertTextContains("Benchmark progress paragraph")
        enablePageTurnMode(enableVolumeKeys = true, disableAnimation = true)

        repeat(24) {
            device.pressKeyCode(android.view.KeyEvent.KEYCODE_VOLUME_DOWN)
        }
        waitForFlipPagerIdle()
        turnPagesUntilChapter("benchmark-chapter-2", forward = true)
        val lastPage = reachPageBoundary(forward = true)

        device.pressKeyCode(android.view.KeyEvent.KEYCODE_VOLUME_UP)
        assertNotEquals(
            "The book-end long-press producer must not keep overwriting reverse input",
            lastPage,
            waitForDifferentFlipPage(lastPage),
        )
        assertForegroundPackage(TARGET_PACKAGE)
    }

    @Test
    fun readerTextCanBeSelectedAndCopied() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        val paragraph = assertTextContains("Benchmark progress paragraph")

        paragraph.longClick()
        assertTrue(
            "Long-pressing reader text must open the system copy action",
            device.wait(
                Until.hasObject(By.text(Pattern.compile("Copy|复制"))),
                TIMEOUT,
            ),
        )
    }

    @Test
    fun progressAndLastChapterRemainIndependentAcrossMultipleBooks() {
        openBookDetails()
        clickScrolledText("Benchmark Chapter One")
        swipeReaderDown(times = 2)
        val firstBookAnchor = centeredVisibleText("Benchmark progress paragraph")

        restartApp()
        navigateToBookDetails("Second Benchmark Novel")
        clickScrolledText("Second Book Chapter Two")
        assertTextContains("Second book chapter two progress paragraph")
        swipeReaderDown(times = 2)
        val secondBookAnchor = centeredVisibleText("Second book chapter two progress paragraph")

        restartApp()
        navigateToBookDetails("Benchmark Sample Novel")
        clickScrolledText("Benchmark Chapter One")
        SystemClock.sleep(1_000)
        assertEquals(
            "Opening the first book again should restore its own chapter progress",
            firstBookAnchor,
            centeredVisibleText("Benchmark progress paragraph"),
        )

        restartApp()
        navigateToBookDetails("Second Benchmark Novel")
        clickScrolledText("Second Book Chapter Two")
        SystemClock.sleep(1_000)
        assertEquals(
            "Opening the second book again should restore its chapter and independent progress",
            secondBookAnchor,
            centeredVisibleText("Second book chapter two progress paragraph"),
        )
    }
}
