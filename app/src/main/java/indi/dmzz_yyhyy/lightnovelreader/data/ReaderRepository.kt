package indi.dmzz_yyhyy.lightnovelreader.data

import dagger.hilt.android.scopes.ActivityRetainedScoped
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Information
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.room.dao.BookMetadataDao
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class ReaderRepository @Inject constructor(
    private val webDataSource: WebDataSource,
    private val bookMetadataDao: BookMetadataDao,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var _book: MutableStateFlow<Information> = MutableStateFlow(Information(0, "", "", "", "", listOf(), ""))
    private var _bookId: Int = 0
    private var _webVolumeList: MutableStateFlow<MutableList<Volume>> = MutableStateFlow(mutableListOf())
    private var _chapterContentId: MutableStateFlow<Int> = MutableStateFlow(0)
    private var _webChapterContent: MutableStateFlow<ChapterContent> = MutableStateFlow(ChapterContent("", ""))
    private var _lastReadChapterName: MutableStateFlow<String> = MutableStateFlow("暂未阅读")

    val book: StateFlow<Information> get() = _book
    val bookId: Int get() = _bookId
    val bookName: String get() = _book.value.name
    val bookCoverUrl: String get() = _book.value.coverUrl
    val bookIntroduction: String get() = _book.value.introduction
    val lastReadChapterName: StateFlow<String> get() = _lastReadChapterName
    val volumeList: StateFlow<List<Volume>> get() = _webVolumeList
    val chapterContentId: StateFlow<Int> get() = _chapterContentId
    val chapterContent: StateFlow<ChapterContent> get() = _webChapterContent


    suspend fun loadChapterList(bookId: Int) {
        _bookId = bookId
        val book = webDataSource.getInformation(bookId)
        if (book != null) {
            _book.value = book
        }
        val chapterList = webDataSource.getBookChapterList(bookId)
        if (chapterList != null) {
            _webVolumeList.value = chapterList.toMutableList()
        }
    }

    suspend fun loadChapterContent() {
        _webChapterContent.value = ChapterContent("", "")
        val bookContent = webDataSource.getBookContent(_bookId, _chapterContentId.value)
        if (bookContent != null) {
            _webChapterContent.value = bookContent
        }
    }


    fun setChapterContentId(id: Int) {
        // 设置最近阅读章节
        coroutineScope.launch {
            bookMetadataDao.setBookLastReadChapterId(_bookId, id)
        }
        _chapterContentId.value = id
    }

    fun loadLastReadChapter() {
        coroutineScope.launch {
            val chapterId: Int = if (bookMetadataDao.getBookLastReadChapterId(_bookId) == -1) {
                volumeList.value[0].chapters[0].id
            }
            else {
                bookMetadataDao.getBookLastReadChapterId(_bookId)
            }
            setChapterContentId(chapterId)
        }
    }
}