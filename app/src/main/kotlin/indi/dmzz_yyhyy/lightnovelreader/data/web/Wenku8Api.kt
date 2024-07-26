package indi.dmzz_yyhyy.lightnovelreader.data.web


import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.utils.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.jsoup.Jsoup

object Wenku8Api: WebBookDataSource {
    private val BOOK_INFORMATION_URL = update("eNpb85aBtYTBNKOkpKDYSl-_vLxcrzw1L7vUQi85Wb88sUA_sagkMzknFUZn5qXl6xVkFNhnptgCAJkrFgQ").toString()
    private val BOOK_VOLUMES_URL = update("eNpb85aBtYTBOKOkpKDYSl-_vLxcrzw1L7vUQi85Wb88sUA_sagkMzknVb8oNTElKT8_W68go8A-MTPFFgBq4hUa").toString()
    private val CHAPTER_CONTENT_URL = update("eNpb85aBtYTBLKOkpKDYSl-_vLxcrzw1L7vUQi85Wb88sUA_sagkMzknVb8oNTElOSOxoCS1SK8go8A-MTPFFgCu1BZZ").toString()
    private val DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override suspend fun getBookInformation(id: Int): BookInformation {
        val soup = Jsoup.connect(BOOK_INFORMATION_URL+id).get()
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
                DATA_TIME_FORMATTER).atStartOfDay(),
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

    private fun getImages(bookId: Int, chapterId: Int): String {
        return Jsoup
            .connect("https://www.wenku8.net/novel/${bookId/1000}/${bookId}/${chapterId}.htm")
            .cookie("__51uvsct__1xtyjOqSZ75DRXC0", "1")
            .cookie(" __51vcke__1xtyjOqSZ75DRXC0", "5fd1e310-a176-5ee6-9144-ed977bccf14e")
            .cookie(" __51vuft__1xtyjOqSZ75DRXC0", "1691164424380")
            .cookie(
                " Hm_lvt_d72896ddbf8d27c750e3b365ea2fc902",
                "1695572903,1695666346,1696009387,1696966471"
            )
            .cookie(" Hm_lvt_acfbfe93830e0272a88e1cc73d4d6d0f", "1721130033,1721491724,1721570341")
            .cookie(" PHPSESSID", "4d1c461c284bfa784985dc462d92188a")
            .cookie(
                " jieqiUserInfo",
                "jieqiUserId%3D1125456%2CjieqiUserName%3Dyyhyy%2CjieqiUserGroup%3D3%2CjieqiUserVip%3D0%2CjieqiUserPassword%3Deb62861281462fd923fb99218735fef0%2CjieqiUserName_un%3Dyyhyy%2CjieqiUserHonor_un%3D%26%23x666E%3B%26%23x901A%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserGroupName_un%3D%26%23x666E%3B%26%23x901A%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserLogin%3D1721745838"
            )
            .cookie(" jieqiVisitInfo", "jieqiUserLogin%3D1721745838%2CjieqiUserId%3D1125456")
            .cookie(
                " cf_clearance",
                "rAZBJvDmKV_DyAMY3k8n0_tMWW_lEz3ycWfYtjfTPcg-1721745844-1.0.1.1-mqt8uqswt6KtEdjtDq5m_yrRpR0x6QUhux3.J5B_OQMCso87cCu2psOEn0KVC1xOzmJinWcs7eeZTAi1ruNA_w"
            )
            .cookie(" HMACCOUNT", "10DAC0CE2BEFA41A")
            .cookie(" _clck", "jvuxvk%7C2%7Cfnp%7C0%7C1658")
            .cookie(" Hm_lvt_d72896ddbf8d27c750e3b365ea2fc902", "")
            .cookie(" Hm_lpvt_d72896ddbf8d27c750e3b365ea2fc902", "1721745932")
            .cookie(" _clsk", "1xyg0vc%7C1721745933282%7C2%7C1%7Co.clarity.ms%2Fcollect")
            .get()
            .select("#content > div > a > img")
            .joinToString("\n") { "[image]${it.attr("src")}[/image]" }
    }
}