package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import app.shosetsu.lib.IExtension
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.Extension
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import app.shosetsu.lib.lua.LuaExtension as ShosetsuLuaExtension

/**
 * Wrapper around Shosetsu's LuaExtension to implement our Extension interface
 */
class ShosetsuLuaExtensionWrapper(
    private val luaScript: String,
    private val fileName: String
) : Extension {

    private val shosetsuExtension: IExtension = ShosetsuLuaExtension(luaScript, fileName)

    override val id: String = shosetsuExtension.formatterID.toString()
    override val name: String = shosetsuExtension.name
    override val version: String = "1.0.0" // Default version since Shosetsu doesn't expose this
    override val language: String = "en" // Default language since Shosetsu doesn't expose this
    override val description: String = shosetsuExtension.baseURL

    override suspend fun search(query: String): List<ExtensionSearchResult> =
        withContext(Dispatchers.IO) {
            try {
                // Don't search with empty queries - return empty list
                if (query.isBlank() || query.isEmpty()) {
                    println("ShosetsuLuaExtensionWrapper: Empty query provided, returning empty list")
                    return@withContext emptyList<ExtensionSearchResult>()
                }

                // Validate extension before search
                if (shosetsuExtension.baseURL.isBlank()) {
                    println("ShosetsuLuaExtensionWrapper: Extension ${this@ShosetsuLuaExtensionWrapper.name} has no base URL configured")
                    return@withContext emptyList<ExtensionSearchResult>()
                }

                println("ShosetsuLuaExtensionWrapper: Searching '${query}' on ${shosetsuExtension.baseURL}")
                
                // Create a text filter for the search
                val filters = mapOf(0 to query)
                val novels = shosetsuExtension.search(filters)
                novels.map { novel ->
                    ExtensionSearchResult(
                        id = novel.link,
                        title = novel.title,
                        author = novel.authors.firstOrNull() ?: "",
                        description = novel.description ?: "",
                        imageUrl = novel.imageURL ?: "",
                        url = novel.link,
                        extensionId = id
                    )
                }
            } catch (e: Exception) {
                println("ShosetsuLuaExtensionWrapper: Search failed for query '$query' on ${this@ShosetsuLuaExtensionWrapper.name}: ${e.message}")
                // Check if it's a Lua script error
                if (e.message?.contains("vm error") == true || e.message?.contains("lib(") == true) {
                    println("ShosetsuLuaExtensionWrapper: This appears to be a Lua script error. The extension may need updating or the target website may have changed.")
                }
                // Return empty list instead of mock data
                emptyList()
            }
        }

    override suspend fun getLatest(): List<ExtensionSearchResult> =
        withContext(Dispatchers.IO) {
            try {
                // Try to get latest novels using Shosetsu's latest function
                val novels =
                    shosetsuExtension.search(emptyMap<Int, String>()) // Empty map for latest
                novels.map { novel ->
                    ExtensionSearchResult(
                        id = novel.link,
                        title = novel.title,
                        author = novel.authors.firstOrNull() ?: "",
                        description = novel.description ?: "",
                        imageUrl = novel.imageURL ?: "",
                        url = novel.link,
                        extensionId = id
                    )
                }
            } catch (e: Exception) {
                println("ShosetsuLuaExtensionWrapper: getLatest failed on ${this@ShosetsuLuaExtensionWrapper.name}: ${e.message}")
                // Check if it's a Lua script error
                if (e.message?.contains("vm error") == true || e.message?.contains("lib(") == true) {
                    println("ShosetsuLuaExtensionWrapper: This appears to be a Lua script error. The extension may need updating or the target website may have changed.")
                }
                // Return empty list instead of mock data
                emptyList()
            }
        }

    override suspend fun getBook(novelId: String): ExtensionBook? = withContext(Dispatchers.IO) {
        try {
            val novel = shosetsuExtension.parseNovel(novelId, true)
            ExtensionBook(
                id = novel.link,
                title = novel.title,
                author = novel.authors.firstOrNull() ?: "",
                description = novel.description ?: "",
                imageUrl = novel.imageURL ?: "",
                url = novel.link,
                status = "Unknown",
                genres = novel.tags.toList(),
                chapters = novel.chapters.map { chapter ->
                    ExtensionChapter(
                        id = chapter.link,
                        title = chapter.title,
                        content = "",
                        order = chapter.order.toInt(),
                        url = chapter.link,
                        releaseDate = System.currentTimeMillis() // Use current time as fallback
                    )
                }
            )
        } catch (e: Exception) {
            println("ShosetsuLuaExtensionWrapper: getBook failed for URL '$novelId' on ${this@ShosetsuLuaExtensionWrapper.name}: ${e.message}")
            // Check if it's a Lua script error
            if (e.message?.contains("vm error") == true || e.message?.contains("lib(") == true) {
                println("ShosetsuLuaExtensionWrapper: This appears to be a Lua script error. The extension may need updating or the target website may have changed.")
            }
            // Return null instead of mock data
            null
        }
    }

    override suspend fun getChapter(bookId: String, chapterId: String): ExtensionChapter? =
        withContext(Dispatchers.IO) {
            try {
                val chapterContent = shosetsuExtension.getPassage(chapterId)
                ExtensionChapter(
                    id = chapterId,
                    title = "Chapter Title", // Chapter title may not be available from getPassage
                    content = String(chapterContent), // Convert ByteArray to String
                    order = 0, // Order not available from getPassage
                    url = chapterId,
                    releaseDate = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                println("ShosetsuLuaExtensionWrapper: getChapter failed for URL '$chapterId' on ${this@ShosetsuLuaExtensionWrapper.name}: ${e.message}")
                // Check if it's a Lua script error
                if (e.message?.contains("vm error") == true || e.message?.contains("lib(") == true) {
                    println("ShosetsuLuaExtensionWrapper: This appears to be a Lua script error. The extension may need updating or the target website may have changed.")
                }
                // Return null instead of mock data
                null
            }
        }

    override suspend fun getChapters(bookId: String): List<ExtensionChapter> =
        withContext(Dispatchers.IO) {
            try {
                val novelData = shosetsuExtension.parseNovel(bookId, true)
                novelData.chapters.map { chapter ->
                    ExtensionChapter(
                        id = chapter.link,
                        title = chapter.title,
                        content = "",
                        order = chapter.order.toInt(),
                        url = chapter.link,
                        releaseDate = System.currentTimeMillis() // Use current time as fallback
                    )
                }
            } catch (e: Exception) {
                println("ShosetsuLuaExtensionWrapper: getChapters failed for book '$bookId' on ${this@ShosetsuLuaExtensionWrapper.name}: ${e.message}")
                // Check if it's a Lua script error
                if (e.message?.contains("vm error") == true || e.message?.contains("lib(") == true) {
                    println("ShosetsuLuaExtensionWrapper: This appears to be a Lua script error. The extension may need updating or the target website may have changed.")
                }
                emptyList()
            }
        }
}
