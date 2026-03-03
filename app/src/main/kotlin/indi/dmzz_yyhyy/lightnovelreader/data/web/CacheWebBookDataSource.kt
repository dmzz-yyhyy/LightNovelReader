package indi.dmzz_yyhyy.lightnovelreader.data.web

import android.content.Context
import android.net.Uri
import androidx.navigation.NavController
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource

class CacheWebBookDataSource(
    override val cache: Cache,
    val webBookDataSource: WebBookDataSource
): WebBookDataSource {
    private inline fun <reified T: CanBeEmpty> ifCache(id: String, block: () -> T): T {
        val cacheData = cache.getCache<T>(id.hashCode())
        if (cacheData == null) {
            val data = block.invoke()
            if (data.isEmpty()) return data
            cache.cache(id.hashCode(), data)
            return data
        }
        return cacheData
    }

    override val id = webBookDataSource.id
    override val permits get() = webBookDataSource.permits
    override fun onLoad() = webBookDataSource.onLoad()
    override val imageHeader get() = webBookDataSource.imageHeader
    override fun progressBookTagClick(
        tag: String,
        navController: NavController
    ) = webBookDataSource.progressBookTagClick(tag, navController)
    override suspend fun getCoverUriInVolume(
        bookId: String,
        volume: Volume,
        volumeChapterContentMap: MutableMap<String, ChapterContent>,
        context: Context
    ): Uri? = webBookDataSource.getCoverUriInVolume(bookId, volume, volumeChapterContentMap, context)
    override suspend fun isOffLine() = webBookDataSource.isOffLine()
    override val offLine get() = webBookDataSource.offLine
    override val isOffLineFlow get() = webBookDataSource.isOffLineFlow
    override val searchProvider get() = webBookDataSource.searchProvider
    override val explorePageProvider get() = webBookDataSource.explorePageProvider

    override suspend fun getBookInformation(id: String) = ifCache(id) {
        webBookDataSource.getBookInformation(id)
    }

    override suspend fun getBookVolumes(id: String) = ifCache(id) {
        webBookDataSource.getBookVolumes(id)
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String) = ifCache(chapterId + bookId) {
        webBookDataSource.getChapterContent(chapterId, bookId)
    }
}