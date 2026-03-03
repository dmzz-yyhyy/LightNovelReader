package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book

import androidx.core.net.toUri
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.autoReconnectionGetWithWenku8Cookie
import indi.dmzz_yyhyy.lightnovelreader.utils.CxHttpInit
import indi.dmzz_yyhyy.lightnovelreader.utils.network.selectFirstXpath
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.image
import io.nightfish.lightnovelreader.api.content.builder.simpleText
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class Wenku8WebsiteDataSource: Wenku8BookDataSource {
    private val host get() = Wenku8Api.host
    private val titleRegex = Regex("(.*) ?[(（](.*)[)）] ?$")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val cache = Cache(
        timeout = 2 * 60 * 60 * 1000
    )

    private inline fun <reified T: CanBeEmpty> ifCache(id: String, block: () -> T): T {
        val cacheData = cache.getCache<T>(id.hashCode())
        if (cacheData == null) {
            val data = block.invoke()
            if (data.isEmpty()) return data
            cache.cache(id.hashCode(), data)
            return data
        }
        return cacheData
    }

    private fun url(string: String) = "$host/$string"

    override suspend fun getBookInformation(id: String): BookInformation = ifCache(id) {
        val soup = autoReconnectionGetWithWenku8Cookie(url("book/$id.htm")) ?: return@ifCache BookInformation.empty(id)
        if (soup.text().contains("因版权问题")) return@ifCache BookInformation.empty()
        val titleGroup = soup
            .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[1]/td/table/tbody/tr/td[1]/span/b")
            ?.text()
            ?.let { titleRegex.find(it)?.groups }
        return@ifCache MutableBookInformation(
            id = id,
            title = titleGroup
                ?.get(1)?.value
                ?: soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[1]/td/table/tbody/tr/td[1]/span/b")
                    ?.text()
                ?: return@ifCache BookInformation.empty(id),
            subtitle = titleGroup
                ?.get(2)
                ?.value
                ?: "",
            coverUrl = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[2]/tbody/tr/td[1]/img")
                ?.attr("src")
                ?.toUri()
                ?: return@ifCache BookInformation.empty(),
            author = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[2]")
                ?.text()
                ?.replace("小说作者：", "")
                ?: return@ifCache BookInformation.empty(),
            description = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[2]/tbody/tr/td[2]/span[6]")
                ?.text()
                ?: return@ifCache BookInformation.empty(),
            tags = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[2]/tbody/tr/td[2]/span[1]/b")
                ?.text()
                ?.replace("作品Tags：", "")
                ?.split(" ")
                ?: return@ifCache BookInformation.empty(),
            publishingHouse = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[1]")
                ?.text()
                ?.replace("文库分类：", "")
                ?: return@ifCache BookInformation.empty(),
            wordCount = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[5]")
                ?.text()
                ?.replace("全文长度：", "")
                ?.replace("字", "")
                ?.toIntOrNull()
                ?.let { WordCount(it) }
                ?: return@ifCache BookInformation.empty(),
            lastUpdated = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[4]")
                ?.text()
                ?.replace("最后更新：", "")
                ?.let { LocalDate.parse(it, dateTimeFormatter) }
                ?.atStartOfDay()
                ?: return@ifCache BookInformation.empty(),
            isComplete = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[3]")
                ?.text()
                ?.contains("已完结")
                ?: return@ifCache BookInformation.empty()
        )
    }

    override suspend fun getBookVolumes(id: String): BookVolumes = ifCache(id) {
        val soup = autoReconnectionGetWithWenku8Cookie(url("novel/${id.toInt() / 1000}/$id/index.htm")) ?: return@ifCache BookVolumes.empty(id)
        val trs = soup.selectXpath("/html/body/table/tbody/tr")
        val volumes = mutableListOf<Volume>()
        var volume: Volume? = null
        var chapters = mutableListOf<ChapterInformation>()
        trs.forEach { tr ->
            if (tr.selectFirst("td")?.attr("class") == "vcss") {
                volume?.let(volumes::add)
                val td = tr.selectFirst("td")
                val vId = td?.attr("vid") ?: return@ifCache BookVolumes.empty()
                val title = td.text().ifEmpty { return@ifCache BookVolumes.empty() }
                chapters = mutableListOf()
                volume = Volume(vId, title, chapters)
                return@forEach
            }
            for (td in tr.select("td > a")) {
                val id = td
                    .attr("href")
                    .split(".")
                    .firstOrNull()
                    ?: return@ifCache BookVolumes.empty()
                val title = td.text().ifEmpty { return@ifCache BookVolumes.empty() }
                chapters.add(ChapterInformation(id, title))
            }
        }
        volume?.let(volumes::add)
        return@ifCache BookVolumes(id, volumes)
    }

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ): ChapterContent = ifCache(chapterId + bookId) {
        val soup = autoReconnectionGetWithWenku8Cookie(url("novel/${bookId.toInt() / 1000}/$bookId/$chapterId.htm")) ?: return@ifCache ChapterContent.empty(chapterId)
        if (soup.text().contains("因版权问题")) return@ifCache ChapterContent.empty(chapterId)
        val content = soup.selectFirstXpath("//*[@id=\"content\"]") ?: return@ifCache ChapterContent.empty(chapterId)
        val jsonObject = ContentBuilder().apply {
            var text = ""
            for (node in content.childNodes()) {
                when (node) {
                    is TextNode -> text += node.nodeValue().replace(" ", "  ")
                    is Element if node.`is`("div.divimage") -> {
                        simpleText(text)
                        text = ""
                        node
                            .selectFirst("img")
                            ?.attr("src")
                            ?.toUri()
                            ?.let(::image)
                    }
                }
            }
            if (text.isNotEmpty()) {
                simpleText(text)
            }
        }.build()
        val lastChapter = soup.selectFirstXpath("//*[@id=\"foottext\"]/a[3]").let {
            it ?: return@let ""
            if (it.attr("href") == "index.htm" || it.attr("href").contains("article")) ""
            else it.attr("href").split(".").firstOrNull() ?: ""
        }
        val nextChapter = soup.selectFirstXpath("//*[@id=\"foottext\"]/a[4]").let {
            it ?: return@let ""
            if (it.attr("href") == "index.htm" || it.attr("href").contains("article")) ""
            else it.attr("href").split(".").firstOrNull() ?: ""
        }
        return@ifCache MutableChapterContent(
            id = chapterId,
            title = soup.selectFirstXpath("//*[@id=\"title\"]")?.text() ?: return@ifCache ChapterContent.empty(chapterId),
            content = jsonObject,
            lastChapter = lastChapter,
            nextChapter = nextChapter
        )
    }

    override fun search(searchType: String, keyword: String): Flow<SearchResult> = flow {
        val encodedKeyword = URLEncoder.encode(keyword, "gb2312")

        var targetPage = 1
        var presentPage = 1
        while (presentPage <= targetPage) {
            val soup =
                autoReconnectionGetWithWenku8Cookie(url("modules/article/search.php?searchtype=$searchType&searchkey=$encodedKeyword&page=$presentPage"))
            if (soup == null) {
                emit(SearchResult.Error("Failed to request the web page"))
                return@flow
            }
            if (soup.text().contains("错误原因：对不起，两次搜索的间隔时间不得少于 5 秒")) {
                delay(5.seconds)
                continue
            }
            val menu =
                soup.selectFirstXpath("//*[@id=\"content\"]/div[1]/div[4]/div/span[1]/fieldset/div/a")
            if (menu != null && menu.text().contains("小说目录")) {
                val id = menu.attr("href").split("/").getOrNull(3)
                if (id == null) {
                    emit(SearchResult.Error("Failed to prase single book id"))
                    return@flow
                }
                emit(SearchResult.SingleBook(id))
                return@flow
            }
            soup.baseUri()
            if (targetPage == 1) {
                val page = soup.selectFirstXpath("//*[@id=\"pagelink\"]/em")?.text()?.split("/")
                    ?.getOrNull(1)?.toIntOrNull()
                if (page == null) {
                    emit(SearchResult.Error("Failed to request the web page"))
                    return@flow
                }
                targetPage = page
            }

            val books =
                Wenku8Api.getBookInformationListFromBookCards(soup.selectXpath("//*[@id=\"content\"]/table/tbody/tr/td/div"))
            for (information in books) {
                emit(SearchResult.MultipleBook(information))
            }
            /*
            if (targetPage == 1 && books.isEmpty() && searchType == "articlename") {
                val soup = autoReconnectionGet("https://cn.bing.com/search?q=$keyword%20wenku8") {
                    header("cookie", "MUID=39DDB51E98846CC02118A3C099146D54; SRCHD=AF=NOFORM; SRCHUID=V=2&GUID=06DC4253E1934C3588AE679C541B4D66&dmnchg=1; MUIDB=39DDB51E98846CC02118A3C099146D54; _UR=QS=0&TQS=0&Pn=0; BFBUSR=BFBHP=0; _Rwho=u=d&ts=2026-01-18; ipv6=hit=1768753589444&t=4; SRCHUSR=DOB=20260118&DS=1&POEX=W; _U=1MEFKBimFaqLPpKync1TM_qNx6gWz2LR_O_Znah0DoyShq0U5LpTlayJgi5QESx71ZrMn32grCUlcaKMjJ1IFbkHWHJzsO2JqmsTWQtalhgbRGkZxDtk-mA3KBHOLuetN7QCeKcTCcH1O9vB918hyOCFKqmXxw-pivahpL0ZC0nvPZtjcWioQhdN7Xr78oBMkZzvhCzGja5L-tOP8HswL5AfYFrUGXXnNUzoVre34PNc; ANON=A=FA79CDF52987792A18096ECBFFFFFFFF; WLS=C=49a69887840347cf&N=%e3%82%86%e3%81%9b%e3%82%93; _HPVN=CS=eyJQbiI6eyJDbiI6MSwiU3QiOjAsIlFzIjowLCJQcm9kIjoiUCJ9LCJTYyI6eyJDbiI6MSwiU3QiOjAsIlFzIjowLCJQcm9kIjoiSCJ9LCJReiI6eyJDbiI6MSwiU3QiOjAsIlFzIjowLCJQcm9kIjoiVCJ9LCJBcCI6dHJ1ZSwiTXV0ZSI6dHJ1ZSwiTGFkIjoiMjAyNi0wMS0xOFQwMDowMDowMFoiLCJJb3RkIjowLCJHd2IiOjAsIlRucyI6MCwiRGZ0IjpudWxsLCJNdnMiOjAsIkZsdCI6MCwiSW1wIjo0LCJUb2JuIjowfQ==; _SS=SID=2BE575B9E11360290988635CE0C16130&R=281&RB=281&GB=0&RG=3850&RP=281&h5comp=0; _EDGE_S=SID=2BE575B9E11360290988635CE0C16130&mkt=zh-CN; MUIDB=39DDB51E98846CC02118A3C099146D54; USRLOC=HS=1&ELOC=LAT=23.345535278320312|LON=116.74060821533203|N=%E9%BE%99%E6%B9%96%E5%8C%BA%EF%BC%8C%E5%B9%BF%E4%B8%9C%E7%9C%81|ELT=4|; _C_ETH=1; _RwBf=r=0&ilt=2&ihpd=1&ispd=8&rc=281&rb=281&rg=3850&pc=281&mtu=0&rbb=0.0&clo=0&v=10&l=2026-01-18T08:00:00.0000000Z&lft=0001-01-01T00:00:00.0000000&aof=0&ard=0001-01-01T00:00:00.0000000&rwdbt=1733529679&rwflt=-62135539200&rwaul2=0&g=&o=0&p=BINGTRIAL5TO250P201808&c=MY00IA&t=3530&s=2022-11-09T15:48:05.6754429+00:00&ts=2026-01-18T16:28:49.1763563+00:00&rwred=0&wls=2&wlb=0&wle=0&ccp=2&cpt=0&lka=0&lkt=0&aad=0&TH=&cid=0&gb=2025w23_c&mta=0&e=6QdzFnT99RI1G0ggUxX8zvuQ0Gx-TEDNS3_bViVNUl68AiR3J9OAfbjsQI2S-0E0L1jsbyMyhMKEtctVg7OqyaZcPoaWlATXC0ff0gb3lRU; SRCHHPGUSR=SRCHLANG=zh-Hans&PREFCOL=1&BRW=XW&BRH=T&CW=2048&CH=1064&SCW=2033&SCH=3205&DPR=1.3&UTC=480&PV=10.0.0&HV=1768753748&HVE=CfDJ8HAK7eZCYw5BifHFeUHnkJEYtb6XQ30MiRcoJG1JcNn7kLNpgd42aNfC8Wrv1_ggHyeNiTQAzILDdx2oI2hwEF-XBlRWS5Ud_tquuK2u_c5RU3MBRtZVAEzCNGbqfqPh4lwI39CbVuF8Ta3v86sSl7rIUcw_tqbRWGpZxsER3DobfEekb1I3sO6yHY0JwxcMKg&BZA=0&PRVCW=2048&PRVCH=1064&B=0&EXLTT=8")
                    header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36")
                }
                soup?.forEach {  }
            }*/
            presentPage++
            delay(5.seconds)
        }
        emit(SearchResult.End())
    }
        .flowOn(Dispatchers.IO)

    init {
        CxHttpInit.init()
    }
}