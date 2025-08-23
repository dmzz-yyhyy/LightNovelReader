package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionConverter @Inject constructor() {

    fun convertSearchResultToBookInfo(
        searchResult: ExtensionSearchResult,
        extensionId: String
    ): BookInformationEntity {
        return BookInformationEntity(
            id = generateBookId(extensionId, searchResult.id),
            title = searchResult.title,
            subtitle = "",
            coverUrl = searchResult.coverUrl,
            author = searchResult.author,
            description = searchResult.description,
            tags = searchResult.tags,
            publishingHouse = "",
            wordCount = searchResult.wordCount,
            lastUpdated = LocalDateTime.now(),
            isComplete = searchResult.isComplete
        )
    }

    fun convertExtensionBookToBookInfo(
        book: ExtensionBook,
        extensionId: String
    ): BookInformationEntity {
        return BookInformationEntity(
            id = generateBookId(extensionId, book.id),
            title = book.title,
            subtitle = "",
            coverUrl = book.coverUrl,
            author = book.author,
            description = book.description,
            tags = book.tags,
            publishingHouse = "",
            wordCount = book.wordCount,
            lastUpdated = LocalDateTime.now(),
            isComplete = book.isComplete
        )
    }

    fun convertExtensionChapterToChapterInfo(
        chapter: ExtensionChapter,
        bookId: Int
    ): ChapterInformationEntity {
        return ChapterInformationEntity(
            id = chapter.id.hashCode(),
            title = chapter.title
        )
    }

    fun convertExtensionChapterToChapterContent(
        chapter: ExtensionChapter,
        bookId: Int
    ): ChapterContentEntity {
        return ChapterContentEntity(
            id = chapter.id.hashCode(),
            title = chapter.title,
            content = chapter.content,
            lastChapter = -1,
            nextChapter = -1
        )
    }

    private fun generateBookId(extensionId: String, bookId: String): Int {
        return "$extensionId:$bookId".hashCode()
    }
}
