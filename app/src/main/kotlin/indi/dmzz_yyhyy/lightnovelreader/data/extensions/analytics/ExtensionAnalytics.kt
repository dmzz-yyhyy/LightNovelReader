package indi.dmzz_yyhyy.lightnovelreader.data.extensions.analytics

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks extension performance and errors for analytics and debugging
 */
@Singleton
class ExtensionAnalytics @Inject constructor() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Analytics data storage
    private val extensionStats = ConcurrentHashMap<String, ExtensionStats>()
    private val recentErrors = ConcurrentHashMap<String, MutableList<ExtensionError>>()
    
    private val _analyticsState = MutableStateFlow(ExtensionAnalyticsState())
    val analyticsState: StateFlow<ExtensionAnalyticsState> = _analyticsState.asStateFlow()

    /**
     * Record a search operation
     */
    fun recordSearch(extensionId: String, query: String, resultCount: Int, duration: Long, success: Boolean) {
        coroutineScope.launch {
            val stats = getOrCreateStats(extensionId)
            stats.totalSearches++
            stats.totalSearchTime += duration
//            stats.averageSearchTime = stats.totalSearchTime / stats.totalSearches
            
            if (success) {
                stats.successfulSearches++
                stats.totalSearchResults += resultCount
                stats.averageSearchResults = stats.totalSearchResults.toDouble() / stats.successfulSearches
            } else {
                stats.failedSearches++
            }
            
            updateAnalyticsState()
            
            Log.d("ExtensionAnalytics", "Search recorded for $extensionId: " +
                  "query='$query', results=$resultCount, duration=${duration}ms, success=$success")
        }
    }

    /**
     * Record a book fetch operation
     */
    fun recordBookFetch(extensionId: String, bookId: String, duration: Long, success: Boolean) {
        coroutineScope.launch {
            val stats = getOrCreateStats(extensionId)
            stats.totalBookFetches++
            stats.totalBookFetchTime += duration
//            stats.averageBookFetchTime = stats.totalBookFetchTime / stats.totalBookFetches
            
            if (success) {
                stats.successfulBookFetches++
            } else {
                stats.failedBookFetches++
            }
            
            updateAnalyticsState()
            
            Log.d("ExtensionAnalytics", "Book fetch recorded for $extensionId: " +
                  "bookId='$bookId', duration=${duration}ms, success=$success")
        }
    }

    /**
     * Record a chapter fetch operation
     */
    fun recordChapterFetch(extensionId: String, bookId: String, chapterId: String, duration: Long, success: Boolean) {
        coroutineScope.launch {
            val stats = getOrCreateStats(extensionId)
            stats.totalChapterFetches++
            stats.totalChapterFetchTime += duration
//            stats.averageChapterFetchTime = stats.totalChapterFetchTime / stats.totalChapterFetches
            
            if (success) {
                stats.successfulChapterFetches++
            } else {
                stats.failedChapterFetches++
            }
            
            updateAnalyticsState()
            
            Log.d("ExtensionAnalytics", "Chapter fetch recorded for $extensionId: " +
                  "bookId='$bookId', chapterId='$chapterId', duration=${duration}ms, success=$success")
        }
    }

    /**
     * Record an extension error
     */
    fun recordError(extensionId: String, operation: String, error: Throwable, context: String = "") {
        coroutineScope.launch {
            val stats = getOrCreateStats(extensionId)
            stats.totalErrors++
            
            val extensionError = ExtensionError(
                extensionId = extensionId,
                operation = operation,
                errorMessage = error.message ?: "Unknown error",
                errorType = error.javaClass.simpleName,
                context = context,
                timestamp = System.currentTimeMillis(),
                stackTrace = error.stackTraceToString()
            )
            
            // Add to recent errors (keep only last 50 per extension)
            val errors = recentErrors.getOrPut(extensionId) { mutableListOf() }
            errors.add(extensionError)
            if (errors.size > 50) {
                errors.removeAt(0)
            }
            
            updateAnalyticsState()
            
            Log.e("ExtensionAnalytics", "Error recorded for $extensionId: " +
                  "operation='$operation', error='${error.message}', context='$context'", error)
        }
    }

    /**
     * Record extension load time
     */
    fun recordExtensionLoad(extensionId: String, duration: Long, success: Boolean) {
        coroutineScope.launch {
            val stats = getOrCreateStats(extensionId)
            if (success) {
                stats.lastLoadTime = duration
                stats.loadSuccessCount++
            } else {
                stats.loadFailureCount++
            }
            
            updateAnalyticsState()
            
            Log.d("ExtensionAnalytics", "Extension load recorded for $extensionId: " +
                  "duration=${duration}ms, success=$success")
        }
    }

    /**
     * Get statistics for a specific extension
     */
    fun getExtensionStats(extensionId: String): ExtensionStats? {
        return extensionStats[extensionId]
    }

    /**
     * Get recent errors for a specific extension
     */
    fun getRecentErrors(extensionId: String): List<ExtensionError> {
        return recentErrors[extensionId]?.toList() ?: emptyList()
    }

    /**
     * Get all extension statistics
     */
    fun getAllStats(): Map<String, ExtensionStats> {
        return extensionStats.toMap()
    }

    /**
     * Clear statistics for a specific extension
     */
    fun clearExtensionStats(extensionId: String) {
        coroutineScope.launch {
            extensionStats.remove(extensionId)
            recentErrors.remove(extensionId)
            updateAnalyticsState()
            
            Log.d("ExtensionAnalytics", "Cleared statistics for $extensionId")
        }
    }

    /**
     * Clear all statistics
     */
    fun clearAllStats() {
        coroutineScope.launch {
            extensionStats.clear()
            recentErrors.clear()
            updateAnalyticsState()
            
            Log.d("ExtensionAnalytics", "Cleared all extension statistics")
        }
    }

    /**
     * Generate performance report for an extension
     */
    fun generatePerformanceReport(extensionId: String): ExtensionPerformanceReport? {
        val stats = extensionStats[extensionId] ?: return null
        val errors = recentErrors[extensionId] ?: emptyList()
        
        return ExtensionPerformanceReport(
            extensionId = extensionId,
            stats = stats,
            recentErrors = errors,
            reliabilityScore = calculateReliabilityScore(stats),
            performanceScore = calculatePerformanceScore(stats)
        )
    }

    private fun getOrCreateStats(extensionId: String): ExtensionStats {
        return extensionStats.getOrPut(extensionId) { ExtensionStats(extensionId) }
    }

    private fun updateAnalyticsState() {
        val totalExtensions = extensionStats.size
        val totalOperations = extensionStats.values.sumOf { 
            it.totalSearches + it.totalBookFetches + it.totalChapterFetches 
        }
        val totalErrors = extensionStats.values.sumOf { it.totalErrors }
        
        _analyticsState.value = ExtensionAnalyticsState(
            totalExtensions = totalExtensions,
            totalOperations = totalOperations,
            totalErrors = totalErrors,
            overallSuccessRate = if (totalOperations > 0) {
                (totalOperations - totalErrors).toDouble() / totalOperations
            } else 0.0
        )
    }

    private fun calculateReliabilityScore(stats: ExtensionStats): Double {
        val totalOperations = stats.totalSearches + stats.totalBookFetches + stats.totalChapterFetches
//        if (totalOperations == 0) return 1.0
        
        val successfulOperations = stats.successfulSearches + stats.successfulBookFetches + stats.successfulChapterFetches
        return successfulOperations.toDouble() / totalOperations
    }

    private fun calculatePerformanceScore(stats: ExtensionStats): Double {
        // Performance score based on average response times
        // Lower times = higher score (normalized to 0-1 scale)
        val avgTime = (stats.averageSearchTime + stats.averageBookFetchTime + stats.averageChapterFetchTime) / 3.0
        
        // Normalize: anything under 1000ms gets full score, anything over 10000ms gets 0
        return when {
            avgTime <= 1000 -> 1.0
            avgTime >= 10000 -> 0.0
            else -> (10000 - avgTime) / 9000.0
        }
    }
}

