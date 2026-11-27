package indi.dmzz_yyhyy.lightnovelreader.data.web

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.BookPageResult
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.explore.AbstractDefaultExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotFoundWebDataSource(override val id: Identifier.DataSource) : WebBookDataSource {
    override suspend fun isOffLine(): Boolean = true

    override val offLine: Boolean = true
    override val isOffLineFlow: StateFlow<Boolean> = MutableStateFlow(true)
    override val explorePageProvider: ExplorePageProvider =
        object : AbstractDefaultExplorePageProvider() {}
    override val searchProvider: SearchProvider = object : SearchProvider {
        override suspend fun loadPage(
            keyword: String,
            settings: OptionValues,
            filters: OptionValues,
            cursor: String?
        ): Result<BookPageResult, WebRequestError> = Ok(BookPageResult.Page(emptyList(), null))
    }

    override suspend fun getBookInformation(id: Identifier.Book): Result<BookInformation, WebRequestError> =
        Err(
            WebRequestError(
                "Data source not founded",
                "Did not found the current data source($id) from plugins. Please check your plugin settings"
            )
        )

    override suspend fun getBookVolumes(id: Identifier.Book): Result<BookVolumes, WebRequestError> =
        Err(
            WebRequestError(
                "Data source not founded",
                "Did not found the current data source($id) from plugins. Please check your plugin settings"
            )
        )

    override suspend fun getChapterContent(
        chapterId: Identifier.Chapter,
        bookId: Identifier.Book
    ): Result<ChapterContent, WebRequestError> =
        Err(
            WebRequestError(
                "Data source not founded",
                "Did not found the current data source($id) from plugins. Please check your plugin settings"
            )
        )
}
