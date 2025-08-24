package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult

/**
 * Interface for extensions that can provide novel content
 */
interface Extension {
    val id: String
    val name: String
    val version: String
    val language: String
    val description: String

    /**
     * Search for novels
     */
    suspend fun search(query: String): List<ExtensionSearchResult>

    /**
     * Get novel details
     */
    suspend fun getBook(id: String): ExtensionBook?

    /**
     * Get chapter content
     */
    suspend fun getChapter(bookId: String, chapterId: String): ExtensionChapter?

    /**
     * Get chapter list for a book
     */
    suspend fun getChapters(bookId: String): List<ExtensionChapter>?
}
