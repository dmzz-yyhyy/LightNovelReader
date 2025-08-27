package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages caching for extension data to improve performance
 */
@Singleton
class ExtensionCacheManager @Inject constructor() {

    private val searchResultsCache = ConcurrentHashMap<String, List<ExtensionSearchResult>>()
    private val bookInfoCache = ConcurrentHashMap<String, ExtensionBook>()
    private val chapterListCache = ConcurrentHashMap<String, List<ExtensionChapter>>()
    private val chapterContentCache = ConcurrentHashMap<String, ExtensionChapter>()
    private val latestBooksCache = ConcurrentHashMap<String, List<ExtensionSearchResult>>()
    
    private val mutex = Mutex()
    
    // Cache expiration time (30 minutes)
    private val cacheExpirationTime = 30 * 60 * 1000L
    private val cacheTimestamps = ConcurrentHashMap<String, Long>()

    /**
     * Cache search results
     */
    suspend fun cacheSearchResults(extensionId: String, query: String, results: List<ExtensionSearchResult>) {
        mutex.withLock {
            val key = "${extensionId}_search_$query"
            searchResultsCache[key] = results
            cacheTimestamps[key] = System.currentTimeMillis()
        }
    }

    /**
     * Get cached search results
     */
    suspend fun getCachedSearchResults(extensionId: String, query: String): List<ExtensionSearchResult>? {
        mutex.withLock {
            val key = "${extensionId}_search_$query"
            if (isCacheValid(key)) {
                return searchResultsCache[key]
            } else {
                // Remove expired cache
                searchResultsCache.remove(key)
                cacheTimestamps.remove(key)
                return null
            }
        }
    }

    /**
     * Cache book information
     */
    suspend fun cacheBookInfo(extensionId: String, bookId: String, book: ExtensionBook) {
        mutex.withLock {
            val key = "${extensionId}_book_$bookId"
            bookInfoCache[key] = book
            cacheTimestamps[key] = System.currentTimeMillis()
        }
    }

    /**
     * Get cached book information
     */
    suspend fun getCachedBookInfo(extensionId: String, bookId: String): ExtensionBook? {
        mutex.withLock {
            val key = "${extensionId}_book_$bookId"
            if (isCacheValid(key)) {
                return bookInfoCache[key]
            } else {
                bookInfoCache.remove(key)
                cacheTimestamps.remove(key)
                return null
            }
        }
    }

    /**
     * Cache chapter list
     */
    suspend fun cacheChapterList(extensionId: String, bookId: String, chapters: List<ExtensionChapter>) {
        mutex.withLock {
            val key = "${extensionId}_chapters_$bookId"
            chapterListCache[key] = chapters
            cacheTimestamps[key] = System.currentTimeMillis()
        }
    }

    /**
     * Get cached chapter list
     */
    suspend fun getCachedChapterList(extensionId: String, bookId: String): List<ExtensionChapter>? {
        mutex.withLock {
            val key = "${extensionId}_chapters_$bookId"
            if (isCacheValid(key)) {
                return chapterListCache[key]
            } else {
                chapterListCache.remove(key)
                cacheTimestamps.remove(key)
                return null
            }
        }
    }

    /**
     * Cache chapter content
     */
    suspend fun cacheChapterContent(extensionId: String, bookId: String, chapterId: String, chapter: ExtensionChapter) {
        mutex.withLock {
            val key = "${extensionId}_chapter_${bookId}_$chapterId"
            chapterContentCache[key] = chapter
            cacheTimestamps[key] = System.currentTimeMillis()
        }
    }

    /**
     * Get cached chapter content
     */
    suspend fun getCachedChapterContent(extensionId: String, bookId: String, chapterId: String): ExtensionChapter? {
        mutex.withLock {
            val key = "${extensionId}_chapter_${bookId}_$chapterId"
            if (isCacheValid(key)) {
                return chapterContentCache[key]
            } else {
                chapterContentCache.remove(key)
                cacheTimestamps.remove(key)
                return null
            }
        }
    }

    /**
     * Cache latest books from extension
     */
    suspend fun cacheLatestBooks(extensionId: String, books: List<ExtensionSearchResult>) {
        mutex.withLock {
            val key = "${extensionId}_latest"
            latestBooksCache[key] = books
            cacheTimestamps[key] = System.currentTimeMillis()
        }
    }

    /**
     * Get cached latest books
     */
    suspend fun getCachedLatestBooks(extensionId: String): List<ExtensionSearchResult>? {
        mutex.withLock {
            val key = "${extensionId}_latest"
            if (isCacheValid(key)) {
                return latestBooksCache[key]
            } else {
                latestBooksCache.remove(key)
                cacheTimestamps.remove(key)
                return null
            }
        }
    }

    /**
     * Clear all cache for a specific extension
     */
    suspend fun clearExtensionCache(extensionId: String) {
        mutex.withLock {
            val keysToRemove = mutableListOf<String>()
            
            // Find all keys that start with the extension ID
            searchResultsCache.keys.forEach { key ->
                if (key.startsWith("${extensionId}_")) {
                    keysToRemove.add(key)
                }
            }
            
            // Remove from all caches
            keysToRemove.forEach { key ->
                searchResultsCache.remove(key)
                bookInfoCache.remove(key)
                chapterListCache.remove(key)
                chapterContentCache.remove(key)
                latestBooksCache.remove(key)
                cacheTimestamps.remove(key)
            }
        }
    }

    /**
     * Clear all expired cache entries
     */
    suspend fun clearExpiredCache() {
        mutex.withLock {
            val expiredKeys = mutableListOf<String>()
            
            cacheTimestamps.forEach { (key, timestamp) ->
                if (System.currentTimeMillis() - timestamp > cacheExpirationTime) {
                    expiredKeys.add(key)
                }
            }
            
            expiredKeys.forEach { key ->
                searchResultsCache.remove(key)
                bookInfoCache.remove(key)
                chapterListCache.remove(key)
                chapterContentCache.remove(key)
                latestBooksCache.remove(key)
                cacheTimestamps.remove(key)
            }
            
            println("ExtensionCacheManager: Cleared ${expiredKeys.size} expired cache entries")
        }
    }

    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): ExtensionCacheStats {
        mutex.withLock {
            return ExtensionCacheStats(
                searchResultsCount = searchResultsCache.size,
                bookInfoCount = bookInfoCache.size,
                chapterListCount = chapterListCache.size,
                chapterContentCount = chapterContentCache.size,
                latestBooksCount = latestBooksCache.size,
                totalCacheSize = searchResultsCache.size + bookInfoCache.size + 
                               chapterListCache.size + chapterContentCache.size + latestBooksCache.size
            )
        }
    }

    private fun isCacheValid(key: String): Boolean {
        val timestamp = cacheTimestamps[key] ?: return false
        return System.currentTimeMillis() - timestamp < cacheExpirationTime
    }
}

data class ExtensionCacheStats(
    val searchResultsCount: Int,
    val bookInfoCount: Int,
    val chapterListCount: Int,
    val chapterContentCount: Int,
    val latestBooksCount: Int,
    val totalCacheSize: Int
)
