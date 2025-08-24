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
            coverUrl = searchResult.imageUrl,
            author = searchResult.author,
            description = searchResult.description,
            tags = emptyList(), // ExtensionSearchResult doesn't have tags anymore
            publishingHouse = "",
            wordCount = 0, // ExtensionSearchResult doesn't have wordCount anymore
            lastUpdated = LocalDateTime.now(),
            isComplete = false // ExtensionSearchResult doesn't have isComplete anymore
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
            coverUrl = book.imageUrl,
            author = book.author,
            description = book.description,
            tags = book.genres, // Use genres instead of tags
            publishingHouse = "",
            wordCount = 0, // ExtensionBook doesn't have wordCount anymore
            lastUpdated = LocalDateTime.now(),
            isComplete = book.status == "Complete" // Use status to determine completion
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
