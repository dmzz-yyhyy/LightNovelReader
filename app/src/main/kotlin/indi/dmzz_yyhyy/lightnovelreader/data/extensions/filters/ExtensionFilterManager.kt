package indi.dmzz_yyhyy.lightnovelreader.data.extensions.filters

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult

/**
 * Represents a filter that can be applied to extension searches
 */
sealed class ExtensionFilter {
    abstract fun apply(results: List<ExtensionSearchResult>): List<ExtensionSearchResult>
    
    data class ByLanguage(val languages: Set<String>) : ExtensionFilter() {
        override fun apply(results: List<ExtensionSearchResult>): List<ExtensionSearchResult> {
            if (languages.isEmpty()) return results
            return results.filter { result ->
                // This would need extension metadata to determine language
                true // For now, return all
            }
        }
    }
    
    data class ByGenre(val genres: Set<String>) : ExtensionFilter() {
        override fun apply(results: List<ExtensionSearchResult>): List<ExtensionSearchResult> {
            if (genres.isEmpty()) return results
            return results.filter { result ->
                // This would check if the book has any of the specified genres
                genres.any { genre -> 
                    result.description.contains(genre, ignoreCase = true)
                }
            }
        }
    }
    
    data class ByStatus(val statuses: Set<String>) : ExtensionFilter() {
        override fun apply(results: List<ExtensionSearchResult>): List<ExtensionSearchResult> {
            if (statuses.isEmpty()) return results
            return results.filter { result ->
                statuses.any { status ->
                    result.description.contains(status, ignoreCase = true)
                }
            }
        }
    }
    
    data class ByExtension(val extensionIds: Set<String>) : ExtensionFilter() {
        override fun apply(results: List<ExtensionSearchResult>): List<ExtensionSearchResult> {
            if (extensionIds.isEmpty()) return results
            return results.filter { result ->
                extensionIds.contains(result.extensionId)
            }
        }
    }
    
    data class ByTitle(val query: String) : ExtensionFilter() {
        override fun apply(results: List<ExtensionSearchResult>): List<ExtensionSearchResult> {
            if (query.isBlank()) return results
            return results.filter { result ->
                result.title.contains(query, ignoreCase = true)
            }
        }
    }
    
    data class ByAuthor(val query: String) : ExtensionFilter() {
        override fun apply(results: List<ExtensionSearchResult>): List<ExtensionSearchResult> {
            if (query.isBlank()) return results
            return results.filter { result ->
                result.author.contains(query, ignoreCase = true)
            }
        }
    }
}

/**
 * Combines multiple filters and applies them to search results
 */
class ExtensionFilterManager {
    
    private val activeFilters = mutableSetOf<ExtensionFilter>()
    
    fun addFilter(filter: ExtensionFilter) {
        // Remove existing filter of the same type
        activeFilters.removeIf { it::class == filter::class }
        activeFilters.add(filter)
    }
    
    fun removeFilter(filterClass: kotlin.reflect.KClass<out ExtensionFilter>) {
        activeFilters.removeIf { it::class == filterClass }
    }
    
    fun clearFilters() {
        activeFilters.clear()
    }
    
    fun applyFilters(results: List<ExtensionSearchResult>): List<ExtensionSearchResult> {
        return activeFilters.fold(results) { filteredResults, filter ->
            filter.apply(filteredResults)
        }
    }
    
    fun getActiveFilters(): Set<ExtensionFilter> = activeFilters.toSet()
    
    fun hasActiveFilters(): Boolean = activeFilters.isNotEmpty()
}

/**
 * Sorting options for extension search results
 */
enum class ExtensionSortOrder {
    TITLE_ASC,
    TITLE_DESC,
    AUTHOR_ASC,
    AUTHOR_DESC,
    EXTENSION_NAME,
    RELEVANCE
}

/**
 * Sorts extension search results
 */
class ExtensionSorter {
    
    fun sort(results: List<ExtensionSearchResult>, sortOrder: ExtensionSortOrder): List<ExtensionSearchResult> {
        return when (sortOrder) {
            ExtensionSortOrder.TITLE_ASC -> results.sortedBy { it.title }
            ExtensionSortOrder.TITLE_DESC -> results.sortedByDescending { it.title }
            ExtensionSortOrder.AUTHOR_ASC -> results.sortedBy { it.author }
            ExtensionSortOrder.AUTHOR_DESC -> results.sortedByDescending { it.author }
            ExtensionSortOrder.EXTENSION_NAME -> results.sortedBy { it.extensionId }
            ExtensionSortOrder.RELEVANCE -> results // Keep original order for relevance
        }
    }
}
