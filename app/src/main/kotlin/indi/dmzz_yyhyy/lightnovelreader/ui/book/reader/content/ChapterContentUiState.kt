package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.runtime.Stable
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponent

@Stable
class ChapterContentUiState(
    val id: String,
    val title: String,
    val content: List<AbstractContentComponent<*>>,
    val prevChapter: String?,
    val nextChapter: String?
) {
    fun hasPrevChapter(): Boolean = prevChapter != null

    fun hasNextChapter(): Boolean = nextChapter != null
}