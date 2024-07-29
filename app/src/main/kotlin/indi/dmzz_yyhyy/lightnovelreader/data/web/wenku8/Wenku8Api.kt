package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8


import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.utils.update
import indi.dmzz_yyhyy.lightnovelreader.utils.wenku8.wenku8Cookie
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jsoup.Jsoup

object Wenku8Api: WebBookDataSource {
    private val BOOK_INFORMATION_URL = update("eNpb85aBtYTBNKOkpKDYSl-_vLxcrzw1L7vUQi85Wb88sUA_sagkMzknFUZn5qXl6xVkFNhnptgCAJkrFgQ").toString()
    private val BOOK_VOLUMES_URL = update("eNpb85aBtYTBOKOkpKDYSl-_vLxcrzw1L7vUQi85Wb88sUA_sagkMzknVb8oNTElKT8_W68go8A-MTPFFgBq4hUa").toString()
    private val CHAPTER_CONTENT_URL = update("eNpb85aBtYTBLKOkpKDYSl-_vLxcrzw1L7vUQi85Wb88sUA_sagkMzknVb8oNTElOSOxoCS1SK8go8A-MTPFFgCu1BZZ").toString()
    private val DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override suspend fun getBookInformation(id: Int): BookInformation {
        val soup = Jsoup.connect(BOOK_INFORMATION_URL +id).get()
        return BookInformation(
            id,
            soup.select("wml > card")[0].attr("title").split("(")[0],
            soup.select("img")[0].attr("src"),
            soup.select("anchor")[0].text(),
            soup.select("card")[0].toString()
                .split("[作品简介]<br>\n")[1]
                .split("<br></p>")[0].replace("<br>", ""),
            soup.select("a")[10].text(),
            soup.select("card")[0].toString()
                .split("字数:")[1]
                .split("字<br>")[0].toInt(),
            LocalDate.parse("20"+soup.select("card")[0].toString()
                .split("更新:")[1]
                .split("<br>")[0],
                DATA_TIME_FORMATTER
            ).atStartOfDay(),
                soup.select("card")[0].toString()
                    .split("状态:")[1]
                    .split("<br>")[0] != "连载中"
        )
    }

    override suspend fun getBookVolumes(id: Int): BookVolumes {
        val soup = Jsoup.connect(BOOK_VOLUMES_URL +id).get()
        val totalPage = soup.text().split("[1/")[1].split("]")[0].toInt()
        val elements: MutableList<String> = mutableListOf()
        for (volume in 1..totalPage) {
            elements += Jsoup.connect("$BOOK_VOLUMES_URL$id&page=$volume").get().toString()
                .split("作者").drop(1).joinToString()
                .split("\n", limit = 2)[1]
                .split("到第")[0]
                .replace("<br>", "")
                .replace("      ", "")
                .replace(", ", "")
                .replace("</a>", "")
                .replace("〖", "")
                .split("\n")
                .filter { it.isNotEmpty() }
        }
        val volumes: MutableList<Volume> = mutableListOf()
        var title = ""
        val chapters: MutableList<ChapterInformation> = mutableListOf()
        var index = 0
        for (element in elements) {
            if (element.endsWith("〗")) {
                if (index != 0)
                    volumes.add(Volume(id*100000+index, title, chapters.toList()))
                title = element.replace("〗", "")
                chapters.clear()
                index++
                continue
            }
            chapters.add(
                ChapterInformation(
                    element
                        .split("cid=")[1]
                        .split("\">")[0]
                        .toInt(),
                    element.split(">")[1]
                )
            )
        }
        volumes.add(Volume(id*100000+index, title, chapters.toList()))
        return BookVolumes(volumes)
    }

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent {
        val pageRegex = Regex("[0-9]/(.*)]<input")
        val contentRegex = Regex("</anchor><b.*>([\\s\\S]*)<br.*>\n*?.*?<input name=\"page\" format=\".*N\"")
        val soup = Jsoup.connect("$CHAPTER_CONTENT_URL${bookId}&cid=${chapterId}").get()
        var title: String
        var content = ""
        val lastChapter = soup.select("a[title=\"链接\"]").toList()
            .filter { it.text().equals("上章") }.let {
                if (it.isEmpty())
                    -1
                else it[0].attr("href")
                    .split("=").last().toInt()
            }
        val nextChapter = soup.select("a[title=\"链接\"]").toList()
            .filter { it.text().equals("下章") }.let {
                if (it.isEmpty())
                    -1
                else it[0].attr("href")
                    .split("=").last().toInt()
            }
        soup.let { document ->
            title = document.selectFirst("card")?.attr("title") ?: ""
            val page = pageRegex.find(document.toString())?.let {
                it.groups[1]?.value?.toInt() ?: 0
            } ?: 0
            content = (1..page).map { pageIndex ->
                contentRegex.find(Jsoup.connect("$CHAPTER_CONTENT_URL${bookId}&cid=${chapterId}&page=$pageIndex").get().toString())
                    ?.let { result ->
                        (result.groups[1]?.value ?: "")
                            .replace("      ", "")
                            .replace("&nbsp;", " ")
                            .replace("<br><br>", "\n")
                            .let {
                                if (it.startsWith("\n"))
                                    it.replaceFirst("\n", "")
                                else it
                            }.replace("<br>", "\n")
                    }
            }.joinToString("")
        }
        if (content.contains("本章节是纯图片内容，请访问文库电脑版网页浏览！"))
            content = getImages(bookId, chapterId)
        return ChapterContent(chapterId, title, content, lastChapter, nextChapter)
    }

    override suspend fun getExplorationPageMap(): Map<String, ExplorationPageDataSource> =
        mapOf(
            Pair("首页", Wenku8HomeExplorationPage),
            Pair("全部", Wenku8AllExplorationPage),
            Pair("分类", Wenku8TagsExplorationPage)
        )

    override suspend fun getExplorationPageTitleList(): List<String> = listOf("首页", "全部", "分类")

    private fun getImages(bookId: Int, chapterId: Int): String {
        return Jsoup
            .connect("https://www.wenku8.net/novel/${bookId/1000}/${bookId}/${chapterId}.htm")
            .wenku8Cookie()
            .get()
            .select("#content > div > a > img")
            .joinToString("\n") { "[image]${it.attr("src")}[/image]" }
    }

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        val searchResult = MutableStateFlow(emptyList<BookInformation>())
        val encodedKeyword = URLEncoder.encode(keyword, "gb2312")
        Jsoup
            .connect("https://www.wenku8.net/modules/article/search.php?searchtype=$searchType&searchkey=${encodedKeyword}")
            .wenku8Cookie()
            .get()
            .selectFirst("#pagelink > a.last")?.text()?.toInt()?.let { maxPage ->
                (0..<maxPage).map{ index ->
                    searchResult.update {
                        it + Jsoup
                            .connect("https://www.wenku8.net/modules/article/search.php?searchtype=$searchType&searchkey=${encodedKeyword}&page=$index")
                            .wenku8Cookie()
                            .get()
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
                }
            }
        return searchResult
    }
}