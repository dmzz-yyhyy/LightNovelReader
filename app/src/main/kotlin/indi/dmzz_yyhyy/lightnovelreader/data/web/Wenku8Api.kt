package indi.dmzz_yyhyy.lightnovelreader.data.web


import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
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

    override suspend fun getChapterContent(bookId: Int, chapterId: Int): ChapterContent {
        val pageRegex = Regex("[0-9]/(.*)]<input")
        val titleRegex = Regex("报时.*<br>\n*(.*)<br")
        val contentRegex = Regex("</anchor><b.*>([\\s\\S]*)<br.*>\n*?.*?<input name=\"page\" format=\".*N\"")
        val soup = Jsoup.connect("$CHAPTER_CONTENT_URL${bookId}&cid=${chapterId}").get()
        var title = ""
        var content = ""
        soup.let { document ->
            titleRegex.find(document.toString())?.let {
                title = it.groups[1]?.value ?: ""
                title = title.replace("      ", "")
            }
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
        return ChapterContent(chapterId, title, content)
    }
}