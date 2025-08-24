package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import app.shosetsu.lib.ShosetsuSharedLib
import app.shosetsu.lib.lua.ShosetsuLuaLib
import app.shosetsu.lib.lua.shosetsuGlobals
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.Extension
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import okhttp3.OkHttpClient
import org.luaj.vm2.LuaValue
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses and loads Lua-based extensions using Shosetsu's LuaExtension
 */
@Singleton
class LuaExtensionParser @Inject constructor() {

    init {
        initializeShosetsuEnvironment()
        initializeShosetsuLibLoader()
    }

    private fun initializeShosetsuEnvironment() {
        try {
            println("LuaExtensionParser: Initializing Shosetsu environment...")
            
            // Create HTTP client for Shosetsu extensions
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .build()
                    chain.proceed(request)
                }
                .build()
                
            // Initialize Shosetsu shared library
            ShosetsuSharedLib.httpClient = httpClient
            
            // Set up logger
            ShosetsuSharedLib.logger = { ext, arg ->
                println("ShosetsuLib[$ext]: $arg")
            }
            
            // Set headers for Shosetsu
            ShosetsuSharedLib.shosetsuHeaders = arrayOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
            )
            
            println("LuaExtensionParser: Shosetsu environment initialized successfully")
        } catch (e: Exception) {
            println("LuaExtensionParser: Failed to initialize Shosetsu environment: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun initializeShosetsuLibLoader() {
        // Initialize the Shosetsu lib loader
        ShosetsuLuaLib.libLoader = { name ->
            println("LuaExtensionParser: Loading library: $name")
            try {
                // Try to load actual library from Shosetsu's repository
                val libraryCode = try {
                    val url = URL("https://gitlab.com/shosetsuorg/extensions/-/raw/dev/lib/$name.lua")
                    val connection = url.openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 10000
                    connection.getInputStream().bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    println("LuaExtensionParser: Failed to download library $name: ${e.message}")
                    // Return a more comprehensive mock library based on common libraries
                    generateMockLibrary(name)
                }
                
                val globals = shosetsuGlobals()
                val chunk = globals.load(libraryCode, "lib($name)")
                val result = chunk.call()
                
                // Return the result of the chunk execution (which should be the library table)
                result
            } catch (e: Exception) {
                println("LuaExtensionParser: Error loading library $name: ${e.message}")
                // Return a basic empty table as fallback
                val globals = shosetsuGlobals()
                val mockLib = LuaValue.tableOf()
                
                // Add common functions that libraries might export
                mockLib.set("trim", LuaValue.tableOf())
                mockLib.set("querystringparse", LuaValue.tableOf())
                mockLib.set("querystring", LuaValue.tableOf())
                
                mockLib
            }
        }
    }
    
    private fun generateMockLibrary(name: String): String {
        return when (name.lowercase()) {
            "madara" -> """
                -- Mock Madara library
                local M = {}
                
                M.id = 1
                M.name = "Madara"
                M.baseURL = "https://example.com"
                M.listings = {}
                M.settings = {}
                
                function M.parseNovel(url, loadChapters)
                    return {
                        title = "Mock Novel",
                        authors = {"Mock Author"},
                        description = "Mock description",
                        imageURL = "",
                        tags = {},
                        chapters = {},
                        status = 0
                    }
                end
                
                function M.search(data)
                    return {}
                end
                
                function M.latest(data)
                    return {}
                end
                
                return M
            """.trimIndent()
            
            "novelvault" -> """
                -- Mock NovelVault library
                local M = {}
                
                M.id = 2
                M.name = "NovelVault"
                M.baseURL = "https://example.com"
                M.listings = {}
                M.settings = {}
                M.httpClient = {
                    get = function(url) return { body = { string = function() return "" end } } end,
                    post = function(url, data) return { body = { string = function() return "" end } } end
                }
                
                function M.parseNovel(url, loadChapters)
                    return {
                        title = "Mock Novel",
                        authors = {"Mock Author"},
                        description = "Mock description",
                        imageURL = "",
                        tags = {},
                        chapters = {},
                        status = 0
                    }
                end
                
                function M.search(data)
                    return {}
                end
                
                function M.latest(data)
                    return {}
                end
                
                return M
            """.trimIndent()
            
            else -> """
                -- Mock library for $name
                local M = {}
                
                M.id = 0
                M.name = "$name"
                M.baseURL = "https://example.com"
                M.listings = {}
                M.settings = {}
                
                function M.parseNovel(url, loadChapters)
                    return {
                        title = "Mock Novel",
                        authors = {"Mock Author"},
                        description = "Mock description",
                        imageURL = "",
                        tags = {},
                        chapters = {},
                        status = 0
                    }
                end
                
                function M.search(data)
                    return {}
                end
                
                function M.latest(data)
                    return {}
                end
                
                return M
            """.trimIndent()
        }
    }

    /**
     * Parse a Lua extension file and create an Extension instance using Shosetsu's implementation
     */
    fun parseExtension(file: File, extensionEntity: InstalledExtensionEntity): Extension? {
        return try {
            val luaScript = file.readText()
            val fileName = file.nameWithoutExtension
            ShosetsuLuaExtensionWrapper(luaScript, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Validate that a Lua script contains required functions or is dependency-based
     */
    fun validateLuaExtension(luaScript: String): ValidationResult {
        // Shosetsu extensions use: search, parseNovel, getPassage
        // Some extensions may not have all functions (dependency-based extensions)
        val requiredFunctions = listOf("search", "parseNovel", "getPassage")
        val missingFunctions = mutableListOf<String>()
        
        // Check if it's a dependency-based extension (uses Require())
        val isDependencyBased = luaScript.contains("Require(") || luaScript.contains("return {")
        
        if (isDependencyBased) {
            // For dependency-based extensions, just check if it has a return statement or Require call
            // These extensions get their functionality from base libraries
            return ValidationResult.Success
        }
        
        requiredFunctions.forEach { functionName ->
            if (!luaScript.contains("function $functionName") && 
                !luaScript.contains("$functionName = function") &&
                !luaScript.contains("$functionName =")) {
                missingFunctions.add(functionName)
            }
        }
        
        return if (missingFunctions.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.MissingFunctions(missingFunctions)
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class MissingFunctions(val functions: List<String>) : ValidationResult()
    }
}
