package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8


import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.MutableBookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.MutableChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.IsCompletedSwitchFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.SingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.WordCountFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.Wenku8AllExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.Wenku8HomeExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.Wenku8TagsExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage.HomeBookExpandPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage.filter.FirstLetterSingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage.filter.PublishingHouseSingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.expanded.navigateToExplorationExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.update
import io.nightfish.potatoautoproxy.ProxyPool
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
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Wenku8Api: WebBookDataSource {
    private val tagList = listOf(
        "校园", "青春", "恋爱", "治愈", "群像",
        "竞技", "音乐", "美食", "旅行", "欢乐向",
        "经营", "职场", "斗智", "脑洞", "宅文化",
        "穿越", "奇幻", "魔法", "异能", "战斗",
        "科幻", "机战", "战争", "冒险", "龙傲天",
        "悬疑", "犯罪", "复仇", "黑暗", "猎奇",
        "惊悚", "间谍", "末日", "游戏", "大逃杀",
        "青梅竹马", "妹妹", "女儿", "JK", "JC",
        "大小姐", "性转", "伪娘", "人外",
        "后宫", "百合", "耽美", "NTR", "女性视角"
    )
    private var allBookChapterListCacheId: Int = -1
    private var allBookChapterListCache: List<ChapterInformation> = emptyList()
    private val DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    override val explorationExpandedPageDataSourceMap = mutableMapOf<String, ExplorationExpandedPageDataSource>()
    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val titleRegex = Regex("(.*) ?[(（](.*)[)）] ?$")
    private val hosts = listOf("https://www.wenku8.cc", "https://www.wenku8.net", "https://www.wenku8.com")
    private var hostIndex = 0
    private var isLocalIpUnableUse = true
    val host get() =  hosts[hostIndex]

    init {
        coroutineScope.launch {
            offLine = isOffLine()
        }
    }

    override var offLine: Boolean = true

    override val isOffLineFlow = flow {
        while(true) {
            offLine = isOffLine()
            emit(offLine)
            delay(if (offLine) 2000 else 6000)
        }
    }

    override suspend fun isOffLine(): Boolean =
        try {
            Jsoup
                .connect(update("eNpb85aBtYRBMaOkpMBKXz-xoECvPDUvu9RCLzk_Vz8xL6UoPzNFryCjAAAfiA5Q").toString())
                .timeout(2000)
                .let {
                    if (ProxyPool.enable && !isLocalIpUnableUse)
                        ProxyPool.apply {
                            it.proxyGet()
                        }
                    else it.get()
                }
            Jsoup
                .connect("$host/")
                .wenku8Cookie()
                .timeout(2000)
                .let {
                    if (ProxyPool.enable && !isLocalIpUnableUse)
                        ProxyPool.apply {
                            it.proxyGet()
                        }
                    else it.get()
                }
            false
        } catch (e: Exception) {
            //FIXME
            e.printStackTrace()
            if (hostIndex == hosts.size - 1)
                isLocalIpUnableUse = false
            hostIndex = (hostIndex + 1) % hosts.size
            true
        }

    override val id: Int = "wenku8".hashCode()

    override suspend fun getBookInformation(id: Int): BookInformation {
        return wenku8Api("action=book&do=meta&aid=$id&t=0")?.let {
            val titleGroup = it
                .selectFirst("[name=Title]")?.text()
                ?.let { it1 -> titleRegex.find(it1)?.groups }
            try {
                MutableBookInformation(
                    id = id,
                    title = titleGroup?.get(1)?.value ?: it.selectFirst("[name=Title]")?.text()
                    ?: "",
                    subtitle = titleGroup?.get(2)?.value ?: "",
                    coverUrl = "https://img.wenku8.com/image/${id / 1000}/$id/${id}s.jpg",
                    author = it.selectFirst("[name=Author]")?.attr("value") ?: "",
                    description = wenku8Api("action=book&do=intro&aid=$id&t=0")?.text() ?: "",
                    tags = it.selectFirst("[name=Tags]")?.attr("value")?.split(" ") ?: emptyList(),
                    publishingHouse = it.selectFirst("[name=PressId]")?.attr("value") ?: "",
                    wordCount = it.selectFirst("[name=BookLength]")?.attr("value")?.toInt() ?: -1,
                    lastUpdated = LocalDate.parse(
                        it.selectFirst("[name=LastUpdate]")?.attr("value"), DATA_TIME_FORMATTER
                    ).atStartOfDay(),
                    isComplete = it.selectFirst("[name=BookStatus]")?.attr("value") == "已完成"
                )
            } catch (e: NullPointerException) {
                e.printStackTrace()
                BookInformation.empty()
            }
        } ?: BookInformation.empty()
    }

    override suspend fun getBookVolumes(id: Int): BookVolumes {
        return BookVolumes(
            id,
            wenku8Api("action=book&do=list&aid=$id&t=0")
            ?.select("volume")
            ?.map { element ->
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
            } ?: emptyList()
        )
    }

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent {
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
                    ?.wholeText()
                    ?.let { s ->
                        var title = ""
                        var content = ""
                        s.split("\n").forEachIndexed { index, line ->
                            if (content != "") return@forEachIndexed
                            if (title == "" && line.any { !it.isWhitespace() }) {
                                title = line.trim()
                                return@forEachIndexed
                            }
                            if (title != "" && line.any { !it.isWhitespace() }) {
                                content = s.split("\n").drop(index).joinToString("\n")
                                return@forEachIndexed
                            }
                        }
                        val imagesResult = Regex("(http.*?)(<!--image-->)")
                            .findAll(document.toString())
                            .toList()
                        imagesResult.forEach {
                            content = content.replace(it.groups[1]?.value ?: it.value, "[image]${it.groups[1]?.value ?: ""}[image]")
                        }
                        MutableChapterContent(
                            id = chapterId,
                            title = title,
                            content = content,
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
                    } ?: ChapterContent.empty()
            }
    }

    override val explorationPageDataSourceMap: Map<String, ExplorationPageDataSource> =
        mapOf(
            Pair("首页", Wenku8HomeExplorationPage),
            Pair("全部", Wenku8AllExplorationPage),
            Pair("分类", Wenku8TagsExplorationPage)
        )

    override val explorationPageIdList: List<String> = listOf("首页", "全部", "分类")

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        val searchResult = MutableStateFlow(emptyList<BookInformation>())
        val encodedKeyword = URLEncoder.encode(keyword, "gb2312")
        coroutineScope.launch {
            delay(1)
            wenku8Api("action=search&searchtype=$searchType&searchkey=${URLEncoder.encode(encodedKeyword, "utf-8")}")
                ?.select("item")
                ?.forEach { element ->
                    searchResult.update {
                        it + listOf(getBookInformation(element.attr("aid").toInt()))
                    }
                }
                ?.let {
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

    override val searchTypeIdList =
        listOf("articlename", "author")

    override val searchTypeMap: Map<String, String> =
        mapOf(
            Pair("articlename", "按书名搜索"),
            Pair("author", "按作者名搜索"),
        )

    override val searchTipMap: Map<String, String> =
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
                else {
                    val titleGroup = element.selectFirst("div > div:nth-child(1) > a")
                        ?.attr("title")
                        ?.let { it1 -> titleRegex.find(it1)?.groups }
                    MutableBookInformation(
                        id = element.selectFirst("div > div:nth-child(1) > a")
                            ?.attr("href")
                            ?.replace("/book/", "")
                            ?.replace(".htm", "")
                            ?.toInt() ?: -1,
                        title = titleGroup?.get(1)?.value ?: element.selectFirst("div > div:nth-child(1) > a")
                            ?.attr("title") ?: "",
                        subtitle = titleGroup?.get(2)?.value ?: "",
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
                        PublishingHouseSingleChoiceFilter { this.refresh() },
                        WordCountFilter { this.refresh() }
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
                        PublishingHouseSingleChoiceFilter { this.refresh() },
                        WordCountFilter { this.refresh() }
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
                    baseUrl = "$host/modules/article/toplist.php",
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
                            PublishingHouseSingleChoiceFilter { this.refresh() },
                            WordCountFilter { this.refresh() }
                        )
                    },
                    extendedParameters = "&sort=$id",
                    contentSelector = "#content > table > tbody > tr > td > div"
                )
            )
        }
        tagList.forEach { tag ->
            registerExplorationExpandedPageDataSource(
                id = tag,
                expandedPageDataSource = HomeBookExpandPageDataSource(
                    baseUrl = "$host/modules/article/tags.php",
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
                                dialogTitleId = R.string.key_pub_filter_title,
                                descriptionId = R.string.key_pub_filter_desc,
                                choices = listOf("默认", "按更新时间排序", "按热度排序", "仅动画化"),
                                defaultChoice = "默认"
                            ) {
                                this.arg = choicesMap[it.trim()] ?: ""
                                this.refresh()
                            },
                            PublishingHouseSingleChoiceFilter { this.refresh() },
                            WordCountFilter { this.refresh() }
                        )
                    },
                    extendedParameters = "&t=${URLEncoder.encode(tag, "gb2312")}",
                    contentSelector = "#content > table > tbody > tr:nth-child(2) > td > div"
                )
            )
        }
    }

    override fun progressBookTagClick(tag: String, navController: NavController) {
        if (tagList.contains(tag))
            navController.navigateToExplorationExpandDestination(tag)
    }

    override fun getCoverUrlInVolume(bookId: Int, volume: Volume, volumeChapterContentMap: Map<Int, ChapterContent>): String? {
        return volume.chapters
            .find { it.title.endsWith("插图") }
            ?.let { chapterInformation ->
                val chapterContent = volumeChapterContentMap[chapterInformation.id] ?: return null
                if (chapterContent.isEmpty()) return null
                chapterContent.content.split("[image]")
                    .filter(String::isNotEmpty)
                    .forEach { singleText ->
                        if (singleText.startsWith("http://") || singleText.startsWith("https://")) {
                            return singleText
                        }
                    }
                return null
            }
    }
}