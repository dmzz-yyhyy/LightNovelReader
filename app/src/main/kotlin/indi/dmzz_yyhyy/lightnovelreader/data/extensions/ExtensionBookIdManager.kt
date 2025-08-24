package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles encoding and decoding of book IDs that contain extension information
 */
@Singleton
class ExtensionBookIdManager @Inject constructor() {

    companion object {
        private const val EXTENSION_PREFIX = "ext_"
        private const val SEPARATOR = "_"
        private const val HASH_MULTIPLIER = 31
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
     * Extracts extension information from a book ID
     */
    fun extractExtensionInfo(bookId: Int): ExtensionBookInfo? {
        // Since we're using hash-based IDs, we need to store the mapping
        // This is a limitation of the hash approach - we'll need a lookup table
        // For now, return null indicating this is not an extension book
        return null
    }

    /**
     * Checks if a book ID represents an extension book
     */
    fun isExtensionBook(bookId: Int): Boolean {
        // This would require a lookup table to properly determine
        // For now, we'll assume negative IDs or very large IDs are extension books
        return bookId > 1_000_000
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
