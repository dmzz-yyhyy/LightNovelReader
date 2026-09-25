package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import io.nightfish.lightnovelreader.api.book.RelatedBookKind
import io.nightfish.lightnovelreader.api.book.RelatedBooksRequest
import org.junit.Assert.*
import org.junit.Test

class RelatedBooksDetailTest {
    @Test
    fun requestsKeepRawValuesAndOnlySupportedNonblankValuesAreClickable() {
        val author = " 川原 礫 "
        val tags = listOf("校園", "", " ", "戀愛")
        val supported = RelatedBookKind.entries.toSet()
        val detail = relatedBooksForDetail("site:one", "42", author, tags, supported)
        assertEquals("site:one", detail.sourceId)
        assertEquals(RelatedBooksRequest("42", RelatedBookKind.AUTHOR, author), detail.authorRequest)
        assertEquals(tags, detail.tags.map { it.value })
        assertEquals(RelatedBooksRequest("42", RelatedBookKind.TAG, "校園"), detail.tags.first().request)
        assertNull(detail.tags[1].request)
        assertNull(detail.tags[2].request)

        val authorOnly = relatedBooksForDetail("site:one", "42", author, tags, setOf(RelatedBookKind.AUTHOR))
        assertNotNull(authorOnly.authorRequest)
        assertTrue(authorOnly.tags.all { it.request == null })
        val tagOnly = relatedBooksForDetail("site:one", "42", author, tags, setOf(RelatedBookKind.TAG))
        assertNull(tagOnly.authorRequest)
        assertNotNull(tagOnly.tags.first().request)
        assertNull(relatedBooksForDetail("site:one", "42", "  ", tags, supported).authorRequest)

        val missingSource = relatedBooksForDetail(null, "42", author, tags, supported)
        assertNull(missingSource.authorRequest)
        assertEquals(tags, missingSource.tags.map { it.value })
        assertTrue(missingSource.tags.all { it.request == null })
    }
}
