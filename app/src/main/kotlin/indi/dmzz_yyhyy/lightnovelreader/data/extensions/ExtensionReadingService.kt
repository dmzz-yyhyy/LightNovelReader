package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.ExtensionBookDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionBookEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionReadingService @Inject constructor(
    private val extensionManager: ExtensionManager,
    private val extensionConverter: ExtensionConverter,
    private val extensionLoader: ExtensionLoader,
    private val extensionBookIdManager: ExtensionBookIdManager,
    private val extensionBookDao: ExtensionBookDao,
    private val repositoryServiceProvider: javax.inject.Provider<indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService>
) {

    suspend fun searchBooks(query: String): List<BookInformationEntity> {
        val searchResults = extensionManager.searchAllExtensions(query)
        val books = mutableListOf<BookInformationEntity>()

        for (result in searchResults) {
            try {
                // Generate consistent book ID
                val bookId =
                    extensionBookIdManager.generateExtensionBookId(result.extensionId, result.id)

                // Create or update extension book mapping
                val extensionBook = ExtensionBookEntity(
                    internalBookId = bookId,
                    extensionId = result.extensionId.toIntOrNull() ?: 0,
                    originalBookId = result.id,
                    title = result.title,
                    author = result.author,
                    description = result.description,
                    imageUrl = result.imageUrl,
                    url = result.url
                )

                // Save to database for future reference
                extensionBookDao.insertExtensionBook(extensionBook)

                // Convert to BookInformationEntity
                val bookInfo =
                    extensionConverter.convertSearchResultToBookInfo(result, result.extensionId)
                        .copy(id = bookId)
                books.add(bookInfo)
            } catch (e: Exception) {
                e.printStackTrace()
                // Continue with other results
            }
        }

        return books
    }

    suspend fun getBookFromExtension(bookId: Int): BookInformationEntity? {
        // Try to find the book in our extension book mapping
        val extensionBook = extensionBookDao.getExtensionBookById(bookId)
        if (extensionBook != null) {
            val extension = extensionManager.getExtension(extensionBook.extensionId.toString())
            if (extension != null) {
                try {
                    val book = extension.getBook(extensionBook.originalBookId)
                    return book?.let {
                        extensionConverter.convertExtensionBookToBookInfo(
                            it,
                            extensionBook.extensionId.toString()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    suspend fun getChapterFromExtension(
        bookId: Int,
        chapterId: Int
    ): ChapterContentEntity? {
        val extensionBook = extensionBookDao.getExtensionBookById(bookId)
        if (extensionBook != null) {
            val extension = extensionManager.getExtension(extensionBook.extensionId.toString())
            if (extension != null) {
                try {
                    val chapter =
                        extension.getChapter(extensionBook.originalBookId, chapterId.toString())
                    return chapter?.let {
                        extensionConverter.convertExtensionChapterToChapterContent(it, bookId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    suspend fun getChaptersFromExtension(bookId: Int): List<ChapterInformationEntity>? {
        val extensionBook = extensionBookDao.getExtensionBookById(bookId)
        if (extensionBook != null) {
            val extension = extensionManager.getExtension(extensionBook.extensionId.toString())
            if (extension != null) {
                try {
                    val chapters = extension.getChapters(extensionBook.originalBookId)
                    return chapters?.map { chapter ->
                        extensionConverter.convertExtensionChapterToChapterInfo(chapter, bookId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    suspend fun addExtensionBookToLibrary(bookId: Int): Boolean {
        return try {
            extensionBookDao.updateLibraryStatus(bookId, true, System.currentTimeMillis())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeExtensionBookFromLibrary(bookId: Int): Boolean {
        return try {
            extensionBookDao.updateLibraryStatus(bookId, false, null)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
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

    suspend fun getEnabledExtensions(): List<Extension> {
        println("ExtensionReadingService.getEnabledExtensions: Starting...")
        
        val installedExtensions =
            repositoryServiceProvider.get().getAllInstalledExtensions().first()
        println("ExtensionReadingService.getEnabledExtensions: Found ${installedExtensions.size} installed extensions")
        
        val enabledExtensions = mutableListOf<Extension>()

        for (installedExtension in installedExtensions) {
            println("ExtensionReadingService.getEnabledExtensions: Processing ${installedExtension.name} (Enabled: ${installedExtension.isEnabled})")
            
            if (installedExtension.isEnabled) {
                val extension =
                    extensionManager.getExtension(installedExtension.id.toString())
                if (extension != null) {
                    enabledExtensions.add(extension)
                    println("ExtensionReadingService.getEnabledExtensions: Found extension in manager: ${extension.name}")
                } else {
                    println("ExtensionReadingService.getEnabledExtensions: Extension not in manager, trying to load: ${installedExtension.name}")
                    // Try to load the extension if it's not in memory
                    val loadedExtension = extensionLoader.loadExtension(installedExtension)
                    if (loadedExtension != null) {
                        extensionManager.registerExtension(loadedExtension)
                        enabledExtensions.add(loadedExtension)
                        println("ExtensionReadingService.getEnabledExtensions: Successfully loaded and registered: ${loadedExtension.name}")
                    } else {
                        println("ExtensionReadingService.getEnabledExtensions: Failed to load extension: ${installedExtension.name}")
                    }
                }
            }
        }

        println("ExtensionReadingService.getEnabledExtensions: Returning ${enabledExtensions.size} enabled extensions")
        return enabledExtensions
    }
}
