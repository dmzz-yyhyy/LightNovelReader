package indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.exploration

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationBooksRow
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationDisplayBook
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.ZaiComic
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.ZaiComic.HOST
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.DataContent
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.TagTypeItem
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionGetJsonText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

object TypesExplorationPageDataSource : ExplorationPageDataSource {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lock = false
    private val explorationBooksRows: MutableStateFlow<List<ExplorationBooksRow>> = MutableStateFlow(emptyList())
    private val explorationPage = ExplorationPage("分类", explorationBooksRows)

    override val title = "分类"

    override fun getExplorationPage(): ExplorationPage {
        if (lock) return explorationPage
        lock = true
        scope.launch {
            getAllTypes().forEach { tagTypeItem ->
                explorationBooksRows.update {
                    it + ExplorationBooksRow(
                        tagTypeItem.title,
                        getTagBooks(tagTypeItem.tagId)
                    )
                }
            }
        }
        return explorationPage
    }

    private suspend fun getAllTypes(): List<TagTypeItem> = Jsoup
        .connect(HOST + "/app/v1/comic/filter/category?source=1&channel=android&timestamp=${(System.currentTimeMillis() / 1000)}")
        .autoReconnectionGetJsonText()
        .let {
            ZaiComic.gson.fromJson<DataContent<CateList<TagTypeItem>>>(
                it,
                object : TypeToken<DataContent<CateList<TagTypeItem>>>() {}.type
            )
        }
        .data
        .cateList

    private data class CateList<T> (@SerializedName("cateList") val cateList: List<T>)

    private suspend fun getTagBooks(tagId: Int) = Jsoup
        .connect(HOST + "/app/v1/comic/filter/list?tagId=$tagId&status=0&sortType=1&page=1&size=20&channel=android&timestamp=${(System.currentTimeMillis() / 1000)}")
        .autoReconnectionGetJsonText()
        .let {
            ZaiComic.gson.fromJson<DataContent<ComicList<DisplayTagBook>>>(
                it,
                object : TypeToken<DataContent<ComicList<DisplayTagBook>>>() {}.type
            )
        }
        .data
        .comicList
        .map { ExplorationDisplayBook(it.id, it.title, "", it.cover) }

    private data class ComicList<T> (@SerializedName("comicList") val comicList: List<T>)

    private data class DisplayTagBook(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val title: String,
        @SerializedName("cover")
        val cover: String
    )
}