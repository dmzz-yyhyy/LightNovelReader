package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.runtime.Stable
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.identifier.Identifier

@Stable
class ChapterContentUiState(
    val id: Identifier.Chapter,
    val title: String,
    val content: List<AbstractContentComponentData>,
    val prevChapter: Identifier.Chapter?,
    val nextChapter: Identifier.Chapter?
) {
    fun hasPrevChapter(volume: BookVolumes? = null): Boolean = previousId(volume) != null

    fun hasNextChapter(volume: BookVolumes? = null): Boolean = nextId(volume) != null

    fun previousId(volume: BookVolumes?): Identifier.Chapter? =
        prevChapter?.takeIf { it != id } ?: adjacentId(volume, -1)

    fun nextId(volume: BookVolumes?): Identifier.Chapter? =
        nextChapter?.takeIf { it != id } ?: adjacentId(volume, 1)

    private fun adjacentId(volume: BookVolumes?, direction: Int): Identifier.Chapter? {
        val chapters = volume?.allChapters ?: return null
        val index = chapters.indexOfFirst { it.id == id }
        return if (index >= 0) chapters.getOrNull(index + direction)?.id else null
    }
}
