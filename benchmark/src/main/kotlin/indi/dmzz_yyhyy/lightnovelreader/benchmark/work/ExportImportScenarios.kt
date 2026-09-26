package indi.dmzz_yyhyy.lightnovelreader.benchmark.work

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.DocumentsTestCase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ExportImportScenarios : DocumentsTestCase() {
    @Test
    fun userSnapshotCompletesExportDeleteImportRoundTrip() {
        deleteDocument(SNAPSHOT)
        openSettings()
        tapScrolledText("Snapshot User Data")
        tapScrolledText("Export to File")
        saveDocument(SNAPSHOT)
        assertDocumentHasData(SNAPSHOT)

        restartApp()
        openRoot("Bookshelf")
        openBookshelfOverflow()
        tapText("Bookshelf Settings")
        tapText("Delete Bookshelf")
        tapText("OK")
        assertTextNotVisible("Benchmark Shelf")

        openRoot("Settings")
        tapScrolledText("Import User Data")
        selectDocument(SNAPSHOT)
        visibleText("Merge")
        tapText("Merge")
        assertTrue(
            "Import worker did not complete",
            device.wait(Until.gone(By.text("Merge")), WORK_TIMEOUT),
        )
        restartApp()
        openRoot("Bookshelf")
        visibleText("Benchmark Shelf")
        visibleText(PRIMARY_BOOK)
    }

    @Test
    fun bookshelfExportWritesNonEmptyLnrFile() {
        deleteDocument(BOOKSHELF)
        openBookshelf()
        openBookshelfOverflow()
        tapText("Import & Export…")
        tapText("Export to .lnr File")
        saveDocument(BOOKSHELF)
        assertDocumentHasData(BOOKSHELF)
        visibleText("Benchmark Shelf")
    }

    @Test
    fun epubExportWritesNonEmptyBook() {
        deleteDocument(EPUB)
        openBook()
        tapDescription("export")
        visibleText("Export as Epub")
        tapText("Export")
        saveDocument(EPUB)
        assertDocumentHasData(EPUB)
        visibleText(PRIMARY_BOOK)
    }

    companion object {
        private const val SNAPSHOT = "BenchmarkUiSnapshot.lnr"
        private const val BOOKSHELF = "BenchmarkUiBookshelf.lnr"
        private const val EPUB = "BenchmarkUiNovel.epub"
        private const val WORK_TIMEOUT = 45_000L
    }
}
