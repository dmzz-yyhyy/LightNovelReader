package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8


import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.utils.wenku8.wenku8Api
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Wenku8Api: WebBookDataSource {
    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var allBookChapterListCacheId: Int = -1
    private var allBookChapterListCache: List<ChapterInformation> = emptyList()
    private val DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val isOffLineFlow = flow {
        while(true) {
            emit(isOffLine())
            delay(2500)
        }
    }
    private var isStopSearch = false
    private var runningSearchCount = 0

    private fun isOffLine(): Boolean {
        try {
            Jsoup.connect("http://app.wenku8.com/").get()
            return false
        } catch (_: Exception) {
            return true
        }
    }

    override suspend fun getIsOffLineFlow(): Flow<Boolean> = isOffLineFlow

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun getBookInformation(id: Int): BookInformation {
        if (isOffLine()) return BookInformation.empty()
        return wenku8Api("action=book&do=meta&aid=$id&t=0").let {
            BookInformation(
                id = id,
                title = it.selectFirst("[name=Title]")?.text() ?: "",
                coverUrl = "https://img.wenku8.com/image/${id/1000}/$id/${id}s.jpg",
                author = it.selectFirst("[name=Author]")?.attr("value") ?: "",
                description = wenku8Api("action=book&do=intro&aid=$id&t=0").text(),
                tags = it.selectFirst("[name=Tags]")?.attr("value")?.split(" ") ?: emptyList(),
                publishingHouse = it.selectFirst("[name=PressId]")?.attr("value") ?: "",
                wordCount = it.selectFirst("[name=BookLength]")?.attr("value")?.toInt() ?: -1,
                lastUpdated = LocalDate.parse(it.selectFirst("[name=LastUpdate]")?.attr("value")).atStartOfDay(),
                isComplete = it.selectFirst("[name=BookStatus]")?.attr("value") == "已完成"
            )
        }
    }

    override suspend fun getBookVolumes(id: Int): BookVolumes {
        if (isOffLine()) return BookVolumes.empty()
        return BookVolumes(wenku8Api("action=book&do=list&aid=$id&t=0")
            .select("volume")
            .map { element ->
                Volume(
                    volumeId = element.attr("vid").toInt(),
                    volumeTitle = element.ownText(),
                    chapters = element.select("volume > chapter")
                        .map {
                            ChapterInformation(
                                id = it.attr("cid").toInt(),
                                title = it.text(),
                            )
                        }
                )
            }
        )
    }

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent {
        if (isOffLine()) return ChapterContent.empty()
        if (allBookChapterListCacheId != bookId) {
            allBookChapterListCacheId = bookId
            allBookChapterListCache = getBookVolumes(bookId).let { bookVolumes ->
                var list = emptyList<ChapterInformation>()
                bookVolumes.volumes.forEach {
                    list = list + it.chapters
                }
                return@let list
            }
        }
        return wenku8Api("action=book&do=text&aid=$bookId&cid=$chapterId&t=0")
            .let { document ->
                document
                    .toString()
                    .replace("<html><head></head><body>\n\n", "")
                    .replace("\n \n</body></html>", "")
                    .let { s ->
                        println(allBookChapterListCache)
                        ChapterContent(
                            id = chapterId,
                            title = s.split("\n\n \n  \n \n\n \n    \n    \n    \n \n\n\n \n \n")[0],
                            content = s
                                .split("\n\n \n  \n \n\n \n    \n    \n    \n \n\n\n \n \n")[1]
                                .replace("<!--image-->", "[image]")
                                .replace("       <!--image-->", "\n[image]")
                            ,
                            lastChapter = allBookChapterListCache
                                .indexOfFirst { it.id == chapterId }
                                .let {
                                    if (it == -1) it else allBookChapterListCache.getOrNull(it - 1)?.id ?: -1
                                },
                            nextChapter = allBookChapterListCache
                                .indexOfFirst { it.id == chapterId }
                                .let {
                                    if (it == -1) it else allBookChapterListCache.getOrNull(it + 1)?.id ?: -1
                                }
                        )
                    }
            }
    }

    override suspend fun getExplorationPageMap(): Map<String, ExplorationPageDataSource> =
        mapOf(
            Pair("首页", Wenku8HomeExplorationPage),
            Pair("全部", Wenku8AllExplorationPage),
            Pair("分类", Wenku8TagsExplorationPage)
        )

    override suspend fun getExplorationPageTitleList(): List<String> = listOf("首页", "全部", "分类")

    private fun getSearchResult(document: Document): List<BookInformation> = document
            .select("#content > table > tbody > tr > td > div")
            .map { element ->
                BookInformation(
                    id = element.selectFirst("div > div:nth-child(1) > a")
                        ?.attr("href")
                        ?.replace("/book/", "")
                        ?.replace(".htm", "")
                        ?.toInt() ?: -1,
                    title = element.selectFirst("div > div:nth-child(1) > a")
                        ?.attr("title") ?: "",
                    coverUrl = element.selectFirst("div > div:nth-child(1) > a > img")
                        ?.attr("src") ?: "",
                    author = element.selectFirst("div > div:nth-child(2) > p:nth-child(2)")
                        ?.text()?.split("/")?.getOrNull(0)
                        ?.split(":")?.getOrNull(1) ?: "",
                    description = element.selectFirst("div > div:nth-child(2) > p:nth-child(5)")
                        ?.text()?.replace("简介:", "") ?: "",
                    tags = emptyList(),
                    publishingHouse = element.selectFirst("div > div:nth-child(2) > p:nth-child(2)")
                        ?.text()?.split("/")?.getOrNull(1)
                        ?.split(":")?.getOrNull(1) ?: "",
                    wordCount = element.selectFirst("div > div:nth-child(2) > p:nth-child(3)")
                        ?.text()?.split("/")?.getOrNull(1)
                        ?.split(":")?.getOrNull(1)
                        ?.replace("K", "")?.toInt()?.times(1000) ?: -1,
                    lastUpdated = element.selectFirst("div > div:nth-child(2) > p:nth-child(3)")
                        ?.text()?.split("/")?.getOrNull(0)
                        ?.split(":")?.getOrNull(1)
                        ?.let {
                            LocalDate.parse(it, DATA_TIME_FORMATTER)
                        }
                        ?.atStartOfDay() ?: LocalDateTime.MIN,
                    isComplete = element.selectFirst("div > div:nth-child(2) > p:nth-child(3)")
                        ?.text()?.split("/")?.getOrNull(2) == "已完结"
                )
            }

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        val searchResult = MutableStateFlow(emptyList<BookInformation>())
        if (isOffLine()) return searchResult
        val encodedKeyword = URLEncoder.encode(keyword, "gb2312")
        coroutineScope.launch {
            delay(1)
            wenku8Api("action=search&searchtype=$searchResult&searchkey=${URLEncoder.encode(encodedKeyword, "utf-8")}")
                .select("item")
                .forEach { element ->
                    searchResult.update {
                        it + listOf(getBookInformation(element.attr("aid").toInt()))
                    }
                }
                .let {
                    searchResult.update {
                        it + listOf(BookInformation.empty())
                    }
                }
        }
        return searchResult
    }

    override fun stopAllSearch() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.IO)
    }

    override fun getSearchTypeNameList(): List<String> =
        listOf("按书名搜索", "按作者名搜索")

    override fun getSearchTypeMap(): Map<String, String> =
        mapOf(
            Pair("按书名搜索", "articlename"),
            Pair("按作者名搜索", "author"),
        )

    override fun getSearchTipMap(): Map<String, String> =
        mapOf(
            Pair("articlename", "请输入书本名称"),
            Pair("author", "请输入作者名称"),
        )
}