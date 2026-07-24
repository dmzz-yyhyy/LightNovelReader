package indi.dmzz_yyhyy.lightnovelreader.data.web.proxy

import android.content.Context
import android.net.Uri
import androidx.navigation.NavController
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.web.WebBookDataSource

interface ProxyWebBookDataSource: PriorityWebBookDataSource {
    override val origin: WebBookDataSource get() = proxiedWebBookDataSource.origin
    val proxiedWebBookDataSource: ProxyWebBookDataSource
    override val id get() = origin.id
    override val permits get() = origin.permits
    override fun onLoad() = origin.onLoad()
    override val imageHeader get() = origin.imageHeader
    override fun progressBookTagClick(
        tag: String,
        navController: NavController
    ) = origin.progressBookTagClick(tag, navController)
    override suspend fun isOffLine() = origin.isOffLine()
    override val offLine get() = origin.offLine
    override val isOffLineFlow get() = origin.isOffLineFlow
    override val searchProvider get() = origin.searchProvider
    override val explorePageProvider get() = origin.explorePageProvider
    override suspend fun getCoverUriInVolume(
        bookId: String,
        volume: Volume,
        volumeChapterContentMap: MutableMap<String, ChapterContent>,
        context: Context
    ): Uri? = origin.getCoverUriInVolume(bookId, volume, volumeChapterContentMap, context)
}