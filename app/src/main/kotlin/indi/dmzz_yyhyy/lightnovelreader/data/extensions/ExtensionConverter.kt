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
import org.jsoup.Jsoup
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

    fun generateBookId(extensionId: String, bookId: String): Int {
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
        // Clean HTML content as a fallback if the extension didn't do it
        val cleanedContent = cleanHtmlContent(chapter.content)
        
        return MutableChapterContent(
            id = chapterId,
            title = chapter.title,
            content = cleanedContent,
            lastChapter = -1,
            nextChapter = -1
        )
    }
    
    /**
     * Clean HTML content from chapter text.
     * This is a fallback mechanism in case extensions don't properly use the unhtml library.
     */
    private fun cleanHtmlContent(content: String): String {
        if (content.isBlank()) return content
        
        // Check if content contains HTML tags
        val hasHtmlTags = content.contains("<") && content.contains(">")
        
        return if (hasHtmlTags) {
            try {
                // Use Jsoup to parse and extract text content
                val doc = Jsoup.parse(content)
                val cleanText = doc.text()
                
                // If the cleaned text is significantly shorter, it might have been mostly HTML
                // In that case, preserve some structure by converting specific tags
                if (cleanText.length < content.length * 0.3) {
                    // Convert some common tags to preserve structure
                    content
                        .replace("<br>", "\n")
                        .replace("<br/>", "\n") 
                        .replace("<br />", "\n")
                        .replace("</p>", "\n\n")
                        .replace("<p>", "")
                        .replace("</div>", "\n")
                        .replace("<div>", "")
                        .replace(Regex("<[^>]+>"), "") // Remove remaining HTML tags
                        .replace(Regex("\\s+"), " ") // Collapse whitespace
                        .trim()
                } else {
                    cleanText
                }
            } catch (e: Exception) {
                println("ExtensionConverter: Error cleaning HTML content: ${e.message}")
                // Fallback: simple HTML tag removal
                content.replace(Regex("<[^>]+>"), "").trim()
            }
        } else {
            // No HTML tags detected, return as-is
            content
        }
    }
}
