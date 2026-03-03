package indi.dmzz_yyhyy.lightnovelreader.data.web

import android.content.Context
import android.net.Uri
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.utils.RequestMarge
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.web.WebBookDataSource

class MargeWebBookDataSource(
    val webBookDataSource: WebBookDataSource,
    val requestMarge: RequestMarge = RequestMarge()
): WebBookDataSource {
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

    override suspend fun getBookInformation(id: String) = requestMarge.margeRequest(id.hashCode()) {
        webBookDataSource.getBookInformation(id)
    }

    override suspend fun getBookVolumes(id: String) = requestMarge.margeRequest(id.hashCode()) {
        webBookDataSource.getBookVolumes(id)
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String) = requestMarge.margeRequest((chapterId + bookId).hashCode()) {
        webBookDataSource.getChapterContent(chapterId, bookId)
    }
}