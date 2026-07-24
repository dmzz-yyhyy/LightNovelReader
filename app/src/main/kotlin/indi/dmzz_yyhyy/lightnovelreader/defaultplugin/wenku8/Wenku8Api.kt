package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.runCatching
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book.BookRequestDispatcher
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.Wenku8ExplorePageProvider
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded.navigateToExploreExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils
import indi.dmzz_yyhyy.lightnovelreader.utils.network.UserAgentGenerator
import indi.dmzz_yyhyy.lightnovelreader.utils.ofId
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cookies.ConstantCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.http.userAgent
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.content.component.ImageComponentData
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.io.EOFException
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.ConnectException
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds


@WebDataSource(
    "Wenku8",
    "LightNovelReader from wenku8.net"
)
class Wenku8Api : WebBookDataSource {
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
    val ktorClient = HttpClient(OkHttp) {
        install(UserAgent) {
            agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/125.0.0.0 Safari/537.36"
        }

        install(HttpCookies) {
            storage = ConstantCookiesStorage(
                *(
                    createCookies("www.wenku8.net") +
                    createCookies("www.wenku8.cc") +
                    createCookies("www.wenku8.com")
                ).toTypedArray()
            )
        }

        install(DefaultRequest) {
            headers {
                append(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                append(HttpHeaders.AcceptLanguage, "zh-CN,zh;q=0.9,en;q=0.8")
                append(HttpHeaders.CacheControl, "max-age=0")
                append("Upgrade-Insecure-Requests", "1")
                append("Sec-Fetch-Dest", "document")
                append("Sec-Fetch-Mode", "navigate")
                append("Sec-Fetch-Site", "none")
                append("Sec-Fetch-User", "?1")
            }
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
            retryIf { _, response ->
                !response.status.isSuccess()
            }
            retryOnExceptionIf { _, cause ->
                cause is EOFException || cause is ConnectException
            }
        }
        install(HttpTimeout)
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.HEADERS
        }
    }
    private val hosts =
        listOf("https://www.wenku8.cc", "https://www.wenku8.net", "https://www.wenku8.com")
    var host = hosts[0]
    private val bookRequestDispatcher = BookRequestDispatcher(host, this)
    private val isOffLineStateFlow = MutableStateFlow(false)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val requestLimiter = Semaphore(3)
    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val titleRegex = Regex("(.*) ?[(（](.*)[)）] ?$")
    override val cache = Cache(
        timeout = 2 * 60 * 60 * 1000
    )

    override fun onLoad() {
        coroutineScope.launch {
            while (currentCoroutineContext().isActive) {
                offLine = isOffLine()
                isOffLineStateFlow.emit(offLine)
                delay((if (offLine) 3000 else 100000).milliseconds)
            }
        }
    }

    override var offLine: Boolean = true

    override val isOffLineFlow = isOffLineStateFlow

    fun wenku8Cookies(): Map<String, String> = mapOf(
        "jieqiUserInfo" to "jieqiUserId=1125456,jieqiUserName=yyhyy,jieqiUserGroup=3,jieqiUserVip=0,jieqiUserPassword=eb62861281462fd923fb99218735fef0,jieqiUserName_un=yyhyy,jieqiUserHonor_un=&#x4E2D;&#x7EA7;&#x4F1A;&#x5458;,jieqiUserGroupName_un=&#x666E;&#x901A;&#x4F1A;&#x5458;,jieqiUserLogin=1739294499",
        "jieqiVisitInfo" to "jieqiUserLogin=1739294499,jieqiUserId=1125456",
        "HMACCOUNT" to "E7837B0FF79F0590",
    )

    fun createCookies(domain: String): List<Cookie> {
        return wenku8Cookies().map {
            Cookie(
                name = it.key,
                value = it.value,
                domain = domain,
            )
        }
    }


    suspend fun anyTrue(
        tasks: List<suspend () -> Boolean>
    ): Boolean = coroutineScope {
        val deferredList = tasks.map { task ->
            async {
                task()
            }
        }.toMutableList()

        try {
            while (deferredList.isNotEmpty()) {
                val (finished, value) = select {
                    deferredList.forEach { deferred ->
                        deferred.onAwait { result ->
                            deferred to result
                        }
                    }
                }

                deferredList.remove(finished)

                if (value) {
                    deferredList.forEach { it.cancel() }
                    return@coroutineScope true
                }
            }

            false
        } finally {
            deferredList.forEach { it.cancel() }
        }
    }

