package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book

import androidx.core.net.toUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.parseBookPage
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.redirectedBookId
import indi.dmzz_yyhyy.lightnovelreader.utils.network.selectFirstXpath
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.content.builder.buildContent
import io.nightfish.lightnovelreader.api.content.builder.image
import io.nightfish.lightnovelreader.api.content.builder.paragraph
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.error.mapAsWebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.BookPageResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class Wenku8WebsiteDataSource(
    val host: String,
    val wenku8Api: Wenku8Api
) : Wenku8BookDataSource {
    private val titleRegex = Regex("(.*) ?[(（](.*)[)）] ?$")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val searchLock = Mutex()
    private var lastSearch: TimeSource.Monotonic.ValueTimeMark? = null

    private fun url(string: String) = "$host/$string"

    override suspend fun getBookInformation(id: String): Result<BookInformation, WebRequestError> =
        coroutineBinding {
            val soup = wenku8Api.getWithWenku8Cookie(url("book/$id.htm"))
                .mapAsWebRequestError("网络请求失败", "网络请求时出现了错误")
                .bind()
            val titleGroup = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[1]/td/table/tbody/tr/td[1]/span/b")
                ?.text()
                ?.let { titleRegex.find(it)?.groups }
            val title = titleGroup
                ?.get(1)?.value
                ?: soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[1]/td/table/tbody/tr/td[1]/span/b")
                    ?.text()
                ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind()
            if (soup.text().contains("因版权问题")) Err(
                WebRequestError(
                    "版权受限",
                    "由于数据源方面的版权原因，无法加载书本「$title」的信息。"
                )
            ).bind()
            return@coroutineBinding BookInformation(
                id = wenku8Api.id.child(Identifier::Book, id),
                title = titleGroup
                    ?.get(1)?.value
                    ?: soup
                        .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[1]/td/table/tbody/tr/td[1]/span/b")
                        ?.text()
                    ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind(),
                subtitle = titleGroup
                    ?.get(2)
                    ?.value
                    ?: "",
                coverUri = soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[2]/tbody/tr/td[1]/img")
                    ?.attr("src")
                    ?.toUri()
                    ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind(),
                author = soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[2]")
                    ?.text()
                    ?.replace("小说作者：", "")
                    ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind(),
                description = soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[2]/tbody/tr/td[2]/span[6]")
                    ?.text()
                    ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind(),
                tags = soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[2]/tbody/tr/td[2]/span[1]/b")
                    ?.text()
                    ?.replace("作品Tags：", "")
                    ?.split(" ")
                    ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind(),
                publishingHouse = soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[1]")
                    ?.text()
                    ?.replace("文库分类：", "")
                    ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind(),
                wordCount = soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[5]")
                    ?.text()
                    ?.replace("全文长度：", "")
                    ?.replace("字", "")
                    ?.toIntOrNull()
                    ?.let { WordCount(it) }
                    ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind(),
                lastUpdated = soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[4]")
                    ?.text()
                    ?.replace("最后更新：", "")
                    ?.let { LocalDate.parse(it, dateTimeFormatter) }
                    ?.atStartOfDay()
                    ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind(),
                isComplete = soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[3]")
                    ?.text()
                    ?.contains("已完结")
                    ?: Err(WebRequestError("解析错误", "无法解析该书本的信息(id=$id)")).bind()
            )
        }

    override suspend fun getBookVolumes(id: String): Result<BookVolumes, WebRequestError> =
        coroutineBinding {
            val soup =
                wenku8Api.getWithWenku8Cookie(url("novel/${id.toInt() / 1000}/$id/index.htm"))
                    .mapAsWebRequestError("网络请求失败", "网络请求时出现了错误")
                    .bind()
            val trs = soup.selectXpath("/html/body/table/tbody/tr")
            val volumes = mutableListOf<Volume>()
            var volume: Volume? = null
            var chapters = mutableListOf<ChapterInformation>()
            trs.forEach { tr ->
                if (tr.selectFirst("td")?.attr("class") == "vcss") {
                    volume?.let(volumes::add)
                    val td = tr.selectFirst("td")
                    val vId = td?.attr("vid") ?: Err(
                        WebRequestError(
                            "解析错误",
                            "无法解析该书本的目录(id=$id)"
                        )
                    ).bind()
                    val title = td.text().ifEmpty {
                        Err(
                            WebRequestError(
                                "解析错误",
                                "无法解析该书本的目录(id=$id)"
                            )
                        ).bind()
                    }
                    chapters = mutableListOf()
                    volume = Volume(wenku8Api.id.child(Identifier::Volume, vId), title, chapters)
                    return@forEach
                }
                for (td in tr.select("td > a")) {
                    val id = td
                        .attr("href")
                        .split(".")
                        .firstOrNull()
                        ?: Err(WebRequestError("解析错误", "无法解析该书本的目录(id=$id)")).bind()
                    val title = td.text().ifEmpty {
                        Err(
                            WebRequestError(
                                "解析错误",
                                "无法解析该书本的目录(id=$id)"
                            )
                        ).bind()
                    }
                    chapters.add(
                        ChapterInformation(
                            wenku8Api.id.child(Identifier::Chapter, id),
                            title
                        )
                    )
                }
            }
            volume?.let(volumes::add)
            return@coroutineBinding BookVolumes(wenku8Api.id.child(Identifier::Book, id), volumes)
        }

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ): Result<ChapterContent, WebRequestError> = coroutineBinding {
        val soup =
            wenku8Api.getWithWenku8Cookie(url("novel/${bookId.toInt() / 1000}/$bookId/$chapterId.htm"))
                .mapAsWebRequestError("网络请求失败", "网络请求时出现了错误")
                .bind()
        val title = soup.selectFirstXpath("//*[@id=\"title\"]")?.text() ?: Err(
            WebRequestError(
                "解析错误",
                "无法解析该章节的标题(id=$chapterId)"
            )
        ).bind()
        if (soup.text().contains("因版权问题")) Err(
            WebRequestError(
                "版权受限",
                "由于数据源方面的版权原因，无法加载书本「$title」的章节内容。"
            )
        ).bind()
        val content = soup.selectFirstXpath("//*[@id=\"content\"]") ?: Err(
            WebRequestError(
                "解析错误",
                "无法解析该章节的内容(id=$chapterId)"
            )
        ).bind()
        val jsonObject = buildContent {
            var blankLines = 0
            for (node in content.childNodes()) {
                when (node) {
                    is TextNode -> {
                        val text = node.nodeValue().replace(" ", "")
                        if (text.isBlank()) {
                            blankLines++
                            continue
                        }
                        if (blankLines >= 3) {
                            repeat(blankLines) {
                                paragraph {
                                    text("\n")
                                }
                            }
                        }
                        blankLines = 0
                        paragraph {
                            text(text.trim())
                        }
                    }
                    is Element if node.`is`("div.divimage") -> {
                        node
                            .selectFirst("img")
                            ?.attr("src")
                            ?.toUri()
                            ?.let(::image)
                    }
                }
            }
        }
        val prevChapter = soup.selectFirstXpath("//*[@id=\"foottext\"]/a[3]").let {
            it ?: return@let null
            if (it.attr("href") == "index.htm" || it.attr("href").contains("article")) null
            else it.attr("href").split(".").firstOrNull()
        }
        val nextChapter = soup.selectFirstXpath("//*[@id=\"foottext\"]/a[4]").let {
            it ?: return@let null
            if (it.attr("href") == "index.htm" || it.attr("href").contains("article")) null
            else it.attr("href").split(".").firstOrNull()
        }
        val neoTitle = wenku8Api.cache.getCache<BookVolumes>(bookId.hashCode())
            .let {
                if (it == null) {
                    val bookVolumes = getBookVolumes(bookId).get() ?: return@let null
                    wenku8Api.cache.cache(bookId.hashCode()) {
                        bookVolumes
                    }
                    return@let bookVolumes
                }
                return@let null
            }?.let { bookVolumes ->
                bookVolumes.volumes.forEach { volume ->
                    return@let volume.chapters.firstOrNull { it.id.id == chapterId }?.title
                }
                return@let null
            }
        return@coroutineBinding ChapterContent(
            id = wenku8Api.id.child(Identifier::Chapter, chapterId),
            title = neoTitle ?: title,
            content = jsonObject,
            prevChapter = prevChapter?.let { wenku8Api.id.child(Identifier::Chapter, it) },
            nextChapter = nextChapter?.let { wenku8Api.id.child(Identifier::Chapter, it) }
        )
    }

    override suspend fun search(
        searchType: String,
        keyword: String,
        cursor: String?
    ): Result<BookPageResult, WebRequestError> = searchLock.withLock {
        coroutineBinding {
            val previous = lastSearch
            if (previous != null) {
                delay((5.seconds - previous.elapsedNow()).coerceAtLeast(Duration.ZERO))
            }
            val encodedKeyword = URLEncoder.encode(keyword, "gb2312")
            val page = cursor?.toInt() ?: 1
            lastSearch = TimeSource.Monotonic.markNow()
            val document = wenku8Api.getWithWenku8Cookie(
                url("modules/article/search.php?searchtype=$searchType&searchkey=$encodedKeyword&page=$page")
            ).mapAsWebRequestError("搜索失败", "无法请求搜索结果").bind()

            if (document.text().contains("两次搜索的间隔时间不得少于 5 秒")) {
                Err(WebRequestError("搜索过于频繁", "请稍后重试。")).bind()
            }

            val directBook = document.redirectedBookId()
            if (directBook != null && cursor == null) {
                return@coroutineBinding BookPageResult.OpenBook(wenku8Api.id.child(Identifier::Book, directBook))
            }

            val result = wenku8Api.parseBookPage(document, "#content > table > tbody > tr > td > div", page)
            result.books.forEach { book -> wenku8Api.cache.cache(book.id.id.hashCode(), book.information) }
            result
        }
    }
}
