package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.flow.Flow

class BookRequestDispatcher(
    val host: String,
    wenku8Api: Wenku8Api
): Wenku8BookDataSource {
    val source = listOf(
        Wenku8WebsiteDataSource(host, wenku8Api)
    )

    private suspend fun <T>rotation(block: suspend Wenku8BookDataSource.() -> Result<T, WebRequestError>): Result<T, WebRequestError> {
        var result: Result<T, WebRequestError> = Err(WebRequestError("No available data source", "There is no wenku8 data source that can be use"))
        for (dataSource in source) {
            result = block.invoke(dataSource)
            if (result.isErr) continue
            return result
        }
        return result
    }

    override suspend fun getBookInformation(id: String) = rotation {
        getBookInformation(id)
    }

    override suspend fun getBookVolumes(id: String) = rotation {
        getBookVolumes(id)
    }

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ) = rotation {
        getChapterContent(chapterId, bookId)
    }

    override fun search(searchType: String, keyword: String): Flow<SearchResult> {
        return source.first().search(searchType, keyword)
    }
}