data class ExtensionStats(
    val extensionId: String,
    var totalSearches: Long = 0,
    var successfulSearches: Long = 0,
    var failedSearches: Long = 0,
    var totalSearchTime: Long = 0,
    var averageSearchTime: Double = 0.0,
    var totalSearchResults: Long = 0,
    var averageSearchResults: Double = 0.0,
    
    var totalBookFetches: Long = 0,
    var successfulBookFetches: Long = 0,
    var failedBookFetches: Long = 0,
    var totalBookFetchTime: Long = 0,
    var averageBookFetchTime: Double = 0.0,
    
    var totalChapterFetches: Long = 0,
    var successfulChapterFetches: Long = 0,
    var failedChapterFetches: Long = 0,
    var totalChapterFetchTime: Long = 0,
    var averageChapterFetchTime: Double = 0.0,
    
    var totalErrors: Long = 0,
    var lastLoadTime: Long = 0,
    var loadSuccessCount: Long = 0,
    var loadFailureCount: Long = 0
)

data class ExtensionError(
    val extensionId: String,
    val operation: String,
    val errorMessage: String,
    val errorType: String,
    val context: String,
    val timestamp: Long,
    val stackTrace: String
)

data class ExtensionAnalyticsState(
    val totalExtensions: Int = 0,
    val totalOperations: Long = 0,
    val totalErrors: Long = 0,
    val overallSuccessRate: Double = 0.0
)

data class ExtensionPerformanceReport(
    val extensionId: String,
    val stats: ExtensionStats,
    val recentErrors: List<ExtensionError>,
    val reliabilityScore: Double,
    val performanceScore: Double
)
