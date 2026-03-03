package indi.dmzz_yyhyy.lightnovelreader.data.web

import android.content.Context
import android.net.Uri
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.utils.AdvancedPrioritySemaphore
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdvancedPrioritySemaphoreWebBookDataSource(
    val webBookDataSource: WebBookDataSource,
    val advancedPrioritySemaphore: AdvancedPrioritySemaphore,
    val priority: Int
): WebBookDataSource {
    override val id = webBookDataSource.id
    override val permits get() = webBookDataSource.permits
    override val cache get() = webBookDataSource.cache

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

    override suspend fun getBookInformation(id: String): BookInformation = withContext(Dispatchers.IO) {
        try {
            advancedPrioritySemaphore.acquire(priority)
            return@withContext webBookDataSource.getBookInformation(id).copy()
        } finally {
            advancedPrioritySemaphore.release()
        }
    }

    override suspend fun getBookVolumes(id: String): BookVolumes = withContext(Dispatchers.IO) {
        try {
            advancedPrioritySemaphore.acquire(priority)
            return@withContext webBookDataSource.getBookVolumes(id).copy()
        } finally {
            advancedPrioritySemaphore.release()
        }
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent = withContext(Dispatchers.IO) {
            try {
                advancedPrioritySemaphore.acquire(priority)
                return@withContext webBookDataSource.getChapterContent(chapterId, bookId).copy()
            } finally {
                advancedPrioritySemaphore.release()
            }
        }
}