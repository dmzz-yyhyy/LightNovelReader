package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import indi.dmzz_yyhyy.lightnovelreader.utils.network.selectFirstXpath
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.BookPageResult
import io.nightfish.lightnovelreader.api.web.PageBook
import org.jsoup.nodes.Document

internal fun Document.redirectedBookId(): String? {
    val link = selectFirstXpath("//*[@id=\"content\"]/div[1]/div[4]/div/span[1]/fieldset/div/a")
    return link?.takeIf { it.text().contains("小说目录") }?.attr("href")?.split("/")?.getOrNull(3)
}

internal fun Wenku8Api.parseBookPage(document: Document, selector: String, page: Int): BookPageResult.Page {
    val lastPage = document.selectFirst("#pagelink em")?.text()
        ?.substringAfter('/')?.trim()?.toIntOrNull()
    val books = getBookInformationListFromBookCards(document.select(selector)).mapNotNull { (bookId, result) ->
        bookId.takeIf { it.isNotBlank() }?.let { PageBook(id.child(Identifier::Book, it), result) }
    }
    return BookPageResult.Page(books, lastPage?.takeIf { page < it }?.let { (page + 1).toString() })
}
