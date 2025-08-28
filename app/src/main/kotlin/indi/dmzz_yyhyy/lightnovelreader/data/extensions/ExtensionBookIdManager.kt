package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.ExtensionBookDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionBookEntity
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles encoding and decoding of book IDs that contain extension information
 */
@Singleton
class ExtensionBookIdManager @Inject constructor(
    private val extensionBookDao: ExtensionBookDao
) {

    companion object {
        private const val EXTENSION_PREFIX = "ext_"
        private const val SEPARATOR = "_"
        private const val HASH_MULTIPLIER = 31
    }

    /**
     * Generates a unique book ID that encodes extension information and stores the mapping
     */
    suspend fun generateAndStoreExtensionBookId(
        extensionId: String, 
        originalBookId: String,
        bookTitle: String = "",
        bookAuthor: String = "",
        bookDescription: String = "",
        bookImageUrl: String = ""
    ): Int {
        val hashedId = generateExtensionBookId(extensionId, originalBookId)
        
        // Check if this book mapping already exists
        val existingBook = extensionBookDao.getExtensionBook(extensionId.hashCode(), originalBookId)
        
        if (existingBook == null) {
            // Create new mapping in database
            val extensionBookEntity = ExtensionBookEntity(
                internalBookId = hashedId,
                extensionId = extensionId.hashCode(),
                originalBookId = originalBookId,
                title = bookTitle,
                author = bookAuthor,
                description = bookDescription,
                imageUrl = bookImageUrl,
                isInLibrary = false
            )
            
            try {
                extensionBookDao.insertExtensionBook(extensionBookEntity)
                println("ExtensionBookIdManager: Created mapping for book $hashedId -> $originalBookId in extension $extensionId")
            } catch (e: Exception) {
                println("ExtensionBookIdManager: Failed to store book mapping: ${e.message}")
                e.printStackTrace()
            }
        }
        
        return hashedId
    }

    /**
     * Generates a unique book ID that encodes extension information
     * Format: Hash of "ext_{extensionId}_{originalBookId}"
     */
    fun generateExtensionBookId(extensionId: String, originalBookId: String): Int {
        val combinedString = "${EXTENSION_PREFIX}${extensionId}${SEPARATOR}${originalBookId}"
        return combinedString.hashCode().let { hash ->
            // Ensure positive hash
            if (hash < 0) -hash else hash
        }
    }

    /**
     * Extracts extension information from a book ID using database lookup
     */
    suspend fun extractExtensionInfo(bookId: Int): ExtensionBookInfo? {
        return try {
            val extensionBook = extensionBookDao.getExtensionBookById(bookId)
            if (extensionBook != null) {
                ExtensionBookInfo(
                    extensionId = extensionBook.extensionId.toString(),
                    originalBookId = extensionBook.originalBookId
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("ExtensionBookIdManager: Error extracting extension info: ${e.message}")
            null
        }
    }

    /**
     * Checks if a book ID represents an extension book by looking up in database
     */
    suspend fun isExtensionBook(bookId: Int): Boolean {
        return try {
            extensionBookDao.getExtensionBookById(bookId) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Creates a mapping entry for tracking extension books
     */
    fun createExtensionBookMapping(
        bookId: Int,
        extensionId: String,
        originalBookId: String
    ): ExtensionBookMapping {
        return ExtensionBookMapping(
            internalBookId = bookId,
            extensionId = extensionId,
            originalBookId = originalBookId
        )
    }

    data class ExtensionBookInfo(
        val extensionId: String,
        val originalBookId: String
    )

    data class ExtensionBookMapping(
        val internalBookId: Int,
        val extensionId: String,
        val originalBookId: String
    )
}
