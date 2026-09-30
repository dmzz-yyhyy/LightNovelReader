package indi.dmzz_yyhyy.lightnovelreader.benchmark.bookshelf

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BookshelfBehaviorScenarios : BenchmarkTestCase() {
    @Test
    fun sortingAndReverseChangeBookOrderAndSurviveRestart() {
        openBookshelf()
        tapDescription("sort")
        tapText("Word Count")
        awaitShelfContains("sort=word_count")
        pressBack()
        assertBookAbove(PRIMARY_BOOK, SECONDARY_BOOK)

        tapDescription("sort")
        tapText("Reverse")
        awaitShelfContains("reversed=true")
        pressBack()

        restartApp()
        openRoot("Bookshelf")
        awaitShelfContains("sort=word_count")
        awaitShelfContains("reversed=true")
        assertBookAbove(SECONDARY_BOOK, PRIMARY_BOOK)
    }

    @Test
    fun editedShelfFlagsAndNameRemainEffectiveAfterRestart() {
        openBookshelf()
        openBookshelfOverflow()
        tapText("Bookshelf Settings")
        replaceText(0, "Verified Shelf")
        tapSwitchForText("Auto Cache")
        tapSwitchForText("Update Notification")
        tapDescription("save")

        awaitShelfContains("name=Verified Shelf")
        awaitShelfContains("auto-cache=true")
        awaitShelfContains("reminder=true")

        restartApp()
        openRoot("Bookshelf")
        visibleText("Verified Shelf")
        openBookshelfOverflow()
        tapText("Bookshelf Settings")
        assertSwitchForText("Auto Cache", checked = true)
        assertSwitchForText("Update Notification", checked = true)
    }

    @Test
    fun pinAndMoveMutateMembershipAndRemainVisibleAfterRestart() {
        openBookshelf()
        longPressText(PRIMARY_BOOK)
        tapDescription("pin")
        awaitShelfContains("pinned=$PRIMARY_BOOK_ID")

        tapDescription("create")
        replaceText(0, TARGET_SHELF)
        tapDescription("save")
        visibleText(TARGET_SHELF)

        tapText("Benchmark Shelf")
        longPressText(PRIMARY_BOOK)
        tapDescription("bookmark")
        tapText(TARGET_SHELF)
        awaitSwitchForText(TARGET_SHELF, checked = true)
        tapLastText("Favorite")
        waitUntil("Book metadata did not include two shelves") {
            val memberships = bookMetadataReport()
                .substringAfter("shelves=", "")
                .substringBeforeAny(';', '"', '\r', '\n')
                .split(',')
                .filter(String::isNotBlank)
            memberships.size == 2
        }

        restartApp()
        openRoot("Bookshelf")
        tapText(TARGET_SHELF)
        visibleText(PRIMARY_BOOK)
        tapText("Benchmark Shelf")
        visibleTextContaining("Pinned")
        visibleText(PRIMARY_BOOK)
    }

    private fun assertBookAbove(upperTitle: String, lowerTitle: String) {
        var upper = emptyList<android.graphics.Rect>()
        var lower = emptyList<android.graphics.Rect>()
        repeat(40) {
            upper = device.findObjects(androidx.test.uiautomator.By.text(upperTitle))
                .map { it.visibleBounds }
                .sortedWith(compareBy<android.graphics.Rect> { it.top }.thenBy { it.left })
            lower = device.findObjects(androidx.test.uiautomator.By.text(lowerTitle))
                .map { it.visibleBounds }
                .sortedWith(compareBy<android.graphics.Rect> { it.top }.thenBy { it.left })
            val firstUpper = upper.firstOrNull()
            val firstLower = lower.firstOrNull()
            if (firstUpper != null && firstLower != null) {
                val sameRow = kotlin.math.abs(firstUpper.top - firstLower.top) < 24
                if (if (sameRow) firstUpper.left < firstLower.left else firstUpper.top < firstLower.top) {
                    return
                }
            }
            SystemClock.sleep(250)
        }
        assertTrue(
            "'$upperTitle' was not rendered before '$lowerTitle'; upper=$upper lower=$lower",
            false,
        )
    }

    private fun awaitShelfContains(expected: String): String = awaitFixtureReport(
        action = "REPORT_BOOKSHELF",
        arguments = "--ei bookshelf-id $BENCHMARK_SHELF_ID",
        expectedFragment = expected,
    )

    private fun bookMetadataReport(): String = fixtureReport(
        action = "REPORT_BOOK_METADATA",
        arguments = "--es book-id $PRIMARY_BOOK_ID",
    )

    private fun String.substringBeforeAny(vararg delimiters: Char): String {
        val index = indexOfFirst { it in delimiters }
        return if (index < 0) this else substring(0, index)
    }

    companion object {
        private const val BENCHMARK_SHELF_ID = 1_000_001
        private const val PRIMARY_BOOK_ID = "9999999"
        private const val TARGET_SHELF = "Automation Target"
    }
}
