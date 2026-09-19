package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Test

class SearchSessionTest {
    @Test
    fun restoredSearchUsesLastSubmissionInsteadOfRouteAuthor() {
        val saved = SavedStateHandle()
        val session = SearchSession(saved)
        val initial = session.initialize("Author A", "writer", "title")!!
        assertTrue(initial.keepSingleResult)
        session.submit(initial)

        session.selectedType = "title"
        session.keyword = "Book B"
        val manual = SearchRequest("title", session.keyword)
        assertFalse(manual.keepSingleResult)
        session.submit(manual)
        session.keyword = "Unsubmitted C"

        // A new handle and session represent a new ViewModel after process restoration.
        val restored = SearchSession(SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) }))
        assertEquals(manual, restored.initialize("Author A", "writer", "title"))
        assertEquals("title", restored.selectedType)
        assertEquals("Unsubmitted C", restored.keyword)
        assertEquals("Book B", restored.submitted?.keyword)

        // An initialized author entry must preserve its single-result display policy.
        val authorSaved = SavedStateHandle()
        val authorSession = SearchSession(authorSaved)
        authorSession.submit(authorSession.initialize("Author A", "writer", "title")!!)
        val authorRestored = SearchSession(SavedStateHandle(authorSaved.keys().associateWith { authorSaved.get<Any?>(it) }))
        assertEquals(initial, authorRestored.initialize("Author A", "writer", "title"))
    }

    @Test
    fun unsupportedAndBlankAuthorsDoNotStartSearches() {
        assertNull(SearchSession(SavedStateHandle()).initialize("Author", null, "title"))
        assertNull(SearchSession(SavedStateHandle()).initialize("  ", "writer", "title"))
        val session = SearchSession(SavedStateHandle())
        assertNull(session.initialize(null, "writer", "title"))
        session.keyword = "Draft"
        assertNull(session.initialize(null, "writer", "title"))
        assertEquals("Draft", session.keyword)
        assertEquals("title", session.selectedType)
    }
}
