package indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.exploration.RankingsExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.exploration.RecommendExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.exploration.TypesExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.exploration.UpdateExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.ComicChapterComic
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.DataContent
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.DetailData
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.ListDataContent
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.SearchItem
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.ZaiComicData
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionGetJsonText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder

object ZaiComic : WebBookDataSource {
    class LimitedMap<K, V>(private val limitNum: Int): LinkedHashMap<K, V>() {
        private val keyList = mutableListOf<K>()

        override fun put(key: K, value: V): V? {
            keyList.add(key)
            if (keyList.size > limitNum) {
                this.remove(keyList[0])
                keyList.removeAt(0)
            }
            return super.put(key, value)
        }
    }
    const val HOST = "http://v4api.zaimanhua.com"
    val gson = Gson()
    private val comicDetailCacheMap: MutableMap<Int, DetailData> = LimitedMap(10)
    private val comicVolumesCacheMap: MutableMap<Int, BookVolumes> = LimitedMap(10)
    private var searchJob: Job? = null
    override var offLine = true

    override val isOffLineFlow = flow {
        while(true) {
            offLine = isOffLine()
            emit(offLine)
            delay(2500)
        }
    }

    private fun ZaiComicData<DataContent<DetailData>>.cacheDetailData(): ZaiComicData<DataContent<DetailData>> {
        if (comicDetailCacheMap.contains(id)) return this
        comicDetailCacheMap[id] = this.data.data
        return this
    }

    override suspend fun isOffLine(): Boolean =
        try {
            !Jsoup
                .connect(HOST)
                .ignoreContentType(true)
                .get()
                .outputSettings(
                    Document.OutputSettings()
                        .prettyPrint(false)
                        .syntax(Document.OutputSettings.Syntax.xml)
                )
                .body()
                .text()
                .contains("It Works")
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }

    override val id: Int
        get() = "ZaiComic".hashCode()

    private suspend fun getComicDetail(id: Int): DetailData? = if (comicDetailCacheMap.contains(id))
        comicDetailCacheMap[id]
    else
        Jsoup
            .connect(HOST +"/app/v1/comic/detail/$id?channel=android&timestamp=${(System.currentTimeMillis() / 1000)}")
            .autoReconnectionGetJsonText()
            .let {
                gson.fromJson<ZaiComicData<DataContent<DetailData>>>(it, object : TypeToken<ZaiComicData<DataContent<DetailData>>>() {}.type)
            }
            .cacheDetailData()
            .data
            .data

    override suspend fun getBookInformation(id: Int): BookInformation {
        val detailData = getComicDetail(id)
        return detailData?.toBookInformation() ?: BookInformation.empty()
    }

    override suspend fun getBookVolumes(id: Int): BookVolumes {
        val detailData = getComicDetail(id)
        return detailData?.toBookVolumes()
            ?.let {
                comicVolumesCacheMap[id] = it
                it
            } ?: BookVolumes.empty(id)
    }

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent {
        val volumes =
            if (comicVolumesCacheMap.contains(bookId))
                comicVolumesCacheMap[bookId] ?: return ChapterContent.empty()
            else
                getBookVolumes(bookId)
        val chapterIds = mutableListOf<Int>()
            .apply {
                volumes.volumes.forEach { volume ->
                    volume.chapters.map { it.id }.let(::addAll)
                }
            }
        val chapterIdIndex = chapterIds.indexOfFirst(chapterId::equals)
        if (chapterIdIndex == -1) return ChapterContent.empty()
        val lastChapterId =
            if (chapterIdIndex != 0) chapterIds[chapterIdIndex - 1]
            else -1
        val nextChapterId =
            if (chapterIdIndex != chapterIds.size - 1) chapterIds[chapterIdIndex + 1]
            else -1
        val chapterContent = Jsoup
            .connect(HOST +"/app/v1/comic/chapter/$bookId/$chapterId?channel=android&timestamp=${(System.currentTimeMillis() / 1000)}")
            .autoReconnectionGetJsonText()
            .let {
                gson.fromJson<ZaiComicData<DataContent<ComicChapterComic>>>(it, object : TypeToken<ZaiComicData<DataContent<ComicChapterComic>>>() {}.type)
            }
            .data
            .data
            .toChapterContent(lastChapterId, nextChapterId)
        return chapterContent
    }

    override val explorationPageDataSourceMap = mapOf(
        "探索" to RecommendExplorationPageDataSource,
        "更新" to UpdateExplorationPageDataSource,
        "分类" to TypesExplorationPageDataSource,
        "排行" to RankingsExplorationPageDataSource
    )

    override val explorationPageIdList: List<String> =
        listOf("探索", "更新", "分类", "排行")

    override val explorationExpandedPageDataSourceMap: Map<String, ExplorationExpandedPageDataSource> = mapOf()

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        val comicList = MutableStateFlow(listOf<BookInformation>())
        searchJob = CoroutineScope(Dispatchers.IO).launch {
            var page = 1
            while (true) {
                val ids = Jsoup
                    .connect(
                        HOST + "/app/v1/search/index?keyword=${
                            URLEncoder.encode(
                                keyword,
                                "utf-8"
                            )
                        }&page=$page&size=20&channel=android&timestamp=${(System.currentTimeMillis() / 1000)}"
                    )
                    .autoReconnectionGetJsonText()
                    .let {
                        gson.fromJson<ZaiComicData<ListDataContent<SearchItem>>>(
                            it,
                            object : TypeToken<ZaiComicData<ListDataContent<SearchItem>>>() {}.type
                        )
                    }
                    .data
                    .list
                    .let { searchItemList ->
                        if (searchItemList == null) {
                            comicList.update {
                                it + BookInformation.empty()
                            }
                            return@launch
                        }
                        searchItemList
                    }
                    .map { it.id }
                ids.forEach { id ->
                    delay(1)
                    comicList.update {
                        it + (getBookInformation(id))
                    }
                }
                page++
            }
        }
        return comicList
    }

    override val searchTypeMap: Map<String, String> =
        mapOf(
            "按漫画名称搜索" to "name"
        )

    override val searchTipMap: Map<String, String> =
        mapOf(
            "name" to "请输入漫画名称"
        )

    override val searchTypeIdList: List<String> = listOf("按漫画名称搜索")

    override fun stopAllSearch() {
        searchJob?.cancel()
    }
}