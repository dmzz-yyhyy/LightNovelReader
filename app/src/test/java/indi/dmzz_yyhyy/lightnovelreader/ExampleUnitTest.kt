package indi.dmzz_yyhyy.lightnovelreader

import org.jsoup.Jsoup


private const val BOOK_VOLUMES_URL = "https://www.wenku8.net/wap/article/readbook.php?aid="
fun main() {
    val id = 3353
    val soup = Jsoup.connect(BOOK_VOLUMES_URL +id).get()
    val totalPage = soup.text().split("[1/")[1].split("]")[0].toInt()
    val elements: MutableList<String> = mutableListOf()
    for (volume in 1..totalPage) {
        elements += Jsoup.connect("${BOOK_VOLUMES_URL}$id&page=$volume").get().toString()
            .split("作者").drop(1).joinToString()
            .split("\n", limit = 2)[1]
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
    println(volumes)
}