package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.Extension
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses and loads Lua-based extensions
 */
@Singleton
class LuaExtensionParser @Inject constructor() {

    /**
     * Parse a Lua extension file and create an Extension instance
     */
    fun parseExtension(file: File, extensionEntity: InstalledExtensionEntity): Extension? {
        return try {
            val luaScript = file.readText()
            val metadata = extractMetadata(luaScript, extensionEntity)
            LuaExtension(luaScript, metadata)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Extract metadata from Lua script comments or use entity data
     */
    private fun extractMetadata(luaScript: String, extensionEntity: InstalledExtensionEntity): LuaExtensionMetadata {
        // Try to extract metadata from Lua comments
        val extractedMetadata = parseMetadataFromComments(luaScript)
        
        return LuaExtensionMetadata(
            id = extensionEntity.id.toString(),
            name = extractedMetadata["name"] ?: extensionEntity.name,
            version = extractedMetadata["version"] ?: extensionEntity.version,
            language = extractedMetadata["language"] ?: extensionEntity.lang,
            description = extractedMetadata["description"] ?: extensionEntity.description
        )
    }

    /**
     * Parse metadata from Lua script comments
     * Expected format:
     * -- @name Extension Name
     * -- @version 1.0.0
     * -- @language en
     * -- @description Extension description
     */
    private fun parseMetadataFromComments(luaScript: String): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        
        luaScript.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("-- @")) {
                val parts = trimmedLine.substring(4).split(" ", limit = 2)
                if (parts.size == 2) {
                    metadata[parts[0]] = parts[1]
                }
            }
        }
        
        return metadata
    }

    /**
     * Validate that a Lua script contains required functions
     */
    fun validateLuaExtension(luaScript: String): ValidationResult {
        val requiredFunctions = listOf("search", "getBook", "getChapter", "getChapters")
        val missingFunctions = mutableListOf<String>()
        
        requiredFunctions.forEach { functionName ->
            if (!luaScript.contains("function $functionName") && 
                !luaScript.contains("$functionName = function")) {
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
