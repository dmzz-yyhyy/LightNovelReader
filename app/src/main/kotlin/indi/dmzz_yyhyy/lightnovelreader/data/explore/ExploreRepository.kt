package indi.dmzz_yyhyy.lightnovelreader.data.explore

import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.qualifyBookId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.WebDataSourceItem
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 探索/搜索仓库。
 *
 * 搜索、搜索建议会作用于所有已启用的数据源;
 * 探索页可以由界面选择任意已启用数据源。
 */
@Singleton
class ExploreRepository @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val webBookDataSourceManager: WebBookDataSourceManager,
) {
    /** 当前主数据源的搜索类型, 保留给仍在用单源语义的调用方 */
    val searchTypes get() = webBookDataSourceProvider.value.searchProvider.searchTypes
    val explorePageProvider get() = webBookDataSourceProvider.value.explorePageProvider

    val sourceItems: List<WebDataSourceItem> get() = webBookDataSourceManager.webDataSourceItems

    val primarySourceId: Identifier get() = webBookDataSourceProvider.value.id

    fun getEnabledSources(): List<ProxyWebBookDataSource> =
        webBookDataSourceProvider.getEnabled()

    fun getExplorePageProvider(sourceId: Identifier): ExplorePageProvider? =
        webBookDataSourceProvider.get(sourceId)?.explorePageProvider

    fun search(sourceId: Identifier, searchType: SearchType, keyword: String): Flow<SearchResult> {
        val source = webBookDataSourceProvider.get(sourceId) ?: return emptyFlow()
        return source.searchProvider.search(searchType, keyword).map { qualify(sourceId, it) }
    }

    /**
     * 在所有已启用数据源中搜索。每个数据源优先使用与 [preferredTypeId] 同名的搜索类型,
     * 不支持时退回该数据源自己的第一个搜索类型。
     */
    fun searchAll(keyword: String, preferredTypeId: String? = null): Flow<SearchResult> =
        channelFlow {
            val sources = getEnabledSources()
            var hasResult = false
            var hasError = false
            val jobs = sources.map { source ->
                launch {
                    val type = source.searchProvider.searchTypes
                        .firstOrNull { it.type == preferredTypeId }
                        ?: source.searchProvider.searchTypes.firstOrNull()
                        ?: return@launch
                    runCatching {
                        withTimeoutOrNull(60_000) {
                            source.searchProvider.search(type, keyword).collect { result ->
                            when (result) {
                                is SearchResult.SingleBook -> {
                                    hasResult = true
                                    send(
                                        SearchResult.MultipleBook(
                                            qualifyBookId(source.id.toString(), result.bookId)
                                        )
                                    )
                                }

                                is SearchResult.MultipleBook -> {
                                    hasResult = true
                                    send(
                                        SearchResult.MultipleBook(
                                            qualifyBookId(source.id.toString(), result.bookId)
                                        )
                                    )
                                }

                                is SearchResult.Error -> {
                                    hasError = true
                                    send(
                                        SearchResult.Error(
                                            "${source.id}: ${result.error.message}"
                                        )
                                    )
                                }

                                else -> Unit
                                }
                            }
                        }
                    }.onFailure {
                        hasError = true
                        send(SearchResult.Error("${source.id}: ${it.message}"))
                    }
                }
            }
            jobs.forEach { it.join() }
            if (hasResult || hasError) send(SearchResult.End())
            else send(SearchResult.Empty())
        }

    fun getSuggestions(history: List<String>, keyword: String): List<String> =
        getEnabledSources()
            .flatMap { source ->
                runCatching {
                    source.searchProvider.getSearchSuggestions(history, keyword)
                }.getOrDefault(emptyList())
            }
            .distinct()
            .take(10)

    private fun qualify(sourceId: Identifier, result: SearchResult): SearchResult =
        when (result) {
            is SearchResult.SingleBook ->
                SearchResult.SingleBook(qualifyBookId(sourceId.toString(), result.bookId))

            is SearchResult.MultipleBook ->
                SearchResult.MultipleBook(qualifyBookId(sourceId.toString(), result.bookId))

            else -> result
        }
}
