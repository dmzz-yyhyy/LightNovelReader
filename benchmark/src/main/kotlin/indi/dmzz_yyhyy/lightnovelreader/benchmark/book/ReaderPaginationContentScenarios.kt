package indi.dmzz_yyhyy.lightnovelreader.benchmark.book

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.ReaderRegressionTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReaderPaginationContentScenarios : ReaderRegressionTestCase() {
    @Test fun horizontalLongParagraphKeepsAllTextAndHasNoBlankTail() = verify("Horizontal")
    @Test fun verticalRtlLongParagraphKeepsAllTextAndHasNoBlankTail() = verify("Vertical, right to left")
    @Test fun verticalLtrLongParagraphKeepsAllTextAndHasNoBlankTail() = verify("Vertical, left to right")

    private fun verify(direction: String) {
        fixtureAction("LONG_PARAGRAPH", "long-paragraph=SUCCEEDED")
        openChapter(expectedContent = "跨页段落001")
        revealReaderChrome()
        tapDescription("setting")
        expandReaderSettingsSheet()
        tapScrolledText("Writing Direction")
        tapClickableText(direction)
        dismissReaderSettingsSheet()
        hideReaderChrome()
        enablePageMode(volumeKeys = true, noAnimation = true)
        visibleText("Benchmark Chapter One")
        val fragments = mutableListOf<String>()
        val pages = mutableSetOf<String>()
        while (flipChapter().endsWith("flip-chapter-benchmark-chapter-1") && pages.size < 80) {
            val page = flipPage()
            assertTrue("Repeated page while moving forward: $page", pages.add(page))
            val text = device.findObjects(By.text(Pattern.compile("(?=.*[\\p{IsHan}，。])[\\p{IsHan}0-9，。]+")))
                .filter { it.visibleBounds.width() > 0 && it.visibleBounds.height() > 0 }
                .sortedBy { if (direction == "Horizontal") it.visibleBounds.top else
                    it.visibleBounds.left * if (direction == "Vertical, right to left") -1 else 1 }
                .map { it.text }
            assertTrue("Empty page after splitting a long paragraph: ${flipState()}", text.isNotEmpty())
            fragments.addAll(text)
            pressVolume(forward = true)
            assertNotEquals("Pager did not advance: ${flipState()}", page, waitForDifferentPage(page))
        }
        assertTrue("Paragraph never reached the next chapter", flipChapter().endsWith("flip-chapter-benchmark-chapter-2"))
        assertTrue("Fixture did not exercise cross-page splitting", pages.size > 1)
        val expected = (1..100).joinToString("") {
            "跨页段落${it.toString().padStart(3, '0')}，测绘切分必须保持全部文字的顺序，不得遗漏或重复。"
        }
        assertEquals("Pagination lost, duplicated, or reordered text", expected, fragments.joinToString(""))
        visibleText("Benchmark Chapter Two")
        visibleTextContaining("Benchmark chapter two start marker")
    }
}
