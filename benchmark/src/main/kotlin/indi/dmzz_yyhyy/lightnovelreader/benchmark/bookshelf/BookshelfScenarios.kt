package indi.dmzz_yyhyy.lightnovelreader.benchmark.bookshelf

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BookshelfScenarios : BenchmarkTestCase() {
    @Test
    fun seededShelfOpensBookDetails() {
        openBookshelf()
        tapText(PRIMARY_BOOK)
        visibleText("Benchmark Author")
        visibleText("Benchmark Volume")
        visibleDescription("export")
        visibleDescription("formatting")
        visibleDescription("more")
    }

    @Test
    fun createShelfPersistsAcrossRestart() {
        openBookshelf()
        tapDescription("create")
        visibleText("New Bookshelf")
        replaceText(0, "Automation Shelf")
        tapDescription("save")
        visibleText("Automation Shelf")

        restartApp()
        openRoot("Bookshelf")
        visibleText("Automation Shelf")
    }

    @Test
    fun sortAndOverflowExposeEveryAction() {
        openBookshelf()
        tapDescription("sort")
        listOf("Sort Type", "Default", "Recently Updated", "Name", "Word Count", "Reverse")
            .forEach(::visibleText)
        pressBack()

        openBookshelfOverflow()
        listOf("Adjust Order", "Bookshelf Settings", "Share Bookshelf", "Adjust Bookshelf Order")
            .forEach(::visibleText)
    }

    @Test
    fun bothReorderScreensOpenAndReturn() {
        openBookshelf()
        openBookshelfOverflow()
        tapText("Adjust Order")
        visibleText("Adjust Order")
        visibleText(PRIMARY_BOOK)
        visibleDescription("back")
        pressBack()

        openBookshelfOverflow()
        tapText("Adjust Bookshelf Order")
        visibleText("Adjust Bookshelf Order")
        visibleText("Benchmark Shelf")
        visibleDescription("back")
    }

    @Test
    fun editShelfSettingsPersistAcrossRestart() {
        openBookshelf()
        openBookshelfOverflow()
        tapText("Bookshelf Settings")
        visibleText("Edit Bookshelf")
        replaceText(0, "Renamed Benchmark Shelf")
        tapText("Auto Cache")
        tapText("Update Notification")
        tapDescription("save")
        visibleText("Renamed Benchmark Shelf")

        restartApp()
        openRoot("Bookshelf")
        visibleText("Renamed Benchmark Shelf")
    }

    @Test
    fun blankShelfNameShowsValidation() {
        openBookshelf()
        openBookshelfOverflow()
        tapText("Bookshelf Settings")
        tapDescription("cancel")
        tapDescription("save")
        visibleText("Enter the bookshelf name.")
        visibleText("Edit Bookshelf")
    }

    @Test
    fun selectionModeSupportsPinMoveAndCancel() {
        openBookshelf()
        longPressText(PRIMARY_BOOK)
        listOf("select all", "pin", "remove", "bookmark").forEach(::visibleDescription)
        tapDescription("cancel")
        awaitSelectionModeClosed()

        longPressText(PRIMARY_BOOK)
        tapDescription("pin")
        awaitFixtureReport(
            action = "REPORT_BOOKSHELF",
            arguments = "--ei bookshelf-id $BENCHMARK_SHELF_ID",
            expectedFragment = "pinned=$PRIMARY_BOOK_ID",
        )
        awaitSelectionModeClosed()

        longPressText(PRIMARY_BOOK)
        tapDescription("bookmark")
        visibleText("Add this book to the following bookshelves")
        visibleText("Benchmark Shelf")
        tapText("Cancel")
        assertTextNotVisible("Add this book to the following bookshelves")
        awaitFixtureReport(
            action = "REPORT_BOOK_METADATA",
            arguments = "--es book-id $PRIMARY_BOOK_ID",
            expectedFragment = "shelves=$BENCHMARK_SHELF_ID",
        )
        visibleText(PRIMARY_BOOK)
    }

    @Test
    fun selectedBookCanBeRemoved() {
        openBookshelf()
        longPressText(PRIMARY_BOOK)
        tapDescription("remove")
        assertTextNotVisible(PRIMARY_BOOK)
        visibleText(SECONDARY_BOOK)
    }

    @Test
    fun deleteShelfSupportsCancelAndConfirmation() {
        openBookshelf()
        openBookshelfOverflow()
        tapText("Bookshelf Settings")
        tapText("Delete Bookshelf")
        visibleTextContaining("lost forever")
        tapText("Cancel")
        visibleText("Edit Bookshelf")

        tapText("Delete Bookshelf")
        tapText("OK")
        visibleText("Bookshelf")
        assertTextNotVisible("Benchmark Shelf")
    }

    private fun awaitSelectionModeClosed() {
        waitUntil("Selection mode did not close") {
            !device.hasObject(By.desc("pin"))
        }
        visibleText(PRIMARY_BOOK)
    }

    private companion object {
        const val BENCHMARK_SHELF_ID = 1_000_001
        const val PRIMARY_BOOK_ID = "9999999"
    }
}
