package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Adapter that wraps an Extension to implement the WebBookDataSource interface
 */
class ExtensionWebDataSourceAdapter(
    private val extension: Extension
) : WebBookDataSource {

    override val id: Int = extension.id.hashCode()
    
    // Expose extension details for UI
    val extensionName: String = extension.name
    val extensionId: String = extension.id

    override suspend fun isOffLine(): Boolean = false

    override val offLine: Boolean = false

    override val isOffLineFlow: Flow<Boolean> = MutableStateFlow(false)

    override val explorationPageIdList: List<String> = listOf("latest")

    override val explorationPageDataSourceMap: Map<String, ExplorationPageDataSource> = mapOf(
        "latest" to ExtensionExplorationPageDataSource(extension)
    )

    override val explorationExpandedPageDataSourceMap: Map<String, ExplorationExpandedPageDataSource> = emptyMap()

    override val searchTypeMap: Map<String, String> = mapOf("search" to "Search")

    override val searchTipMap: Map<String, String> = mapOf("search" to "Search for novels...")

    override val searchTypeIdList: List<String> = listOf("search")

    override suspend fun getBookInformation(id: Int): BookInformation {
        val extensionBook = extension.getBook(id.toString())
        return if (extensionBook != null) {
            BookInformation(
                id = id,
                title = extensionBook.title,
                author = extensionBook.author,
                description = extensionBook.description,
                coverUrl = extensionBook.imageUrl,
                tags = extensionBook.genres,
                status = extensionBook.status,
                lastUpdated = extensionBook.lastUpdated?.let { 
                    java.time.LocalDateTime.ofEpochSecond(it / 1000, 0, java.time.ZoneOffset.UTC)
                } ?: java.time.LocalDateTime.now()
            )
        } else {
            BookInformation.empty(id)
        }
    }

    override suspend fun getBookVolumes(id: Int): BookVolumes {
        val chapters = extension.getChapters(id.toString())
        return if (chapters != null) {
            // Convert extension chapters to BookVolumes format
            val volumes = listOf(
                Volume(
                    id = 1,
                    title = "Volume 1", // Default volume title
                    chapterIdList = chapters.mapIndexed { index, _ -> index + 1 }
                )
            )
            BookVolumes(
                bookId = id,
                volumes = volumes,
                chapters = chapters.mapIndexed { index, chapter ->
                    indi.dmzz_yyhyy.lightnovelreader.data.book.Chapter(
                        id = index + 1,
                        title = chapter.title,
                        bookId = id
                    )
                }
            )
        } else {
            BookVolumes.empty(id)
        }
    }

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent {
        val chapters = extension.getChapters(bookId.toString())
        if (chapters != null && chapterId <= chapters.size && chapterId > 0) {
            val chapter = chapters[chapterId - 1] // Convert to 0-based index
            val extensionChapter = extension.getChapter(bookId.toString(), chapter.id)
            return if (extensionChapter != null) {
                ChapterContent(
                    chapterId = chapterId,
                    title = extensionChapter.title,
                    content = extensionChapter.content
                )
            } else {
                ChapterContent.empty(chapterId)
            }
        }
        return ChapterContent.empty(chapterId)
    }

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = flow {
        try {
            val searchResults = extension.search(keyword)
            val bookInfoList = searchResults.map { result ->
                BookInformation(
                    id = result.id.hashCode(), // Convert string ID to int
                    title = result.title,
                    author = result.author,
                    description = result.description,
                    coverUrl = result.imageUrl,
                    tags = emptyList(), // Extensions don't typically provide tags in search results
                    status = "Unknown",
                    lastUpdated = java.time.LocalDateTime.now()
                )
            }
            // Emit results with empty BookInformation at the end to signal completion
            emit(bookInfoList + listOf(BookInformation.empty()))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(listOf(BookInformation.empty()))
        }
    }

    override fun stopAllSearch() {
        // Extension searches are typically one-shot, so nothing to stop
    }

    override fun progressBookTagClick(tag: String, navController: NavController) {
        // Default implementation - no special tag handling
    }

    override fun getCoverUrlInVolume(
        bookId: Int,
        volume: Volume,
        volumeChapterContentMap: Map<Int, ChapterContent>
    ): String? = null
}

/**
 * ExplorationPageDataSource implementation for extensions
 */
class ExtensionExplorationPageDataSource(
    private val extension: Extension
) : ExplorationPageDataSource {

    override val id: String = "latest"
    override val title: String = "Latest"
    override val rowTitleList: List<String> = listOf("Latest Novels")

    override suspend fun provide(): Map<String, List<BookInformation>> {
        return try {
            val latestNovels = extension.getLatest()
            val bookInfoList = latestNovels.map { result ->
                BookInformation(
                    id = result.id.hashCode(), // Convert string ID to int
                    title = result.title,
                    author = result.author,
                    description = result.description,
                    coverUrl = result.imageUrl,
                    tags = emptyList(),
                    status = "Unknown",
                    lastUpdated = java.time.LocalDateTime.now()
                )
            }
            mapOf("Latest Novels" to bookInfoList)
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf("Latest Novels" to emptyList())
        }
    }
}
