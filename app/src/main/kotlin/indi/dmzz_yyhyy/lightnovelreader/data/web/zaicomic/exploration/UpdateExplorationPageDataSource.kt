package indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.exploration

import com.google.gson.reflect.TypeToken
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationBooksRow
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationDisplayBook
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.ZaiComic
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.ZaiComic.HOST
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.DataContent
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.UpdatePageItem
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionGetJsonText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

object UpdateExplorationPageDataSource : ExplorationPageDataSource {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lock = false
    private val explorationBooksRows: MutableStateFlow<List<ExplorationBooksRow>> = MutableStateFlow(emptyList())
    private val explorationPage = ExplorationPage("更新", explorationBooksRows)

    override val title = "更新"

    override fun getExplorationPage(): ExplorationPage {
        if (lock) return explorationPage
        lock = true
        scope.launch {
            explorationBooksRows.update { explorationBooksRowList ->
                explorationBooksRowList + ExplorationBooksRow(
                    title = "全部漫画",
                    bookList = getUpdateBooks(100)
                )
            }
        }
        scope.launch {
            explorationBooksRows.update { explorationBooksRowList ->
                explorationBooksRowList + ExplorationBooksRow(
                    title = "原创漫画",
                    bookList = getUpdateBooks(1)
                )
            }
        }
        scope.launch {
            explorationBooksRows.update { explorationBooksRowList ->
                explorationBooksRowList + ExplorationBooksRow(
                    title = "译制漫画",
                    bookList = getUpdateBooks(0)
                )
            }
        }
        return explorationPage
    }

    private suspend fun getUpdateBooks(channel: Int) = Jsoup
        .connect(HOST + "/app/v1/comic/update/list/$channel/1?channel=android&timestamp=${(System.currentTimeMillis() / 1000)}")
        .autoReconnectionGetJsonText()
        .let {
            ZaiComic.gson.fromJson<DataContent<List<UpdatePageItem>>>(
                it,
                object : TypeToken<DataContent<List<UpdatePageItem>>>() {}.type
            )
        }
        .data
        .map {
            ExplorationDisplayBook(it.id, it.title, "", it.cover)
        }
}