    override suspend fun isOffLine(): Boolean = withContext(Dispatchers.IO) {
        suspend fun webSite(index: Int): Boolean = runCatching {
            ktorClient.get(hosts[index]) {
                userAgent(UserAgentGenerator.generate())
                wenku8Cookies().forEach { (name, value) ->
                    cookie(name, value)
                }
            }.status.isSuccess()
        }.getOrElse { false }
        return@withContext !anyTrue(listOf(
            { webSite(0) },
            { webSite(1) },
            { webSite(2) },
        ))
    }

    override val id = "Wenku8".ofId()

    override suspend fun getBookInformation(id: String) = bookRequestDispatcher.getBookInformation(id)

    override suspend fun getBookVolumes(id: String) = bookRequestDispatcher.getBookVolumes(id)

    override suspend fun getChapterContent(chapterId: String, bookId: String) = bookRequestDispatcher.getChapterContent(chapterId, bookId)

    override val searchProvider: SearchProvider = Wenku8SearchProvider(bookRequestDispatcher)
    override val explorePageProvider: ExplorePageProvider = Wenku8ExplorePageProvider(host, this)


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
                chapterContent.content["components"]?.jsonArray
                    ?.mapNotNull { it.jsonObject }
                    ?.filter {
                        it["id"]?.jsonPrimitive?.content == ImageComponentData.id.toString()
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

    fun getBookInformationListFromBookCards(elements: Elements): List<Pair<String, Result<BookInformation, WebRequestError>>> =
        elements
            .mapNotNull { element ->
                if (element.text().contains("因版权问题")) {
                    val id = element
                        .selectFirst("div > div:nth-child(1) > a")
                        ?.attr("href")
                        ?.replace("/book/", "")
                        ?.replace(".htm", "") ?: return@mapNotNull null
                    val titleGroup = element.selectFirst("div > div:nth-child(1) > a")
                        ?.attr("title")
                        ?.let { it1 -> titleRegex.find(it1)?.groups }
                    val title = titleGroup?.get(1)?.value
                        ?: element.selectFirst("div > div:nth-child(1) > a")
                            ?.attr("title") ?: ""
                    id to Err(WebRequestError("版权错误", "由于「$title」为Wenku8的版权"))
                } else {
                    val id = element.selectFirst("div > div:nth-child(1) > a")
                        ?.attr("href")
                        ?.replace("/book/", "")
                        ?.replace(".htm", "") ?: ""
                    val titleGroup = element.selectFirst("div > div:nth-child(1) > a")
                        ?.attr("title")
                        ?.let { it1 -> titleRegex.find(it1)?.groups }
                    id to BookInformation(
                        id = id,
                        title = titleGroup?.get(1)?.value
                            ?: element.selectFirst("div > div:nth-child(1) > a")
                                ?.attr("title") ?: "",
                        subtitle = titleGroup?.get(2)?.value ?: "",
                        coverUri = element.selectFirst("div > div:nth-child(1) > a > img")
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
                                LocalDate.parse(it, dateTimeFormatter)
                            }
                            ?.atStartOfDay() ?: LocalDateTime.MIN,
                        isComplete = element.selectFirst("div > div:nth-child(2) > p:nth-child(3)")
                            ?.text()?.split("/")?.getOrNull(2) == "已完结"
                    ).let { Ok(it) }
                }
            }

    suspend fun getWithWenku8Cookie(url: String): Result<Document, Throwable> = withContext(Dispatchers.IO) {
        requestLimiter.withPermit {
            runCatching {
                 val res = ktorClient.get(url)
                     .bodyAsText(Charset.forName("GBK"))
                Jsoup.parse(res).outputSettings(
                    Document.OutputSettings()
                        .prettyPrint(false)
                        .syntax(Document.OutputSettings.Syntax.xml)
                )
            }
        }
    }
}