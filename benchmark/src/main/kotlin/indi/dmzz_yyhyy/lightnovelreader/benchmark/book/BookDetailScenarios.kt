package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import android.view.KeyEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderTestCase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BookDetailScenarios : ReaderTestCase() {
    @Test
    fun detailToolbarOpensEveryActionSurface() {
        openBook()
        tapDescription("export")
        visibleText("Export as Epub")
        pressBack()
        tapDescription("formatting")
        visibleText("Book Rules")
        pressBack()
        tapDescription("more")
        visibleText("Mark as read…")
    }

    @Test
    fun chapterDirectorySelectsAcrossVolumesAndReaderChromeWorks() {
        openChapter()
        revealReaderChrome()
        listOf("menu", "setting", "mark").forEach(::visibleDescription)
        tapDescription("menu")
        visibleText("Select Chapter")
        expandBottomSheet("Select Chapter")
        visibleText("Benchmark Chapter One")
        tap(scrollToText("Benchmark Bonus Volume"))
        tap(scrollToText("Benchmark Chapter Two"))
        assertForegroundPackage(TARGET_PACKAGE)
    }

    @Test
    fun readerSettingsExposeEveryGroup() {
        openChapter()
        revealReaderChrome()
        tapDescription("setting")
        listOf(
            "Reader Settings",
            "Appearance",
            "Controls",
            "Margins",
            "Keep Screen On",
            "Hide Status Bar",
            "Reader Style",
        ).forEach(::visibleText)
        tapText("Controls")
        visibleText("Page Turn Mode")
        visibleText("Back Prevention")
    }

    @Test
    fun scrollingAndVolumeInputKeepReaderResponsive() {
        openChapter()
        scrollReader(1)
        device.pressKeyCode(KeyEvent.KEYCODE_VOLUME_DOWN)
        device.pressKeyCode(KeyEvent.KEYCODE_VOLUME_UP)
        visibleTextContaining("Benchmark progress paragraph")
    }

    @Test
    fun veryLongSelectableChapterRemainsResponsive() {
        fixtureAction("EXTEND_RAPID_CHAPTER_CHAIN", "rapid-chapters=SUCCEEDED")
        openChapter(
            chapter = "Benchmark Chapter 6",
            expectedContent = "Benchmark rapid chapter 6 paragraph",
        )
        repeat(24) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.78f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.22f).toInt(),
                4,
            )
        }
        device.waitForIdle()
        assertForegroundPackage(TARGET_PACKAGE)
        repeat(3) {
            if (device.hasObject(By.desc("setting"))) return@repeat
            device.click(device.displayWidth / 2, device.displayHeight / 2)
            device.wait(Until.hasObject(By.desc("setting")), 1_000L)
        }
        tapDescription("setting")
        visibleText("Reader Settings")
    }

    @Test
    fun informationSheetShowsIdentityAndStatistics() {
        openBook()
        tapScrolledText("Info")
        listOf("Title", PRIMARY_BOOK, "ID", "9999999", "Author", "Benchmark Author")
            .forEach(::visibleText)
        visibleText("Stats")
        visibleTextContaining("12")
        visibleTextContaining("2 chapters")
    }

    @Test
    fun generatedCoverOpensImageViewerAndCloses() {
        openBook()
        val coverTitle = device.findObjects(By.text(PRIMARY_BOOK))
            .minByOrNull { it.visibleBounds.left }
        assertTrue("Generated cover title was not visible", coverTitle != null)
        tap(coverTitle!!)
        visibleDescription("close")
        visibleDescription("save")
        tapDescription("close")
        visibleText(PRIMARY_BOOK)
    }

    @Test
    fun collectionsActionOpensSingleBookShelfDialog() {
        openBook()
        tapScrolledText("Collected")
        visibleText("Add this book to the following bookshelves")
        visibleText("Benchmark Shelf")
        tapText("Cancel")
        visibleText(PRIMARY_BOOK)
    }

    @Test
    fun epubOptionsCoverWholeBookVolumeAndSelectionModes() {
        openBook()
        tapDescription("export")
        visibleText("Include images")
        visibleText("Export by volumes")
        tapText("Export by volumes")
        visibleText("Benchmark Volume")
        visibleText("Benchmark Bonus Volume")
        tapText("Benchmark Volume")
        visibleText("Select All")
        tapText("Select All")
        assertTextNotVisible("Select All")
    }

    @Test
    fun markReadRangePersistsAsFinished() {
        openBook()
        tapDescription("more")
        tapText("Mark as read…")
        visibleText("全部章节")
        tapText("选择范围")
        visibleText("Benchmark Chapter One")
        visibleText("Benchmark Chapter Two")
        tapScrolledText("Benchmark Chapter One")
        tapText("Benchmark Chapter Two")
        visibleText("标记已选 2 章为已读")
        tapText("标记已选 2 章为已读")

        restartApp()
        openRoot("Bookshelf")
        tapText(PRIMARY_BOOK)
        scrollToText("Finished Reading")
    }

    @Test
    fun markReadCanBeCancelledWithoutMutation() {
        openBook()
        tapDescription("more")
        tapText("Mark as read…")
        tapText("取消")
        visibleText(PRIMARY_BOOK)
        tapDescription("more")
        tapText("Mark as read…")
        visibleText("标记全部为已读")
    }

    @Test
    fun pageModeAndManualMarginsRevealConditionalControls() {
        openChapter()
        revealReaderChrome()
        tapDescription("setting")
        expandReaderSettingsSheet()
        tapText("Controls")
        visibleText("Switch between scrolling mode and page turn mode")
        tapText("Page Turn Mode")
        visibleText("Volume Key Navigation")
        scrollToText("Tap to Turn Pages")
        visibleText("Page Turn Animation")

        tapText("Margins")
        visibleText("Auto Margin Adjustment")
        tapText("Auto Margin Adjustment")
        visibleText("Top Margin")
        visibleText("Bottom Margin")
        scrollToText("Right Margin")
        visibleText("Left Margin")
    }
}
