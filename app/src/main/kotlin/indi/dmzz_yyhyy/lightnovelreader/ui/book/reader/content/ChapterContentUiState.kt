package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.runtime.Stable
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData

@Stable
class ChapterContentUiState(
    val id: String,
    val title: String,
    val content: List<AbstractContentComponentData>,
    val prevChapter: String?,
    val nextChapter: String?
) {
    fun hasPrevChapter(volume: BookVolumes? = null): Boolean = previousId(volume) != null

    fun hasNextChapter(volume: BookVolumes? = null): Boolean = nextId(volume) != null

    fun previousId(volume: BookVolumes?): String? =
        prevChapter?.takeIf { it.isNotBlank() && it != id } ?: adjacentId(volume, -1)

    fun nextId(volume: BookVolumes?): String? =
        nextChapter?.takeIf { it.isNotBlank() && it != id } ?: adjacentId(volume, 1)

    private fun adjacentId(volume: BookVolumes?, direction: Int): String? {
        val chapters = volume?.allChapters ?: return null
        val index = chapters.indexOfFirst { it.id == id }
        return if (index >= 0) chapters.getOrNull(index + direction)?.id else null
    }
}
