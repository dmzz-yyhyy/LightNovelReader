package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book

import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.utils.update
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.flow.Flow

class BookRequestDispatcher(
    val host: String,
    wenku8Api: Wenku8Api
): Wenku8BookDataSource {
    val source = listOf(
        Wenku8WebsiteDataSource(host, wenku8Api),
        Wenku8AppDataSource(wenku8Api, wenku8Api.ktorClient, update("eNpb85aBtYRBNqOkpKDYSl-_PDUvu9RCtyg1J7FSLze1vEIvvygdAO0UDQw").toString(), "1.24-pico-mochi") {
            "Dalvik/2.1.0 (Linux; U; Android 15; 23114RD76B Build/AQ3A.240912.001)"
        },
        Wenku8AppDataSource(wenku8Api, wenku8Api.ktorClient, update("eNpb85aBtYRBMaOkpMBKXz-xoECvPDUvu9RCLzk_Vz8xL6UoPzNFryCjAAAfiA5Q").toString(), "1.21") {
            "wenku8"
        },
    )

    private suspend fun <T: CanBeEmpty>rotation(default: T, block: suspend Wenku8BookDataSource.() -> T): T {
        for (dataSource in source) {
            val result = block.invoke(dataSource)
            if (result.isEmpty()) continue
            return result
        }
        return default
    }

    override suspend fun getBookInformation(id: String): BookInformation = rotation(BookInformation.empty(id)) {
        getBookInformation(id)
    }

    override suspend fun getBookVolumes(id: String): BookVolumes= rotation(BookVolumes.empty(id)) {
        getBookVolumes(id)
    }

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ): ChapterContent= rotation(ChapterContent.empty(chapterId)) {
        getChapterContent(chapterId, bookId)
    }

    override fun search(searchType: String, keyword: String): Flow<SearchResult> {
        return source.first().search(searchType, keyword)
    }
}