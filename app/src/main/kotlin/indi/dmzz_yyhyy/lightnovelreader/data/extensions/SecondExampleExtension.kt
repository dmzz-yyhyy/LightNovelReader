package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult

/**
 * Second example extension for demonstration purposes
 */
class SecondExampleExtension : Extension {
    override val id: String = "second_example_extension"
    override val name: String = "Second Example Extension"
    override val version: String = "1.0.0"
    override val language: String = "en"
    override val description: String = "A second example extension for demonstration"

    private val sampleBooks = listOf(
        ExtensionSearchResult(
            id = "book3",
            title = "Fantasy Adventure",
            author = "Fantasy Writer",
            imageUrl = "",
            description = "An epic fantasy adventure with magic and dragons.",
            url = "",
            extensionId = id
        ),
        ExtensionSearchResult(
            id = "book4",
            title = "Mystery Detective",
            author = "Mystery Author",
            imageUrl = "",
            description = "A thrilling mystery novel with a brilliant detective.",
            url = "",
            extensionId = id
        ),
        ExtensionSearchResult(
            id = "book5",
            title = "Science Fiction Saga",
            author = "Sci-Fi Writer",
            imageUrl = "",
            description = "A science fiction saga set in the distant future.",
            url = "",
            extensionId = id
        )
    )

    private val sampleChapters = mapOf(
        "book3" to listOf(
            ExtensionChapter(
                id = "chapter1",
                title = "Chapter 1: The Prophecy",
                content = "In a world where magic flows like rivers, a young hero discovers their destiny.",
                order = 1
            ),
            ExtensionChapter(
                id = "chapter2",
                title = "Chapter 2: The Journey Begins",
                content = "The hero sets out on their quest, facing the first of many challenges.",
                order = 2
            )
        ),
        "book4" to listOf(
            ExtensionChapter(
                id = "chapter1",
                title = "Chapter 1: The Crime Scene",
                content = "Detective Smith arrives at the scene of a mysterious murder.",
                order = 1
            ),
            ExtensionChapter(
                id = "chapter2",
                title = "Chapter 2: The Investigation",
                content = "The detective begins to piece together the clues.",
                order = 2
            )
        ),
        "book5" to listOf(
            ExtensionChapter(
                id = "chapter1",
                title = "Chapter 1: The Future World",
                content = "In the year 2157, humanity has spread across the stars.",
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
