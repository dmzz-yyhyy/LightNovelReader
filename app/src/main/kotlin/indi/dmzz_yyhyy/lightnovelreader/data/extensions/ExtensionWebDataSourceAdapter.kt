package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
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
    private val extensionConverter: ExtensionConverter
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
        "latest" to ExtensionExplorationPageDataSource(extension, extensionConverter)
    )

    override val explorationExpandedPageDataSourceMap: Map<String, ExplorationExpandedPageDataSource> =
        emptyMap()

    override val searchTypeMap: Map<String, String> = mapOf("search" to "Search")

    override val searchTipMap: Map<String, String> = mapOf("search" to "Search for novels...")

    override val searchTypeIdList: List<String> = listOf("search")

    override suspend fun getBookInformation(id: Int): BookInformation {
        return try {
            val book = extension.getBook(id.toString())
            if (book != null) {
                extensionConverter.convertExtensionBookToBookInfo(book, extension.id)
            } else {
                BookInformation.empty(id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            BookInformation.empty(id)
        }
    }

    override suspend fun getBookVolumes(id: Int): BookVolumes {
        return try {
            val chapters = extension.getChapters(id.toString())
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
        } catch (e: Exception) {
            e.printStackTrace()
            BookVolumes(id, emptyList())
        }
    }

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent {
        return try {
            val chapter = extension.getChapter(bookId.toString(), chapterId.toString())
            if (chapter != null) {
                extensionConverter.convertExtensionChapterToChapterContent(chapter, chapterId)
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
                    extensionConverter.convertSearchResultToBookInfo(result, extension.id)
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
    private val extensionConverter: ExtensionConverter
) : ExplorationPageDataSource {

    private var lock = false
    private val explorationBooksRows: MutableStateFlow<List<ExplorationBooksRow>> =
        MutableStateFlow(emptyList())

    override val title = "${extension.name} Latest"

    override fun getExplorationPage(): ExplorationPage {
        if (!lock) {
            lock = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val latestBooks = extension.getLatest()
                    val explorationBooks = latestBooks.map { result ->
                        ExplorationDisplayBook(
                            id = result.id.toIntOrNull() ?: 0,
                            title = result.title,
                            author = result.author,
                            coverUrl = result.imageUrl
                        )
                    }

                    val booksRow = ExplorationBooksRow(
                        title = "${extension.name} Latest",
                        bookList = explorationBooks
                    )

                    explorationBooksRows.value = listOf(booksRow)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return ExplorationPage(title, explorationBooksRows)
    }
}