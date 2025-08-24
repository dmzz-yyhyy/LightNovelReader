package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.Extension
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import org.luaj.vm2.*
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.JsePlatform

/**
 * Lua-based extension implementation
 */
class LuaExtension(
    private val luaScript: String,
    private val metadata: LuaExtensionMetadata
) : Extension {

    override val id: String = metadata.id
    override val name: String = metadata.name
    override val version: String = metadata.version
    override val language: String = metadata.language
    override val description: String = metadata.description

    private val globals: Globals = JsePlatform.standardGlobals()
    private val chunk: LuaValue

    init {
        // Set up Shosetsu-compatible Lua environment
        setupShosetsuEnvironment()
        
        // Load and compile the Lua script
        chunk = globals.load(luaScript)
        chunk.call()
    }
    
    private fun setupShosetsuEnvironment() {
        // Add Require function for dependency loading
        globals.set("Require", object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return object : OneArgFunction() {
                    override fun call(arg: LuaValue): LuaValue {
                        val libName = arg.tojstring()
                        return when (libName) {
                            "url" -> createUrlLibrary()
                            "dkjson" -> createJsonLibrary()
                            "utf8" -> createUtf8Library()
                            else -> {
                                // For Madara and other dependencies, return a mock object
                                return object : OneArgFunction() {
                                    override fun call(baseUrl: LuaValue): LuaValue {
                                        return createMadaraExtension(baseUrl.tojstring())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
        
        // Add HTML/Document functions
        globals.set("GETDocument", object : OneArgFunction() {
            override fun call(url: LuaValue): LuaValue {
                // Mock document - in real implementation, this would fetch and parse HTML
                return createMockDocument()
            }
        })
        
        // Add Shosetsu data types
        globals.set("Novel", object : OneArgFunction() {
            override fun call(table: LuaValue): LuaValue {
                return table // Just return the table for now
            }
        })
        
        globals.set("NovelInfo", object : OneArgFunction() {
            override fun call(table: LuaValue): LuaValue {
                return table // Just return the table for now
            }
        })
        
        globals.set("NovelChapter", object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return LuaValue.tableOf()
            }
        })
        
        // Add utility functions
        globals.set("mapNotNil", object : TwoArgFunction() {
            override fun call(list: LuaValue, func: LuaValue): LuaValue {
                return LuaValue.tableOf() // Mock implementation
            }
        })
        
        globals.set("map", object : TwoArgFunction() {
            override fun call(list: LuaValue, func: LuaValue): LuaValue {
                return LuaValue.tableOf() // Mock implementation
            }
        })
        
        globals.set("AsList", object : OneArgFunction() {
            override fun call(table: LuaValue): LuaValue {
                return table
            }
        })
        
        globals.set("pageOfElem", object : TwoArgFunction() {
            override fun call(element: LuaValue, flag: LuaValue): LuaValue {
                return LuaValue.valueOf("") // Mock implementation
            }
        })
        
        globals.set("Listing", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                return LuaValue.tableOf()
            }
        })
        
        // Add constants
        val chapterType = LuaValue.tableOf()
        chapterType.set("HTML", LuaValue.valueOf(0))
        chapterType.set("STRING", LuaValue.valueOf(1))
        globals.set("ChapterType", chapterType)
        
        // Add PAGE constant
        globals.set("PAGE", LuaValue.valueOf(1))
        
        // Add NovelStatus constants
        val novelStatus = LuaValue.tableOf()
        novelStatus.set("PUBLISHING", LuaValue.valueOf(0))
        novelStatus.set("COMPLETED", LuaValue.valueOf(1))
        novelStatus.set("PAUSED", LuaValue.valueOf(2))
        novelStatus.set("UNKNOWN", LuaValue.valueOf(3))
        globals.set("NovelStatus", novelStatus)
    }
    
    private fun createUrlLibrary(): LuaValue {
        val urlLib = LuaValue.tableOf()
        urlLib.set("encode", object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                return LuaValue.valueOf(java.net.URLEncoder.encode(arg.tojstring(), "UTF-8"))
            }
        })
        return urlLib
    }
    
    private fun createJsonLibrary(): LuaValue {
        val jsonLib = LuaValue.tableOf()
        jsonLib.set("GET", object : OneArgFunction() {
            override fun call(url: LuaValue): LuaValue {
                // Mock JSON response
                return LuaValue.tableOf()
            }
        })
        return jsonLib
    }
    
    private fun createUtf8Library(): LuaValue {
        val utf8Lib = LuaValue.tableOf()
        utf8Lib.set("char", object : OneArgFunction() {
            override fun call(codepoint: LuaValue): LuaValue {
                return LuaValue.valueOf(String(Character.toChars(codepoint.toint())))
            }
        })
        return utf8Lib
    }
    
    private fun createMadaraExtension(baseUrl: String): LuaValue {
        // Mock Madara extension
        return LuaValue.tableOf()
    }
    
    private fun createMockDocument(): LuaValue {
        val doc = LuaValue.tableOf()
        doc.set("select", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                return LuaValue.tableOf()
            }
        })
        doc.set("selectFirst", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val element = LuaValue.tableOf()
                element.set("text", object : ZeroArgFunction() {
                    override fun call(): LuaValue {
                        return LuaValue.valueOf("Mock Text")
                    }
                })
                element.set("attr", object : OneArgFunction() {
                    override fun call(attrName: LuaValue): LuaValue {
                        return LuaValue.valueOf("mock-attr")
                    }
                })
                return element
            }
        })
        return doc
    }

    override suspend fun search(query: String): List<ExtensionSearchResult> {
        return try {
            // Try global search function (most common)
            val searchFunction = globals.get("search")
            if (searchFunction.isfunction()) {
                val result = searchFunction.call(LuaValue.valueOf(query))
                return parseLuaSearchResults(result)
            }

            println("LuaExtension: No search function found for $name")
            emptyList()
        } catch (e: Exception) {
            println("LuaExtension: Search failed for $name: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getBook(id: String): ExtensionBook? {
        return try {
            // Try parseNovel function (Shosetsu style)
            val parseNovelFunction = globals.get("parseNovel")
            if (parseNovelFunction.isfunction()) {
                val result = parseNovelFunction.call(
                    LuaValue.valueOf(id),
                    LuaValue.TRUE // loadChapters = true
                )
                return parseLuaBook(result)
            }

            // Last fallback to getBook function (our style)
            val getBookFunction = globals.get("getBook")
            if (getBookFunction.isfunction()) {
                val result = getBookFunction.call(LuaValue.valueOf(id))
                return parseLuaBook(result)
            }

            println("LuaExtension: No parseNovel or getBook function found for $name")
            null
        } catch (e: Exception) {
            println("LuaExtension: getBook failed for $name: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun getChapter(bookId: String, chapterId: String): ExtensionChapter? {
        return try {
            // Try getPassage function (Shosetsu style)
            val getPassageFunction = globals.get("getPassage")
            if (getPassageFunction.isfunction()) {
                val result = getPassageFunction.call(LuaValue.valueOf(chapterId))
                return parseLuaChapter(result)
            }

            // Last fallback to getChapter function (our style)
            val getChapterFunction = globals.get("getChapter")
            if (getChapterFunction.isfunction()) {
                val result = getChapterFunction.call(
                    LuaValue.valueOf(bookId),
                    LuaValue.valueOf(chapterId)
                )
                return parseLuaChapter(result)
            }

            println("LuaExtension: No getPassage or getChapter function found for $name")
            null
        } catch (e: Exception) {
            println("LuaExtension: getChapter failed for $name: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun getChapters(bookId: String): List<ExtensionChapter>? {
        return try {
            // For Shosetsu extensions, chapters are typically returned by parseNovel
            // so we try to get them from there first
            val book = getBook(bookId)
            book ?: run {
                // Fallback to getChapters function if it exists
                val getChaptersFunction = globals.get("getChapters")
                if (getChaptersFunction.isfunction()) {
                    val result = getChaptersFunction.call(LuaValue.valueOf(bookId))
                    parseLuaChapterList(result)
                } else {
                    println("LuaExtension: No chapters found for $name")
                    null
                }
            }
        } catch (e: Exception) {
            println("LuaExtension: getChapters failed for $name: ${e.message}")
            e.printStackTrace()
            null
        } as List<ExtensionChapter>?
    }

    private fun parseLuaSearchResults(luaValue: LuaValue): List<ExtensionSearchResult> {
        if (!luaValue.istable()) return emptyList()

        val results = mutableListOf<ExtensionSearchResult>()
        val table = luaValue.checktable()

        var i = 1
        while (true) {
            val entry = table.get(i)
            if (entry.isnil()) break

            if (entry.istable()) {
                val entryTable = entry.checktable()
                val searchResult = ExtensionSearchResult(
                    id = entryTable.get("id").optjstring(""),
                    title = entryTable.get("title").optjstring(""),
                    author = entryTable.get("author").optjstring(""),
                    description = entryTable.get("description").optjstring(""),
                    imageUrl = entryTable.get("imageUrl").optjstring(""),
                    url = entryTable.get("url").optjstring(""),
                    extensionId = this.id
                )
                results.add(searchResult)
            }
            i++
        }

        return results
    }

    private fun parseLuaBook(luaValue: LuaValue): ExtensionBook? {
        if (!luaValue.istable()) return null

        val table = luaValue.checktable()
        return ExtensionBook(
            id = table.get("id").optjstring(""),
            title = table.get("title").optjstring(""),
            author = table.get("author").optjstring(""),
            description = table.get("description").optjstring(""),
            imageUrl = table.get("imageUrl").optjstring(""),
            url = table.get("url").optjstring(""),
            genres = parseLuaStringList(table.get("genres")),
            status = table.get("status").optjstring(""),
            lastUpdated = table.get("lastUpdated").optlong(0),
            chapters = parseLuaChapterList(table.get("chapters")) ?: emptyList()
        )
    }

    private fun parseLuaChapter(luaValue: LuaValue): ExtensionChapter? {
        if (!luaValue.istable()) return null

        val table = luaValue.checktable()
        return ExtensionChapter(
            id = table.get("id").optjstring(""),
            title = table.get("title").optjstring(""),
            content = table.get("content").optjstring(""),
            url = table.get("url").optjstring(""),
            order = table.get("order").optint(0),
            releaseDate = table.get("releaseDate").optlong(0)
        )
    }

    private fun parseLuaChapterList(luaValue: LuaValue): List<ExtensionChapter>? {
        if (!luaValue.istable()) return null

        val chapters = mutableListOf<ExtensionChapter>()
        val table = luaValue.checktable()

        var i = 1
        while (true) {
            val entry = table.get(i)
            if (entry.isnil()) break

            parseLuaChapter(entry)?.let { chapters.add(it) }
            i++
        }

        return chapters
    }

    private fun parseLuaStringList(luaValue: LuaValue): List<String> {
        if (!luaValue.istable()) return emptyList()

        val list = mutableListOf<String>()
        val table = luaValue.checktable()

        var i = 1
        while (true) {
            val entry = table.get(i)
            if (entry.isnil()) break

            list.add(entry.optjstring(""))
            i++
        }

        return list
    }
}

data class LuaExtensionMetadata(
    val id: String,
    val name: String,
    val version: String,
    val language: String,
    val description: String
)
