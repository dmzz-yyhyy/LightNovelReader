package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages caching of Lua libraries for extensions
 */
@Singleton
class LuaLibraryCache @Inject constructor(
    private val context: Context
) {
    private val mutex = Mutex()
    private val memoryCache = mutableMapOf<String, String>()
    private val cacheDir = File(context.filesDir, "lua_libraries")
    
    companion object {
        private const val CACHE_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L // 7 days
        private const val SHOSETSU_LIB_BASE_URL = "https://gitlab.com/shosetsuorg/extensions/-/raw/dev/lib"
    }
    
    init {
        // Ensure cache directory exists
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * Get library code, using cache when available
     */
    suspend fun getLibraryCode(libraryName: String): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            // Check memory cache first
            memoryCache[libraryName]?.let { return@withLock it }
            
            // Check disk cache
            val cacheFile = File(cacheDir, "$libraryName.lua")
            if (cacheFile.exists() && isCacheValid(cacheFile)) {
                val cachedCode = cacheFile.readText()
                memoryCache[libraryName] = cachedCode
                println("LuaLibraryCache: Loaded $libraryName from disk cache")
                return@withLock cachedCode
            }
            
            // Download from remote
            val libraryCode = downloadLibrary(libraryName)
            if (libraryCode != null) {
                // Cache to disk
                cacheFile.writeText(libraryCode)
                memoryCache[libraryName] = libraryCode
                println("LuaLibraryCache: Downloaded and cached $libraryName")
                return@withLock libraryCode
            } else {
                // Use fallback implementation
                val fallbackCode = generateFallbackLibrary(libraryName)
                memoryCache[libraryName] = fallbackCode
                println("LuaLibraryCache: Using fallback implementation for $libraryName")
                return@withLock fallbackCode
            }
        }
    }
    
    private suspend fun downloadLibrary(libraryName: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("$SHOSETSU_LIB_BASE_URL/$libraryName.lua")
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "LightNovelReader/1.0")
            
            val libraryCode = connection.getInputStream().bufferedReader().use { it.readText() }
            println("LuaLibraryCache: Successfully downloaded $libraryName (${libraryCode.length} chars)")
            libraryCode
        } catch (e: Exception) {
            println("LuaLibraryCache: Failed to download $libraryName: ${e.message}")
            null
        }
    }
    
    private fun isCacheValid(cacheFile: File): Boolean {
        val lastModified = cacheFile.lastModified()
        val currentTime = System.currentTimeMillis()
        return currentTime - lastModified < CACHE_EXPIRATION_TIME
    }
    
    /**
     * Clear all cached libraries
     */
    suspend fun clearCache() {
        mutex.withLock {
            memoryCache.clear()
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            println("LuaLibraryCache: Cleared all library cache")
        }
    }
    
    /**
     * Pre-download commonly used libraries
     */
    suspend fun preloadCommonLibraries() {
        val commonLibraries = listOf(
            "Madara",
            "novelvault", 
            "url",
            "dkjson",
            "XenForo",
            "utf8",
            "unhtml"
        )
        
        println("LuaLibraryCache: Preloading ${commonLibraries.size} common libraries...")
        commonLibraries.forEach { libName ->
            try {
                getLibraryCode(libName)
                println("LuaLibraryCache: Preloaded $libName")
            } catch (e: Exception) {
                println("LuaLibraryCache: Failed to preload $libName: ${e.message}")
            }
        }
        println("LuaLibraryCache: Preloading completed")
    }
    
    private fun generateFallbackLibrary(libraryName: String): String {
        return when (libraryName.lowercase()) {
            "madara" -> generateMadaraFallback()
            "novelvault" -> generateNovelVaultFallback()
            "url" -> generateUrlFallback()
            "dkjson" -> generateDkJsonFallback()
            "utf8" -> generateUtf8Fallback()
            else -> generateGenericFallback(libraryName)
        }
    }
    
    private fun generateMadaraFallback(): String {
        return """
        -- Enhanced Madara library fallback
        local M = {
            id = 1,
            name = "Madara",
            baseURL = "",
            listings = {
                {
                    name = "Latest",
                    increments = true,
                    endpoint = "novel/?m_orderby=latest"
                }
            },
            hasSearch = true,
            hasCloudFlare = false,
            searchHasOper = false,
            
            -- Default selectors for Madara-based sites
            latestNovelSel = "div.page-item-detail",
            searchNovelSel = "div.c-tabs-item__content",
            novelListingURLPath = "novel",
            novelPageTitleSel = "div.post-title h1",
            novelPageDescSel = "div.description-summary div.summary__content",
            novelPageAuthorSel = "div.author-content a",
            novelPageCoverSel = "div.summary_image img",
            novelPageGenresSel = "div.genres-content a",
            novelPageStatusSel = "div.post-status div.summary-content",
            chaptersListSelector = "li.wp-manga-chapter",
            chapterContentSel = "div.reading-content",
            
            -- Default settings
            shrinkURLNovel = "novel",
            chapterType = 0, -- HTML
            chaptersOrderReversed = true,
            isSearchIncrementing = true
        }
        
        -- Helper functions that are commonly used
        function M:defaults() 
            return {
                latestNovelSel = self.latestNovelSel,
                searchNovelSel = self.searchNovelSel,
                novelListingURLPath = self.novelListingURLPath,
                novelPageTitleSel = self.novelPageTitleSel,
                novelPageDescSel = self.novelPageDescSel,
                novelPageAuthorSel = self.novelPageAuthorSel,
                novelPageCoverSel = self.novelPageCoverSel,
                novelPageGenresSel = self.novelPageGenresSel,
                novelPageStatusSel = self.novelPageStatusSel,
                chaptersListSelector = self.chaptersListSelector,
                chapterContentSel = self.chapterContentSel,
                shrinkURLNovel = self.shrinkURLNovel,
                chapterType = self.chapterType,
                chaptersOrderReversed = self.chaptersOrderReversed,
                isSearchIncrementing = self.isSearchIncrementing
            }
        end
        
        function M:search(data)
            return {}
        end
        
        function M:parseNovel(url, loadChapters)
            return {
                title = "Fallback Novel",
                authors = {"Unknown Author"},
                description = "Content not available - library fallback",
                imageURL = "",
                status = 0,
                chapters = {},
                tags = {}
            }
        end
        
        function M:latest(data)
            return {}
        end
        
        function M:getPassage(url)
            return ""
        end
        
        -- Provide common utility functions
        function M:shrinkURL(url)
            if not url then return "" end
            return url
        end
        
        function M:expandURL(url)
            if not url then return "" end
            if url:sub(1,4) == "http" then
                return url
            else
                return (self.baseURL or "") .. url
            end
        end
        
        return M
        """.trimIndent()
    }
    
    private fun generateNovelVaultFallback(): String {
        return """
        -- Enhanced NovelVault library fallback
        local M = {
            id = 2,
            name = "NovelVault",
            baseURL = "",
            listings = {},
            hasSearch = true
        }
        
        function M:search(data)
            return {}
        end
        
        function M:parseNovel(url, loadChapters)
            return {
                title = "Fallback Novel",
                authors = {"Unknown Author"},
                description = "Content not available - library fallback",
                imageURL = "",
                status = 0,
                chapters = {},
                tags = {}
            }
        end
        
        function M:latest(data)
            return {}
        end
        
        function M:getPassage(url)
            return ""
        end
        
        return M
        """.trimIndent()
    }
    
    private fun generateUrlFallback(): String {
        return """
        -- URL utility library fallback
        local url = {}
        
        function url.encode(str)
            if not str then return "" end
            return str
        end
        
        function url.decode(str)
            if not str then return "" end
            return str
        end
        
        function url.querystring(params)
            if not params then return "" end
            local result = {}
            for k, v in pairs(params) do
                table.insert(result, tostring(k) .. "=" .. tostring(v))
            end
            return table.concat(result, "&")
        end
        
        return url
        """.trimIndent()
    }
    
    private fun generateDkJsonFallback(): String {
        return """
        -- JSON library fallback
        local json = { version = "dkjson 2.5" }
        
        function json.decode(str)
            if not str or str == "" then
                return {}
            end
            return {}
        end
        
        function json.encode(obj)
            return "{}"
        end
        
        return json
        """.trimIndent()
    }
    
    private fun generateUtf8Fallback(): String {
        return """
        -- UTF8 library fallback
        local utf8 = {}
        
        function utf8.char(...)
            return ""
        end
        
        function utf8.len(s)
            if not s then return 0 end
            return string.len(s)
        end
        
        return utf8
        """.trimIndent()
    }
    
    private fun generateGenericFallback(libraryName: String): String {
        return """
        -- Generic library fallback for $libraryName
        local M = {
            name = "$libraryName",
            id = 0
        }
        
        function M:search(data)
            return {}
        end
        
        function M:parseNovel(url, loadChapters)
            return {
                title = "Library Not Available",
                authors = {"Unknown"},
                description = "Library $libraryName could not be loaded",
                imageURL = "",
                status = 0,
                chapters = {}
            }
        end
        
        function M:latest(data)
            return {}
        end
        
        return M
        """.trimIndent()
    }
}
