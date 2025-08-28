package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.MutableBookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationBooksRow
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationDisplayBook
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Adapter that wraps an Extension to implement the WebBookDataSource interface
 */
class ExtensionWebDataSourceAdapter(
    private val extension: Extension,
    private val extensionConverter: ExtensionConverter,
    private val extensionBookIdManager: ExtensionBookIdManager? = null
) : WebBookDataSource {

    override val id: Int = extension.id.hashCode()

    // Expose extension details for UI
    val extensionName: String = extension.name
    val extensionId: String = extension.id
    
    // Map to store the relationship between generated hashed IDs and original book IDs
    private val bookIdMapping: MutableMap<Int, String> = mutableMapOf()
    
    // Helper function to generate and store book ID mapping
    suspend fun generateAndStoreBookId(
        originalBookId: String,
        bookTitle: String = "",
        bookAuthor: String = "",
        bookDescription: String = "",
        bookImageUrl: String = ""
    ): Int {
        val hashedId = extensionConverter.generateBookId(extension.id, originalBookId)
        bookIdMapping[hashedId] = originalBookId
        
        // Also persist to database if manager is available
        extensionBookIdManager?.let { manager ->
            try {
                manager.generateAndStoreExtensionBookId(
                    extensionId = extension.id,
                    originalBookId = originalBookId,
                    bookTitle = bookTitle,
                    bookAuthor = bookAuthor,
                    bookDescription = bookDescription,
                    bookImageUrl = bookImageUrl
                )
                println("ExtensionWebDataSourceAdapter: Persisted book mapping for $hashedId")
            } catch (e: Exception) {
                println("ExtensionWebDataSourceAdapter: Failed to persist book mapping: ${e.message}")
            }
        }
        
        return hashedId
    }
    
    // Helper function to get original book ID from hashed ID
    private suspend fun getOriginalBookId(hashedId: Int): String? {
        // First check memory cache
        bookIdMapping[hashedId]?.let { return it }
        
        // Then check database via ExtensionBookIdManager
        return extensionBookIdManager?.extractExtensionInfo(hashedId)?.originalBookId
    }

    override suspend fun isOffLine(): Boolean = false

    override val offLine: Boolean = false

    override val isOffLineFlow: Flow<Boolean> = MutableStateFlow(false)

    override val explorationPageIdList: List<String> = listOf("search-a")

    override val explorationPageDataSourceMap: Map<String, ExplorationPageDataSource> = mapOf(
        "search-a" to ExtensionExplorationPageDataSource(extension, extensionConverter, this)
    )

    override val explorationExpandedPageDataSourceMap: Map<String, ExplorationExpandedPageDataSource> =
        emptyMap()

    override val searchTypeMap: Map<String, String> = mapOf("search" to "Search")

    override val searchTipMap: Map<String, String> = mapOf("search" to "Search for novels...")

    override val searchTypeIdList: List<String> = listOf("search")

    override suspend fun getBookInformation(id: Int): BookInformation {
        return try {
            // Get the original book ID from our mapping
            val originalBookId = getOriginalBookId(id)
            println("ExtensionWebDataSourceAdapter: Looking up book ID $id -> originalBookId: $originalBookId")
            if (originalBookId != null) {
                println("ExtensionWebDataSourceAdapter: Calling extension.getBook($originalBookId)")
                val book = extension.getBook(originalBookId)
                if (book != null) {
                    println("ExtensionWebDataSourceAdapter: Successfully got book: ${book.title}")
                    extensionConverter.convertExtensionBookToBookInfo(book, extension.id)
                } else {
                    println("ExtensionWebDataSourceAdapter: Extension.getBook returned null for $originalBookId")
                    BookInformation.empty(id)
                }
            } else {
                println("ExtensionWebDataSourceAdapter: No original book ID found for hashed ID $id")
                BookInformation.empty(id)
            }
        } catch (e: Exception) {
            println("ExtensionWebDataSourceAdapter: Error getting book information: ${e.message}")
            e.printStackTrace()
            BookInformation.empty(id)
        }
    }

    override suspend fun getBookVolumes(id: Int): BookVolumes {
        return try {
            // Get the original book ID from our mapping
            val originalBookId = getOriginalBookId(id)
            if (originalBookId != null) {
                val chapters = extension.getChapters(originalBookId)
                if (chapters != null) {
                    // Convert chapters to volumes
                    val volume = Volume(
                        volumeId = 1,
                        volumeTitle = "Volume 1",
                        chapters = chapters.map { chapter ->
                            ChapterInformation(
                                id = chapter.id.toIntOrNull() ?: 0,
                                title = chapter.title
                            )
                        }
                    )
                    BookVolumes(
                        bookId = id,
                        volumes = listOf(volume)
                    )
                } else {
                    BookVolumes(id, emptyList())
                }
            } else {
                BookVolumes(id, emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            BookVolumes(id, emptyList())
        }
    }

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent {
        return try {
            // Get the original book ID from our mapping
            val originalBookId = getOriginalBookId(bookId)
            if (originalBookId != null) {
                val chapter = extension.getChapter(originalBookId, chapterId.toString())
                if (chapter != null) {
                    extensionConverter.convertExtensionChapterToChapterContent(chapter, chapterId)
                } else {
                    ChapterContent.empty()
                }
            } else {
                ChapterContent.empty()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ChapterContent.empty()
        }
    }

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        return kotlinx.coroutines.flow.flow {
            try {
                val searchResults = extension.search(keyword)
                val bookInfoList = searchResults.map { result ->
                    // Store the mapping between hashed ID and original book ID with book details
                    val hashedId = generateAndStoreBookId(
                        originalBookId = result.id,
                        bookTitle = result.title,
                        bookAuthor = result.author,
                        bookDescription = result.description,
                        bookImageUrl = result.imageUrl
                    )
                    extensionConverter.convertSearchResultToBookInfo(result, extension.id).apply {
                        // Make sure the ID matches what we generated
                        (this as? MutableBookInformation)?.id = hashedId
                    }
                }
                emit(bookInfoList)
                // Emit empty list to signal end of search
                emit(listOf(BookInformation.empty()))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(emptyList())
            }
        }
    }

    override fun stopAllSearch() {
        // Extensions don't have ongoing search operations to stop
    }
}

/**
 * ExplorationPageDataSource implementation for extensions
 */
class ExtensionExplorationPageDataSource(
    private val extension: Extension,
    private val extensionConverter: ExtensionConverter,
    private val parentAdapter: ExtensionWebDataSourceAdapter
) : ExplorationPageDataSource {

    private var lock = false
    private val explorationBooksRows: MutableStateFlow<List<ExplorationBooksRow>> =
        MutableStateFlow(emptyList())

    override val title = "${extension.name} Books"

    override fun getExplorationPage(): ExplorationPage {
        if (!lock) {
            lock = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Instead of getLatest() which might fail, search for books starting with 'a'
                    // This provides a better fallback and shows actual searchable content
                    val searchResults = extension.search("a")
                    val explorationBooks = searchResults.map { result ->
                        ExplorationDisplayBook(
                            id = parentAdapter.generateAndStoreBookId(
                                originalBookId = result.id,
                                bookTitle = result.title,
                                bookAuthor = result.author, 
                                bookDescription = result.description,
                                bookImageUrl = result.imageUrl
                            ),
                            title = result.title,
                            author = result.author,
                            coverUrl = result.imageUrl
                        )
                    }

                    val booksRow = ExplorationBooksRow(
                        title = "${extension.name} Books (Search: 'a')",
                        bookList = explorationBooks
                    )

                    explorationBooksRows.value = listOf(booksRow)
                } catch (e: Exception) {
                    println("ExtensionExplorationPageDataSource: Error loading books for ${extension.name}: ${e.message}")
                    e.printStackTrace()
                    // Create an empty row if search fails
                    val emptyRow = ExplorationBooksRow(
                        title = "${extension.name} (Error loading books)",
                        bookList = emptyList()
                    )
                    explorationBooksRows.value = listOf(emptyRow)
                }
            }
        }

        return ExplorationPage(title, explorationBooksRows)
    }
}