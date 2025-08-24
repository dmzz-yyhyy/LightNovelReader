package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.Extension
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

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
        setupShosetsuGlobals()

        // Add Require function for dependency loading
        globals.set("Require", object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                val libName = arg.tojstring()
                return try {
                    when (libName) {
                        "url" -> loadLibraryFromRemote("url")
                        "dkjson" -> loadLibraryFromRemote("dkjson")
                        "Madara" -> loadLibraryFromRemote("Madara")
                        "utf8" -> createUtf8Library()
                        "XenForo" -> loadLibraryFromRemote("XenForo")
                        else -> {
                            println("LuaExtension: Attempting to load library: $libName")
                            loadLibraryFromRemote(libName)
                        }
                    }
                } catch (e: Exception) {
                    println("LuaExtension: Failed to load library $libName: ${e.message}")
                    createGenericLibrary()
                }
            }
        })
    }


    private fun loadLibraryFromRemote(libraryName: String): LuaValue {
        return try {
            // Try to fetch from Shosetsu's GitLab repository
            val repoUrl = "https://gitlab.com/shosetsuorg/extensions/-/raw/dev/lib"
            val libraryUrl = "$repoUrl/$libraryName.lua"

            val url = URL(libraryUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 10000

            val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
            val libraryCode = reader.readText()
            reader.close()

            println("LuaExtension: Successfully loaded library $libraryName from remote")
            loadLibraryFromString(libraryCode)
        } catch (e: Exception) {
            println("LuaExtension: Failed to load $libraryName remotely: ${e.message}")
            // Fallback to local implementations
            when (libraryName) {
                "url" -> loadLibraryFromString(getUrlLibraryCode())
                "dkjson" -> loadLibraryFromString(getDkJsonLibraryCode())
                "Madara" -> loadLibraryFromString(getMadaraLibraryCode())
                else -> createGenericLibrary()
            }
        }
    }

    private fun loadLibraryFromString(libraryCode: String): LuaValue {
        return try {
            val chunk = globals.load(libraryCode)
            chunk.call()
        } catch (e: Exception) {
            println("LuaExtension: Failed to load library: ${e.message}")
            LuaValue.tableOf()
        }
    }

    private fun setupShosetsuGlobals() {
        // Add all missing Shosetsu global functions and constants

        // Constants
        globals.set("PAGE", LuaValue.valueOf(0))
        globals.set("QUERY", LuaValue.valueOf(1))

        // Add ChapterType constants
        val chapterType = LuaValue.tableOf()
        chapterType.set("HTML", LuaValue.valueOf(0))
        chapterType.set("STRING", LuaValue.valueOf(1))
        globals.set("ChapterType", chapterType)

        // Add NovelStatus constants  
        val novelStatus = LuaValue.tableOf()
        novelStatus.set("PUBLISHING", LuaValue.valueOf(0))
        novelStatus.set("COMPLETED", LuaValue.valueOf(1))
        novelStatus.set("PAUSED", LuaValue.valueOf(2))
        novelStatus.set("UNKNOWN", LuaValue.valueOf(3))
        globals.set("NovelStatus", novelStatus)

        // HTTP and Document functions
        globals.set("GETDocument", object : OneArgFunction() {
            override fun call(url: LuaValue): LuaValue {
                println("LuaExtension: GETDocument called with: ${url.tojstring()}")
                return createMockDocument()
            }
        })

        globals.set("RequestDocument", object : OneArgFunction() {
            override fun call(request: LuaValue): LuaValue {
                println("LuaExtension: RequestDocument called")
                return createMockDocument()
            }
        })

        globals.set("Request", object : OneArgFunction() {
            override fun call(request: LuaValue): LuaValue {
                val response = tableOf()
                response.set("body", object : ZeroArgFunction() {
                    override fun call(): LuaValue {
                        val body = tableOf()
                        body.set("string", object : ZeroArgFunction() {
                            override fun call(): LuaValue {
                                return valueOf("Mock response body")
                            }
                        })
                        return body
                    }
                })
                response.set("headers", object : ZeroArgFunction() {
                    override fun call(): LuaValue {
                        val headers = tableOf()
                        headers.set("get", object : OneArgFunction() {
                            override fun call(headerName: LuaValue): LuaValue {
                                return valueOf("application/json")
                            }
                        })
                        return headers
                    }
                })
                return response
            }
        })

        globals.set("GET", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                val url = if (args.narg() > 0) args.arg1().tojstring() else ""
                println("LuaExtension: GET request to: $url")
                return tableOf()
            }
        })

        globals.set("POST", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                val url = if (args.narg() > 0) args.arg1().tojstring() else ""
                println("LuaExtension: POST request to: $url")
                return tableOf()
            }
        })

        // Form body builder
        globals.set("FormBodyBuilder", object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val builder = tableOf()
                val params = mutableMapOf<String, String>()

                builder.set("add", object : TwoArgFunction() {
                    override fun call(key: LuaValue, value: LuaValue): LuaValue {
                        params[key.tojstring()] = value.tojstring()
                        return builder
                    }
                })

                builder.set("build", object : ZeroArgFunction() {
                    override fun call(): LuaValue {
                        val body = tableOf()
                        body.set("params", tableOf())
                        return body
                    }
                })

                return builder
            }
        })

        // Data structures
        globals.set("Novel", object : OneArgFunction() {
            override fun call(table: LuaValue): LuaValue {
                return table
            }
        })

        globals.set("NovelInfo", object : OneArgFunction() {
            override fun call(table: LuaValue): LuaValue {
                if (table.istable()) {
                    // Add setter methods
                    table.set("setChapters", object : OneArgFunction() {
                        override fun call(chapters: LuaValue): LuaValue {
                            table.set("chapters", chapters)
                            return NIL
                        }
                    })
                    table.set("setStatus", object : OneArgFunction() {
                        override fun call(status: LuaValue): LuaValue {
                            table.set("status", status)
                            return NIL
                        }
                    })
                    table.set("setAuthors", object : OneArgFunction() {
                        override fun call(authors: LuaValue): LuaValue {
                            table.set("authors", authors)
                            return NIL
                        }
                    })
                    table.set("setArtists", object : OneArgFunction() {
                        override fun call(artists: LuaValue): LuaValue {
                            table.set("artists", artists)
                            return NIL
                        }
                    })
                    table.set("setGenres", object : OneArgFunction() {
                        override fun call(genres: LuaValue): LuaValue {
                            table.set("genres", genres)
                            return NIL
                        }
                    })
                }
                return table
            }
        })

        globals.set("NovelChapter", object : OneArgFunction() {
            override fun call(table: LuaValue): LuaValue {
                return table
            }
        })

        // Utility functions
        globals.set("map", object : TwoArgFunction() {
            override fun call(collection: LuaValue, mapper: LuaValue): LuaValue {
                if (!collection.istable() || !mapper.isfunction()) {
                    return tableOf()
                }

                val result = tableOf()
                val table = collection.checktable()
                var resultIndex = 1

                var i = 1
                while (true) {
                    val item = table.get(i)
                    if (item.isnil()) break

                    val mappedItem = mapper.call(item, valueOf(i - 1))
                    if (!mappedItem.isnil()) {
                        result.set(resultIndex++, mappedItem)
                    }
                    i++
                }

                return result
            }
        })

        globals.set("mapNotNil", object : TwoArgFunction() {
            override fun call(collection: LuaValue, mapper: LuaValue): LuaValue {
                if (!collection.istable() || !mapper.isfunction()) {
                    return tableOf()
                }

                val result = tableOf()
                val table = collection.checktable()
                var resultIndex = 1

                var i = 1
                while (true) {
                    val item = table.get(i)
                    if (item.isnil()) break

                    val mappedItem = mapper.call(item, valueOf(i - 1))
                    if (!mappedItem.isnil()) {
                        result.set(resultIndex++, mappedItem)
                    }
                    i++
                }

                return result
            }
        })

        globals.set("AsList", object : OneArgFunction() {
            override fun call(table: LuaValue): LuaValue {
                return table // Just return the table for now
            }
        })

        globals.set("Reverse", object : OneArgFunction() {
            override fun call(table: LuaValue): LuaValue {
                if (!table.istable()) return table

                val original = table.checktable()
                val size = original.length()
                val reversed = tableOf()

                for (i in 1..size) {
                    reversed.set(i, original.get(size - i + 1))
                }

                return reversed
            }
        })

        globals.set("Document", object : OneArgFunction() {
            override fun call(html: LuaValue): LuaValue {
                return createMockDocument()
            }
        })

        globals.set("pageOfElem", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                val content = if (args.narg() > 0) args.arg1() else valueOf("")
                return valueOf("Mock page content: ${content.tojstring()}")
            }
        })

        // Filter functions  
        globals.set("DropdownFilter", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                return tableOf()
            }
        })

        globals.set("TextFilter", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                return tableOf()
            }
        })

        globals.set("CheckboxFilter", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                return tableOf()
            }
        })

        globals.set("FilterGroup", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                return tableOf()
            }
        })

        globals.set("Listing", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                return tableOf()
            }
        })

        // wrap function for Madara compatibility
        globals.set("wrap", object : TwoArgFunction() {
            override fun call(self: LuaValue, func: LuaValue): LuaValue {
                if (!func.isfunction()) return func

                return object : VarArgFunction() {
                    override fun invoke(args: Varargs): LuaValue {
                        return func.invoke(args) as LuaValue
                    }
                }
            }
        })
    }

    private fun getUrlLibraryCode(): String {
        return """
        -- {"ver":"1.0.0","author":"TechnoJo4"}
        
        local gsub, format, byte, char =
        string.gsub, string.format, string.byte, string.char
        
        ---@return string
        ---@param str string
        local function urlDecode(str)
            if str == nil or str == "" then
                return ""
            end
            str = gsub(str, '+', ' ')
            str = gsub(str, '%%(%x%x)', function(h)
                return char(tonumber(h, 16))
            end)
            str = gsub(str, '\r\n', '\n')
            return str
        end
        
        local encode_tbl = {["_"] = true, ["-"] = true, ["~"] = true, ["."] = true}
        for b=byte('a'),byte('z') do encode_tbl[char(b)] = true end
        for b=byte('A'),byte('Z') do encode_tbl[char(b)] = true end
        for b=byte('0'),byte('9') do encode_tbl[char(b)] = true end
        
        ---@return string
        ---@param str string
        local function urlEncode(str)
            if str == nil or str == "" then
                return ""
            end
            if str then
                str = gsub(str, '\n', '\r\n')
                str = gsub(str, '(.)', function(c)
                    if encode_tbl[c] then
                        return c
                    else
                        return format('%%%02X', byte(c))
                    end
                end)
            end
            return str
        end
        
        --- Makes a query string from a table.
        ---@return string
        ---@param tbl table<string, any|any[]>
        ---@param url string | nil
        local function querystring(tbl, url)
            local fields = {}
            for key, value in pairs(tbl) do
                local keyString = urlEncode(tostring(key)) .. "="
                if type(value) == "table" then
                    for _, v in ipairs(value) do
                        table.insert(fields, keyString .. urlEncode(tostring(v)))
                    end
                else
                    table.insert(fields, keyString .. urlEncode(tostring(value)))
                end
            end
            return (url and url .. "?" or "") .. table.concat(fields, "&")
        end
        
        return {
            decode = urlDecode,
            encode = urlEncode,
            querystring = querystring,
        }
        """.trimIndent()
    }

    private fun getDkJsonLibraryCode(): String {
        return """
        -- Basic JSON library based on dkjson
        local json = { version = "dkjson 2.5" }
        
        -- Simple JSON decode function
        function json.decode(str)
            if not str or str == "" then
                return {}
            end
            
            -- Very basic JSON parsing - replace with proper implementation
            -- This is a simplified version for basic compatibility
            return {}
        end
        
        -- Simple JSON encode function  
        function json.encode(obj)
            if type(obj) == "table" then
                local result = {}
                for k, v in pairs(obj) do
                    table.insert(result, tostring(k) .. ":" .. tostring(v))
                end
                return "{" .. table.concat(result, ",") .. "}"
            else
                return tostring(obj)
            end
        end
        
        -- HTTP functions for Shosetsu compatibility
        function json.GET(url, ...)
            -- Mock HTTP GET that returns empty table
            return {}
        end
        
        function json.POST(url, body, ...)
            -- Mock HTTP POST that returns empty table  
            return {}
        end
        
        return json
        """.trimIndent()
    }

    private fun getMadaraLibraryCode(): String {
        return """
        -- Basic Madara library implementation
        local defaults = {
            latestNovelSel = "div.col-12.col-md-6",
            searchNovelSel = "div.c-tabs-item__content",
            novelListingURLPath = "novel",
            novelPageTitleSel = "div.post-title",
            shrinkURLNovel = "novel",
            searchHasOper = false,
            hasCloudFlare = false,
            hasSearch = true,
            chapterType = ChapterType.HTML,
            chaptersOrderReversed = true,
            chaptersScriptLoaded = true,
            chaptersListSelector = "li.wp-manga-chapter",
            ajaxUsesFormData = false,
            ajaxFormDataSel = "a.wp-manga-action-button",
            ajaxFormDataAttr = "data-post",
            ajaxFormDataUrl = "/wp-admin/admin-ajax.php",
            ajaxSeriesUrl = "ajax/chapters/",
            isSearchIncrementing = true,
            customStyle = "",
        }
        
        function defaults:latest(data)
            -- Mock implementation
            return {}
        end
        
        function defaults:search(data)
            -- Mock implementation
            return {}
        end
        
        function defaults:parseNovel(url, loadChapters)
            -- Mock implementation - return basic novel info
            return {
                title = "Sample Novel",
                author = "Sample Author",
                description = "Sample description",
                imageURL = "",
                status = "PUBLISHING",
                chapters = {}
            }
        end
        
        function defaults:getPassage(url)
            -- Mock implementation
            return "Sample chapter content"
        end
        
        function defaults:expandURL(url)
            return self.baseURL .. "/" .. self.shrinkURLNovel .. "/" .. url
        end
        
        function defaults:shrinkURL(url)
            return url:gsub("https?://.-/" .. self.shrinkURLNovel .. "/", "")
        end
        
        return function(baseURL, _self)
            _self = _self or {}
            setmetatable(_self, { __index = defaults })
            _self.baseURL = baseURL
            _self.genres_map = {}
            _self.searchFilters = {}
            _self.listings = {}
            return _self
        end
        """.trimIndent()
    }

    private fun createUtf8Library(): LuaValue {
        val utf8Lib = LuaValue.tableOf()
        utf8Lib.set("char", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                val result = StringBuilder()
                for (i in 1..args.narg()) {
                    val codepoint = args.arg(i).toint()
                    if (codepoint < 128) {
                        result.append(codepoint.toChar())
                    } else {
                        // Basic UTF-8 encoding for codepoints > 127
                        if (codepoint < 2048) {
                            result.append((192 + codepoint / 64).toChar())
                            result.append((128 + (codepoint % 64)).toChar())
                        } else {
                            result.append((224 + codepoint / 4096).toChar())
                            result.append((128 + ((codepoint % 4096) / 64)).toChar())
                            result.append((128 + (codepoint % 64)).toChar())
                        }
                    }
                }
                return valueOf(result.toString())
            }
        })
        return utf8Lib
    }

    private fun createGenericLibrary(): LuaValue {
        return LuaValue.tableOf()
    }


    private fun createMockDocument(): LuaValue {
        val doc = LuaValue.tableOf()

        doc.set("select", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val selectorStr = selector.tojstring()
                return when {
                    selectorStr.contains("smpnovel_list") -> createMockNovelList()
                    selectorStr.contains("p-eplist__sublist") -> createMockChapterList()
                    selectorStr.contains(".c-pager__item--last") -> createMockPager()
                    else -> tableOf()
                }
            }
        })

        doc.set("selectFirst", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val selectorStr = selector.tojstring()
                return when {
                    selectorStr.contains("novel_h") -> createMockElement("Sample Novel Title")
                    selectorStr.contains("read_button") -> createMockElement(
                        "",
                        href = "/novel/sample"
                    )

                    selectorStr.contains("p-novel__title") -> createMockElement("Sample Novel")
                    selectorStr.contains("p-novel__author") -> createMockElement("Sample Author")
                    selectorStr.contains("p-novel__summary") -> createMockElement("Sample description of the novel")
                    selectorStr.contains("p-novel__subtitle-episode") -> createMockElement("Chapter 1")
                    selectorStr.contains("p-novel__text") -> createMockChapterContent()
                    else -> createMockElement("Mock Text")
                }
            }
        })

        return doc
    }

    private fun createMockNovelList(): LuaValue {
        val list = LuaValue.tableOf()
        // Create a few mock novels
        for (i in 1..5) {
            val novel = LuaValue.tableOf()
            novel.set("selectFirst", object : OneArgFunction() {
                override fun call(selector: LuaValue): LuaValue {
                    val selectorStr = selector.tojstring()
                    return when {
                        selectorStr.contains("novel_h") -> createMockElement("Sample Novel $i")
                        selectorStr.contains("read_button") -> createMockElement(
                            "",
                            href = "/novel/sample$i"
                        )

                        else -> createMockElement("Mock")
                    }
                }
            })
            list.set(i, novel)
        }
        return list
    }

    private fun createMockChapterList(): LuaValue {
        val list = LuaValue.tableOf()
        // Create a few mock chapters
        for (i in 1..3) {
            val chapter = LuaValue.tableOf()
            chapter.set("selectFirst", object : OneArgFunction() {
                override fun call(selector: LuaValue): LuaValue {
                    val selectorStr = selector.tojstring()
                    return when {
                        selectorStr.contains("p-eplist__subtitle") -> createMockElement(
                            "Chapter $i",
                            href = "/chapter/$i"
                        )

                        selectorStr.contains("p-eplist__update") -> createMockElement("2024-01-0$i")
                        else -> createMockElement("Mock")
                    }
                }
            })
            list.set(i, chapter)
        }
        return list
    }

    private fun createMockPager(): LuaValue {
        val pager = LuaValue.tableOf()
        pager.set("attr", object : OneArgFunction() {
            override fun call(attrName: LuaValue): LuaValue {
                return if (attrName.tojstring() == "href") {
                    valueOf("?p=1")
                } else {
                    valueOf("")
                }
            }
        })
        return pager
    }

    private fun createMockElement(text: String, href: String = ""): LuaValue {
        val element = LuaValue.tableOf()
        element.set("text", object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return valueOf(text)
            }
        })
        element.set("attr", object : OneArgFunction() {
            override fun call(attrName: LuaValue): LuaValue {
                return when (attrName.tojstring()) {
                    "href" -> valueOf(href)
                    else -> valueOf("")
                }
            }
        })
        element.set("prepend", object : OneArgFunction() {
            override fun call(content: LuaValue): LuaValue {
                return NIL
            }
        })
        return element
    }

    private fun createMockChapterContent(): LuaValue {
        val element = createMockElement("Sample chapter content goes here...")
        element.set("prepend", object : OneArgFunction() {
            override fun call(content: LuaValue): LuaValue {
                // Mock prepend operation
                return NIL
            }
        })
        return element
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
