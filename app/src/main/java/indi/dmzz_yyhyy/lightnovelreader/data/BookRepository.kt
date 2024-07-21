package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class BookRepository @Inject constructor(
    private val webBookDataSource: WebBookDataSource,
    private val localBookDataSource: LocalBookDataSource
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

     suspend fun getBookInformation(id: Int): Flow<BookInformation> {
        val bookInformation: MutableStateFlow<BookInformation> =
            MutableStateFlow(localBookDataSource.getBookInformation(id) ?: BookInformation.empty())
        coroutineScope.launch {
            webBookDataSource.getBookInformation(id)?.let { information ->
                localBookDataSource.updateBookInformation(information)
                localBookDataSource.getBookInformation(id)?.let { newInfo ->
                    bookInformation.update {
                        newInfo
                    }
                }
            }
        }
        return bookInformation
    }

    suspend fun getBookVolumes(id: Int): Flow<BookVolumes> {
        val bookVolumes: MutableStateFlow<BookVolumes> =
            MutableStateFlow(localBookDataSource.getBookVolumes(id) ?: BookVolumes.empty())
        coroutineScope.launch {
            webBookDataSource.getBookVolumes(id)?.let { information ->
                localBookDataSource.updateBookVolumes(id, information)
                localBookDataSource.getBookVolumes(id)?.let { newBookVolumes ->
                    bookVolumes.update {
                        newBookVolumes
                    }
                }
            }
        }
        return bookVolumes
    }

    suspend fun getChapterContent(chapterId: Int, bookId: Int): Flow<ChapterContent> {
        val chapterContent: MutableStateFlow<ChapterContent> =
            MutableStateFlow(localBookDataSource.getChapterContent(chapterId) ?: ChapterContent.empty())
        coroutineScope.launch {
            webBookDataSource.getChapterContent(bookId, chapterId)?.let { content ->
                localBookDataSource.updateChapterContent(content)
                localBookDataSource.getChapterContent(chapterId)?.let { newContent ->
                    chapterContent.update {
                        newContent
                    }
                }
            }
        }
        return chapterContent
    }
}