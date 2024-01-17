package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.`object`.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Information
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.SearchBooks
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.web.api.LightNovelReaderWebAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class WebDataSource @Inject constructor(
    private val lightNovelReaderWebAPI: LightNovelReaderWebAPI,
    private val ioDispatcher: CoroutineDispatcher
) {
    private var searchBookCache: SearchBooks? = null
    suspend fun getInformation(bookId: Int): Information? =
        if (searchBookCache != null && searchBookCache!!.searchBooks.any { it.id == bookId })
            searchBookCache!!.searchBooks.find { it.id == bookId }
        else
            withContext(ioDispatcher) {
                lightNovelReaderWebAPI.getBookInformation(bookId)
            }


    suspend fun getBookChapterList(bookId: Int): List<Volume>? =
        withContext(ioDispatcher) {
            lightNovelReaderWebAPI.getBookChapterList(bookId)
        }

    suspend fun getBookContent(bookId: Int, chapterId: Int): ChapterContent? =

        withContext(ioDispatcher) {
            lightNovelReaderWebAPI.getBookContent(bookId, chapterId)
        }

    suspend fun searchBook(searchType: String, keyword: String, page: Int): SearchBooks? =
        withContext(ioDispatcher) {
            searchBookCache = lightNovelReaderWebAPI.searchBook(searchType, keyword, page)
            searchBookCache
        }
}


