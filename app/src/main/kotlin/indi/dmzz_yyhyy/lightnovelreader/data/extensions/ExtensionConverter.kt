package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.MutableBookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.MutableChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionConverter @Inject constructor() {

    fun convertSearchResultToBookInfoEntity(
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

    fun convertExtensionBookToBookInfoEntity(
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

    fun convertExtensionChapterToChapterInfoEntity(
        chapter: ExtensionChapter,
        bookId: Int
    ): ChapterInformationEntity {
        return ChapterInformationEntity(
            id = chapter.id.hashCode(),
            title = chapter.title
        )
    }

    fun convertExtensionChapterToChapterContentEntity(
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

    // Interface-returning methods for WebBookDataSource compatibility
    fun convertSearchResultToBookInfo(
        searchResult: ExtensionSearchResult,
        extensionId: String
    ): BookInformation {
        return MutableBookInformation(
            id = generateBookId(extensionId, searchResult.id),
            title = searchResult.title,
            subtitle = "",
            coverUrl = searchResult.imageUrl,
            author = searchResult.author,
            description = searchResult.description,
            tags = emptyList(),
            publishingHouse = "",
            wordCount = 0,
            lastUpdated = LocalDateTime.now(),
            isComplete = false
        )
    }

    fun convertExtensionBookToBookInfo(
        book: ExtensionBook,
        extensionId: String
    ): BookInformation {
        return MutableBookInformation(
            id = generateBookId(extensionId, book.id),
            title = book.title,
            subtitle = "",
            coverUrl = book.imageUrl,
            author = book.author,
            description = book.description,
            tags = book.genres,
            publishingHouse = "",
            wordCount = 0,
            lastUpdated = LocalDateTime.now(),
            isComplete = book.status == "Complete"
        )
    }

    fun convertExtensionChapterToChapterContent(
        chapter: ExtensionChapter,
        chapterId: Int
    ): ChapterContent {
        return MutableChapterContent(
            id = chapterId,
            title = chapter.title,
            content = chapter.content,
            lastChapter = -1,
            nextChapter = -1
        )
    }
}
