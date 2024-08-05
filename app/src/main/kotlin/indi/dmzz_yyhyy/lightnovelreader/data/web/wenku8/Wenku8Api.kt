package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8


import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.IsCompletedSwitchFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.SingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.Wenku8AllExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.Wenku8HomeExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.Wenku8TagsExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage.HomeBookExpandPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage.filter.FirstLetterSingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage.filter.PublishingHouseSingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.utils.wenku8.wenku8Api
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
import org.jsoup.select.Elements

object Wenku8Api: WebBookDataSource {
    private var allBookChapterListCacheId: Int = -1
    private var allBookChapterListCache: List<ChapterInformation> = emptyList()
    private val DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val isOffLineFlow = flow {
        while(true) {
            emit(isOffLine())
            delay(2500)
        }
    }
    private val explorationExpandedPageDataSourceMap = mutableMapOf<String, ExplorationExpandedPageDataSource>()
    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private fun isOffLine(): Boolean {
        try {
            Jsoup.connect("http://app.wenku8.com/").get()
            return false
        } catch (_: Exception) {
            return true
        }
    }

    override suspend fun getIsOffLineFlow(): Flow<Boolean> = isOffLineFlow

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

    override fun getExplorationExpandedPageDataSourceMap(): Map<String, ExplorationExpandedPageDataSource> = explorationExpandedPageDataSourceMap

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

    suspend fun getBookInformationListFromBookCards(elements: Elements): List<BookInformation> =
        elements
            .map { element ->
                if (element.text().contains("因版权问题"))
                    getBookInformation(element
                        .selectFirst("div > div:nth-child(1) > a")
                        ?.attr("href")
                        ?.replace("/book/", "")
                        ?.replace(".htm", "")
                        ?.toInt() ?: -1
                    )
                else
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
                        tags = element.selectFirst("div > div:nth-child(2) > p:nth-child(4) > span")
                            ?.text()?.split(" ") ?: emptyList(),
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

    private fun registerExplorationExpandedPageDataSource(id: String, expandedPageDataSource: ExplorationExpandedPageDataSource) =
            explorationExpandedPageDataSourceMap.put(id, expandedPageDataSource)

    init {
        registerExplorationExpandedPageDataSource(
            id = "allBook",
            expandedPageDataSource = HomeBookExpandPageDataSource(
                title = "轻小说列表",
                filtersBuilder = {
                    val choicesMap = mapOf(
                        Pair("任意", ""),
                        Pair("0~9", "1")
                    )
                    listOf(
                        IsCompletedSwitchFilter { this.refresh() },
                        FirstLetterSingleChoiceFilter {
                            this.arg = choicesMap[it.trim()] ?: it.trim()
                            if (this.arg != "") this.arg += "&initial="
                            this.refresh()
                        },
                        PublishingHouseSingleChoiceFilter { this.refresh() }
                    )
                }
            )
        )
        registerExplorationExpandedPageDataSource(
            id = "allCompletedBook",
            expandedPageDataSource = HomeBookExpandPageDataSource(
                title = "完结全本",
                filtersBuilder = {
                    val choicesMap = mapOf(
                        Pair("任意", ""),
                        Pair("0~9", "1")
                    )
                    listOf(
                        IsCompletedSwitchFilter { this.refresh() },
                        FirstLetterSingleChoiceFilter {
                            this.arg = choicesMap[it.trim()] ?: it.trim()
                            if (this.arg != "") this.arg += "&initial="
                            this.refresh()
                        },
                        PublishingHouseSingleChoiceFilter { this.refresh() }
                    )
                },
                extendedParameters = "&fullflag=1"
            )
        )
        listOf("allvisit", "anime", "lastupdate", "postdate").forEach { id ->
            val nameMap = mapOf(
                Pair("allvisit", "热门轻小说"),
                Pair("anime", "动画化作品"),
                Pair("lastupdate", "今日更新"),
                Pair("postdate", "新书一览"),
            )
            registerExplorationExpandedPageDataSource(
                id = "${id}Book",
                expandedPageDataSource = HomeBookExpandPageDataSource(
                    baseUrl = "https://www.wenku8.net/modules/article/toplist.php",
                    title = nameMap[id] ?: "",
                    filtersBuilder = {
                        val choicesMap = mapOf(
                            Pair("任意", ""),
                            Pair("0~9", "1")
                        )
                        listOf(
                            IsCompletedSwitchFilter { this.refresh() },
                            FirstLetterSingleChoiceFilter {
                                this.arg = choicesMap[it.trim()] ?: it.trim()
                                if (this.arg != "") this.arg += "&initial="
                                this.refresh()
                            },
                            PublishingHouseSingleChoiceFilter { this.refresh() }
                        )
                    },
                    extendedParameters = "&sort=$id",
                    contentSelector = "#content > table > tbody > tr > td > div"
                )
            )
        }
        listOf("校园", "青春", "恋爱", "治愈", "群像",
                "竞技", "音乐", "美食", "旅行", "欢乐向",
                "经营", "职场", "斗智", "脑洞", "宅文化",
                "穿越", "奇幻", "魔法", "异能", "战斗",
                "科幻", "机战", "战争", "冒险", "龙傲天",
                "悬疑", "犯罪", "复仇", "黑暗", "猎奇",
                "惊悚", "间谍", "末日", "游戏", "大逃杀",
                "青梅竹马", "妹妹", "女儿", "JK", "JC",
                "大小姐", "性转", "伪娘", "人外",
                "后宫", "百合", "耽美", "NTR", "女性视角").forEach { tag ->
            registerExplorationExpandedPageDataSource(
                id = tag,
                expandedPageDataSource = HomeBookExpandPageDataSource(
                    baseUrl = "https://www.wenku8.net/modules/article/tags.php",
                    title = tag,
                    filtersBuilder = {
                        val choicesMap = mapOf(
                            Pair("默认", ""),
                            Pair("按更新时间排序", ""),
                            Pair("按热度排序", "&v=1"),
                            Pair("仅动画化", "&v=3")
                        )
                        listOf(
                            IsCompletedSwitchFilter { this.refresh() },
                            SingleChoiceFilter(
                                title = "排序",
                                dialogTitle = "排序顺序",
                                description = "书本排序的依据。",
                                choices = listOf("默认", "按更新时间排序", "按热度排序", "仅动画化"),
                                defaultChoice = "默认"
                            ) {
                                this.arg = choicesMap[it.trim()] ?: ""
                                this.refresh()
                            },
                            PublishingHouseSingleChoiceFilter { this.refresh() }
                        )
                    },
                    extendedParameters = "&t=${URLEncoder.encode(tag, "gb2312")}",
                    contentSelector = "#content > table > tbody > tr:nth-child(2) > td > div"
                )
            )
        }
    }
}