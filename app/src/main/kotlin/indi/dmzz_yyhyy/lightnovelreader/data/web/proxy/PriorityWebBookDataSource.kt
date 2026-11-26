package indi.dmzz_yyhyy.lightnovelreader.data.web.proxy

import android.content.Context
import android.net.Uri
import androidx.navigation3.runtime.NavKey
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import kotlinx.coroutines.flow.StateFlow

interface PriorityWebBookDataSource {
    val origin: WebBookDataSource

    val permits: Int get() = 64

    val cache: Cache? get() = null

    val id: Identifier.DataSource

    fun onLoad() {}

    suspend fun isOffLine(): Boolean

    val offLine: Boolean

    val isOffLineFlow: StateFlow<Boolean>

    val searchProvider: SearchProvider

    val explorePageProvider: ExplorePageProvider

    val imageHeader: Map<String, String>

    fun progressBookTagClick(tag: String): NavKey?

    suspend fun getCoverUriInVolume(
        bookId: Identifier.Book,
        volume: Volume,
        volumeChapterContentMap: MutableMap<Identifier.Chapter, ChapterContent>,
        context: Context
    ): Uri?

    suspend fun getBookInformation(
        id: Identifier.Book,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): Result<BookInformation, WebRequestError>

    suspend fun getBookVolumes(
        id: Identifier.Book,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): Result<BookVolumes, WebRequestError>

    suspend fun getChapterContent(
        chapterId: Identifier.Chapter,
        bookId: Identifier.Book,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): Result<ChapterContent, WebRequestError>
}
