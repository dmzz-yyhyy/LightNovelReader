package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult

/**
 * Example extension for demonstration purposes
 */
class ExampleExtension : Extension {
    override val id: String = "example_extension"
    override val name: String = "Example Extension"
    override val version: String = "1.0.0"
    override val language: String = "en"
    override val description: String = "An example extension for demonstration"

    private val sampleBooks = listOf(
        ExtensionSearchResult(
            id = "book1",
            title = "Sample Novel 1",
            author = "Sample Author",
            imageUrl = "",
            description = "This is a sample novel for demonstration purposes.",
            url = "",
            extensionId = id
        ),
        ExtensionSearchResult(
            id = "book2",
            title = "Sample Novel 2",
            author = "Another Author",
            imageUrl = "",
            description = "Another sample novel for demonstration.",
            url = "",
            extensionId = id
        )
    )

    private val sampleChapters = mapOf(
        "book1" to listOf(
            ExtensionChapter(
                id = "chapter1",
                title = "Chapter 1: The Beginning",
                content = "This is the content of chapter 1. It contains sample text for demonstration purposes.",
                order = 1
            ),
            ExtensionChapter(
                id = "chapter2",
                title = "Chapter 2: The Journey",
                content = "This is the content of chapter 2. The story continues here.",
                order = 2
            )
        ),
        "book2" to listOf(
            ExtensionChapter(
                id = "chapter1",
                title = "Chapter 1: Introduction",
                content = "This is the first chapter of the second book.",
                order = 1
            )
        )
    )

    override suspend fun search(query: String): List<ExtensionSearchResult> {
        return sampleBooks.filter { book ->
            book.title.contains(query, ignoreCase = true) ||
            book.author.contains(query, ignoreCase = true) ||
            book.description.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getBook(id: String): ExtensionBook? {
        val searchResult = sampleBooks.find { it.id == id } ?: return null
        
        return ExtensionBook(
            id = searchResult.id,
            title = searchResult.title,
            author = searchResult.author,
            imageUrl = searchResult.imageUrl,
            description = searchResult.description,
            url = searchResult.url,
            genres = emptyList(), // No genres in search result
            status = "Unknown",
            lastUpdated = System.currentTimeMillis()
        )
    }

    override suspend fun getChapter(bookId: String, chapterId: String): ExtensionChapter? {
        return sampleChapters[bookId]?.find { it.id == chapterId }
    }

    override suspend fun getChapters(bookId: String): List<ExtensionChapter>? {
        return sampleChapters[bookId]
    }
}
