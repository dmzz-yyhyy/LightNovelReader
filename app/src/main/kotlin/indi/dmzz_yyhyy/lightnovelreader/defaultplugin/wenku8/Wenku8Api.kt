package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.github.michaelbull.result.get
import cxhttp.CxHttp
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book.BookRequestDispatcher
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.Wenku8ExplorePageProvider
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded.navigateToExploreExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.CxHttpInit
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils
import indi.dmzz_yyhyy.lightnovelreader.utils.network.UserAgentGenerator
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.content.component.ImageComponentData
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.select.Elements
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@WebDataSource(
    "Wenku8",
    "LightNovelReader from wenku8.net"
)
object Wenku8Api : WebBookDataSource {
    init {
        CxHttpInit.init()
    }

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
    private val bookRequestDispatcher = BookRequestDispatcher()
    private val isOffLineStateFlow = MutableStateFlow(false)
    private val DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val titleRegex = Regex("(.*) ?[(（](.*)[)）] ?$")
    private val hosts =
        listOf("https://www.wenku8.cc", "https://www.wenku8.net", "https://www.wenku8.com")
    override val cache = Cache(
        timeout = 2 * 60 * 60 * 1000
    )
    private val _cache = Cache(
        timeout = 2 * 60 * 60 * 1000
    )
    var host = hosts[0]

    override fun onLoad() {
        coroutineScope.launch {
            while (currentCoroutineContext().isActive) {
                offLine = isOffLine()
                isOffLineStateFlow.emit(offLine)
                delay(if (offLine) 3000 else 100000)
            }
        }
    }

    private inline fun <reified T : CanBeEmpty> ifCache(id: String, block: () -> T): T {
        val cacheData = _cache.getCache<T>(id.hashCode())
        if (cacheData == null) {
            val data = block.invoke()
            if (data.isEmpty()) return data
            _cache.cache(id.hashCode(), data)
            return data
        }
        return cacheData
    }

    override var offLine: Boolean = true

    override val isOffLineFlow = isOffLineStateFlow

    override suspend fun isOffLine(): Boolean = withContext(Dispatchers.IO) {
        suspend fun webSite(index: Int): Boolean {
            return !CxHttp
                .get(hosts[index]) {
                    header("user-agent", UserAgentGenerator.generate())
                    header(
                        "cookie",
                        wenku8Cookies().map { "${it.key}=${it.value}" }
                            .joinToString(separator = ";")
                    )
                }
                .await()
                .isSuccessful
                .also { host = hosts[index] }
        }
        return@withContext webSite(0) && webSite(1) && webSite(2)
    }

    override val id: Int = "wenku8".hashCode()

    override suspend fun getBookInformation(id: String): BookInformation = ifCache(id) {
        bookRequestDispatcher.getBookInformation(id)
    }

    override suspend fun getBookVolumes(id: String): BookVolumes = ifCache(id) {
        bookRequestDispatcher.getBookVolumes(id)
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent =
        ifCache(chapterId + bookId) {
            bookRequestDispatcher.getChapterContent(chapterId, bookId)
        }

    override val searchProvider: SearchProvider = Wenku8SearchProvider(bookRequestDispatcher)
    override val explorePageProvider: ExplorePageProvider = Wenku8ExplorePageProvider()


    override fun progressBookTagClick(tag: String, navController: NavController) {
        if (tagList.contains(tag))
            navController.navigateToExploreExpandDestination(tag)
    }

    override suspend fun getCoverUriInVolume(
        bookId: String,
        volume: Volume,
        volumeChapterContentMap: MutableMap<String, ChapterContent>,
        context: Context
    ): Uri? {
        return volume.chapters
            .find { it.title.endsWith("插图") }
            ?.let { chapterInformation ->
                val chapterContent = volumeChapterContentMap[chapterInformation.id] ?: return null
                if (chapterContent.isEmpty()) return null
                chapterContent.content["components"]?.jsonArray
                    ?.mapNotNull { it.jsonObject }
                    ?.filter {
                        it["id"]?.jsonPrimitive?.content == ImageComponentData.ID
                    }
                    ?.forEach {
                        val uri = it["data"]?.jsonObject["uri"]?.jsonPrimitive?.content?.toUri()
                            ?: return null
                        val bitmap = ImageUtils.uriToBitmap(uri, context).get() ?: return@forEach
                        if (bitmap.height > bitmap.width) return uri
                    }
                return null
            }
    }

    fun getBookInformationListFromBookCards(elements: Elements): List<BookInformation> =
        elements
            .map { element ->
                if (element.text().contains("因版权问题")) {
                    val id = element
                        .selectFirst("div > div:nth-child(1) > a")
                        ?.attr("href")
                        ?.replace("/book/", "")
                        ?.replace(".htm", "") ?: ""
                    val bookInformation = MutableBookInformation.empty()
                    bookInformation.id = id
                    coroutineScope.launch(Dispatchers.IO) {
                        val new = getBookInformation(
                            element
                                .selectFirst("div > div:nth-child(1) > a")
                                ?.attr("href")
                                ?.replace("/book/", "")
                                ?.replace(".htm", "") ?: ""
                        )
                        if (new.isNotEmpty()) {
                            bookInformation.update(new)
                        }
                    }
                    bookInformation
                } else {
                    val titleGroup = element.selectFirst("div > div:nth-child(1) > a")
                        ?.attr("title")
                        ?.let { it1 -> titleRegex.find(it1)?.groups }
                    MutableBookInformation(
                        id = element.selectFirst("div > div:nth-child(1) > a")
                            ?.attr("href")
                            ?.replace("/book/", "")
                            ?.replace(".htm", "") ?: "",
                        title = titleGroup?.get(1)?.value
                            ?: element.selectFirst("div > div:nth-child(1) > a")
                                ?.attr("title") ?: "",
                        subtitle = titleGroup?.get(2)?.value ?: "",
                        coverUrl = element.selectFirst("div > div:nth-child(1) > a > img")
                            ?.attr("src")?.toUri() ?: Uri.EMPTY,
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
                        wordCount = WordCount(
                            element.selectFirst("div > div:nth-child(2) > p:nth-child(3)")
                                ?.text()?.split("/")?.getOrNull(1)
                                ?.split(":")?.getOrNull(1)
                                ?.replace("K", "")?.toInt()?.times(1000) ?: -1
                        ),
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
}