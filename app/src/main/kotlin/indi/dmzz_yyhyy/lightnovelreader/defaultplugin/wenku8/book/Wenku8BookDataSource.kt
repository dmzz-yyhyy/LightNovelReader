package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.flow.Flow

interface Wenku8BookDataSource {
    suspend fun getBookInformation(id: String): Result<BookInformation, WebRequestError>
    suspend fun getBookVolumes(id: String): Result<BookVolumes, WebRequestError>
    suspend fun getChapterContent(chapterId: String, bookId: String): Result<ChapterContent, WebRequestError>
    fun search(searchType: String, keyword: String): Flow<SearchResult>
}