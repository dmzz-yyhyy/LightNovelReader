package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionReadingService @Inject constructor(
    private val extensionManager: ExtensionManager,
    private val extensionConverter: ExtensionConverter,
    private val extensionLoader: ExtensionLoader
) {

    suspend fun searchBooks(query: String): List<BookInformationEntity> {
        val searchResults = extensionManager.searchAllExtensions(query)
        return searchResults.map { result ->
            // For now, use a default extension ID. In a real implementation,
            // you'd need to track which extension provided each result
            extensionConverter.convertSearchResultToBookInfo(result, "default_extension")
        }
    }

    suspend fun getBookFromExtension(bookId: Int): BookInformationEntity? {
        // Extract extension ID and book ID from the combined book ID
        val extensionId = "default_extension" // This would be extracted from bookId
        val originalBookId = bookId.toString() // This would be extracted from bookId
        
        val extensionBook = extensionManager.getBookFromExtension(extensionId, originalBookId)
        return extensionBook?.let { book ->
            extensionConverter.convertExtensionBookToBookInfo(book, extensionId)
        }
    }

    suspend fun getChapterFromExtension(
        bookId: Int,
        chapterId: Int
    ): ChapterContentEntity? {
        val extensionId = "default_extension" // This would be extracted from bookId
        val originalBookId = bookId.toString() // This would be extracted from bookId
        val originalChapterId = chapterId.toString() // This would be extracted from chapterId
        
        val extensionChapter = extensionManager.getChapterFromExtension(
            extensionId,
            originalBookId,
            originalChapterId
        )
        
        return extensionChapter?.let { chapter ->
            extensionConverter.convertExtensionChapterToChapterContent(chapter, bookId)
        }
    }

    suspend fun getChaptersFromExtension(bookId: Int): List<ChapterInformationEntity>? {
        val extensionId = "default_extension" // This would be extracted from bookId
        val originalBookId = bookId.toString() // This would be extracted from bookId
        
        val extensionChapters = extensionManager.getChaptersFromExtension(extensionId, originalBookId)
        
        return extensionChapters?.map { chapter ->
            extensionConverter.convertExtensionChapterToChapterInfo(chapter, bookId)
        }
    }

    suspend fun loadExtension(installedExtension: InstalledExtensionEntity): Boolean {
        return try {
            val extension = extensionLoader.loadExtension(installedExtension)
            if (extension != null) {
                extensionManager.registerExtension(extension)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unloadExtension(extensionId: String) {
        extensionManager.unregisterExtension(extensionId)
    }

    fun getEnabledExtensions(): List<Extension> {
        return extensionManager.getEnabledExtensions()
    }
}
