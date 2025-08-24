package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionManager @Inject constructor() {

    private val extensions = mutableMapOf<String, Extension>()

    fun registerExtension(extension: Extension) {
        extensions[extension.id] = extension
    }

    fun unregisterExtension(extensionId: String) {
        extensions.remove(extensionId)
    }

    fun getExtension(extensionId: String): Extension? {
        return extensions[extensionId]
    }

    fun getAllExtensions(): List<Extension> {
        return extensions.values.toList()
    }

    fun getEnabledExtensions(): List<Extension> {
        return extensions.values.toList()
    }

    suspend fun searchAllExtensions(query: String): List<ExtensionSearchResult> {
        val results = mutableListOf<ExtensionSearchResult>()

        getEnabledExtensions().forEach { extension ->
            try {
                val searchResults = extension.search(query)
                // Extension should already set extensionId in results
                results.addAll(searchResults)
            } catch (e: Exception) {
                // Log error but continue with other extensions
                e.printStackTrace()
            }
        }

        return results
    }

    suspend fun getBookFromExtension(extensionId: String, bookId: String): ExtensionBook? {
        val extension = getExtension(extensionId) ?: return null

        return try {
            extension.getBook(bookId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getChapterFromExtension(
        extensionId: String,
        bookId: String,
        chapterId: String
    ): ExtensionChapter? {
        val extension = getExtension(extensionId) ?: return null

        return try {
            extension.getChapter(bookId, chapterId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getChaptersFromExtension(
        extensionId: String,
        bookId: String
    ): List<ExtensionChapter>? {
        val extension = getExtension(extensionId) ?: return null

        return try {
            extension.getChapters(bookId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
