package indi.dmzz_yyhyy.lightnovelreader.data.web


import WebDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import java.time.LocalDateTime
import org.jsoup.Jsoup

object Wenku8Api: WebDataSource {
    private const val BOOK_INFORMATION_URL = "https://www.wenku8.net/wap/article/articleinfo.php?id="
    private const val BOOK_VOLUMES_URL = "https://www.wenku8.net/wap/article/readbook.php?aid="
    override fun getBookInformation(id: Int): BookInformation {
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
            LocalDateTime.parse("20"+soup.select("card")[0].toString()
                .split("更新:")[1]
                .split("<br>")[0]),
                soup.select("card")[0].toString()
                    .split("状态:")[1]
                    .split("<br>")[0] != "连载中"
        )
    }

    override fun getBookVolumes(id: Int): BookVolumes {
        val soup = Jsoup.connect(BOOK_VOLUMES_URL+id).get()
        val totalPage = soup.text().split("[1/")[1].split("]")[0].toInt()
        println(totalPage)
        val elements: MutableList<String> = mutableListOf()
        for (volume in 1..totalPage+1) {
            elements += Jsoup.connect("$BOOK_VOLUMES_URL$id&page=$volume").get().toString()
                .split("〖").drop(1).joinToString()
                .split("到第")[0]
                .replace("<br>", "")
                .replace("      ", "")
                .replace(", ", "")
                .replace("</a>", "")
                .split("\n")
                .filter { it.isNotEmpty() }
        }
        val volumes: MutableList<Volume> = mutableListOf()
        var title = ""
        val chapters: MutableList<ChapterInformation> = mutableListOf()
        for ((index, element) in elements.withIndex()) {
            if (element.endsWith("〗")) {
                if (index != 0)
                    volumes.add(Volume(id*100+index, title, chapters.toList()))
                title = element.replace("〗", "")
                chapters.clear()
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
        println(volumes)
        return BookVolumes(volumes)
    }
}