package indi.dmzz_yyhyy.lightnovelreader.data.web

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import indi.dmzz_yyhyy.lightnovelreader.utils.ofId
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

object EmptyWebDataSource : WebBookDataSource {
    override val id: Identifier.DataSource = "EmptyWebDataSource".ofId(Identifier::DataSource)
    override suspend fun isOffLine(): Boolean = true

    override val offLine: Boolean = true
    override val isOffLineFlow: StateFlow<Boolean> = MutableStateFlow(true)
    override val explorePageProvider: ExplorePageProvider =
        object : ExplorePageProvider.DefaultExplorePageProvider {
            override val explorePageIdList: List<String> = emptyList()
            override val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource> =
                emptyMap()
            override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> =
                emptyMap()
        }
    override val searchProvider: SearchProvider = object : SearchProvider {
        override val searchTypes: List<SearchType> = emptyList()

        override fun search(
            searchType: SearchType,
            keyword: String
        ): Flow<SearchResult> = flow {
        }

    }

    override suspend fun getBookInformation(id: Identifier.Book): Result<BookInformation, WebRequestError> =
        Err(WebRequestError("Empty data source", "The current data source is empty"))

    override suspend fun getBookVolumes(id: Identifier.Book): Result<BookVolumes, WebRequestError> =
        Err(WebRequestError("Empty data source", "The current data source is empty"))

    override suspend fun getChapterContent(
        chapterId: Identifier.Chapter,
        bookId: Identifier.Book
    ): Result<ChapterContent, WebRequestError> =
        Err(WebRequestError("Empty data source", "The current data source is empty"))
}
