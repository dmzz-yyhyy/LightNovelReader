package indi.dmzz_yyhyy.lightnovelreader.data.web


import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.jsoup.Jsoup

object Wenku8Api: WebBookDataSource {
    private const val BOOK_INFORMATION_URL = "https://www.wenku8.net/wap/article/articleinfo.php?id="
    private const val BOOK_VOLUMES_URL = "https://www.wenku8.net/wap/article/readbook.php?aid="
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
}