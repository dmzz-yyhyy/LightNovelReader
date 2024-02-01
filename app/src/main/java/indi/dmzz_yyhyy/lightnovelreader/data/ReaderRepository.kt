package indi.dmzz_yyhyy.lightnovelreader.data

import dagger.hilt.android.scopes.ActivityRetainedScoped
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Information
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.LightNovelReaderWebAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class ReaderRepository @Inject constructor(
    private val webDataSource: WebDataSource,
    private val lightNovelReaderWebAPI: LightNovelReaderWebAPI
) {
    private var _book: MutableStateFlow<Information> = MutableStateFlow(Information(0, "", "", "", "", listOf(), ""))
    private var _bookId: Int = 0
    private var _webVolumeList: MutableStateFlow<MutableList<Volume>> = MutableStateFlow(mutableListOf())
    private var _chapterContentId: MutableStateFlow<Int> = MutableStateFlow(0)
    private var _webChapterContent: MutableStateFlow<ChapterContent> = MutableStateFlow(ChapterContent("", ""))

    val book: StateFlow<Information> get() = _book
    val bookName: String get() = _book.value.name
    val bookCoverUrl: String get() = _book.value.coverUrl
    val bookIntroduction: String get() = _book.value.introduction
    val volumeList: StateFlow<List<Volume>> get() = _webVolumeList
    val chapterContentId: StateFlow<Int> get() = _chapterContentId
    val chapterContent: StateFlow<ChapterContent> get() = _webChapterContent

    // 更新数据的方法
    suspend fun loadChapterList(bookId: Int) {
        _bookId = bookId
        val book = webDataSource.getInformation(bookId)
        if (book != null) { _book.value = book }
        val chapterList = webDataSource.getBookChapterList(bookId)
        if (chapterList != null) { _webVolumeList.value = chapterList.toMutableList() }
    }
    suspend fun loadChapterContent(){
        _webChapterContent.value = ChapterContent("", "")
        val bookContent = webDataSource.getBookContent(_bookId, _chapterContentId.value)
        if (bookContent != null) { _webChapterContent.value = bookContent }
    }
    fun setChapterContentId(id: Int){
        _chapterContentId.value = id;
    }
}