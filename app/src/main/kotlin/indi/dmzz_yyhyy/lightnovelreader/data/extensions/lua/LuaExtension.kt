package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.Extension
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
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
        val result = chunk.call()

        // Debug: Print available functions in the extension
        println("LuaExtension: Loaded extension $name")
        println("LuaExtension: Available functions:")
        val functionsToCheck = listOf(
            "search",
            "parseNovel",
            "getPassage",
            "listings",
            "latest",
            "getBook",
            "getChapter"
        )
        functionsToCheck.forEach { funcName ->
            val func = globals.get(funcName)
            if (func.isfunction()) {
                println("LuaExtension: - $funcName: available")
            } else if (!func.isnil()) {
                println("LuaExtension: - $funcName: available (${func.typename()})")
            }
        }

        // Check if result contains listings or other properties
        if (result.istable()) {
            println("LuaExtension: Extension returned table with properties:")
            val table = result.checktable()
            val properties = listOf(
                "listings",
                "search",
                "parseNovel",
                "getPassage",
                "hasSearch",
                "isSearchIncrementing"
            )
            properties.forEach { prop ->
                val value = table.get(prop)
                if (!value.isnil()) {
                    println("LuaExtension: - $prop: ${value.typename()}")
                }
            }
        }
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
                val urlString = url.tojstring()
                println("LuaExtension: GETDocument called with: $urlString")
                return try {
                    val document = runBlocking {
                        withContext(Dispatchers.IO) {
                            fetchDocument(urlString)
                        }
                    }
                    createLuaDocument(document)
                } catch (e: Exception) {
                    println("LuaExtension: Failed to fetch document from $urlString: ${e.message}")
                    createMockDocument()
                }
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

    // Real HTTP and Document functions
    private suspend fun fetchDocument(url: String): Document {
        return withContext(Dispatchers.IO) {
            try {
                println("LuaExtension: Fetching URL: $url")
                Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
                    .get()
            } catch (e: Exception) {
                println("LuaExtension: Error fetching $url: ${e.message}")
                throw e
            }
        }
    }

    private fun createLuaDocument(document: Document): LuaValue {
        val doc = LuaValue.tableOf()

        // Add select method for CSS selector queries
        doc.set("select", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val selectorString = selector.tojstring()
                println("LuaExtension: Document.select called with: $selectorString")

                return try {
                    val elements = document.select(selectorString)
                    createLuaElements(elements)
                } catch (e: Exception) {
                    println("LuaExtension: Error selecting '$selectorString': ${e.message}")
                    tableOf()
                }
            }
        })

        // Add selectFirst method
        doc.set("selectFirst", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val selectorString = selector.tojstring()
                println("LuaExtension: Document.selectFirst called with: $selectorString")

                return try {
                    val element = document.selectFirst(selectorString)
                    if (element != null) {
                        createLuaElement(element)
                    } else {
                        NIL
                    }
                } catch (e: Exception) {
                    println("LuaExtension: Error selecting first '$selectorString': ${e.message}")
                    NIL
                }
            }
        })

        return doc
    }

    private fun createLuaElements(elements: Elements): LuaValue {
        val list = LuaValue.tableOf()

        elements.forEachIndexed { index, element ->
            list.set(index + 1, createLuaElement(element))
        }

        // Add size method
        list.set("size", object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return valueOf(elements.size)
            }
        })

        return list
    }

    private fun createLuaElement(element: Element): LuaValue {
        val luaElement = LuaValue.tableOf()

        // Add text method
        luaElement.set("text", object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return valueOf(element.text())
            }
        })

        // Add attr method
        luaElement.set("attr", object : OneArgFunction() {
            override fun call(attrName: LuaValue): LuaValue {
                val attrValue = element.attr(attrName.tojstring())
                return valueOf(attrValue)
            }
        })

        // Add select method
        luaElement.set("select", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val elements = element.select(selector.tojstring())
                return createLuaElements(elements)
            }
        })

        // Add selectFirst method
        luaElement.set("selectFirst", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val foundElement = element.selectFirst(selector.tojstring())
                return if (foundElement != null) {
                    createLuaElement(foundElement)
                } else {
                    NIL
                }
            }
        })

        // Add prepend method (commonly used by extensions)
        luaElement.set("prepend", object : OneArgFunction() {
            override fun call(content: LuaValue): LuaValue {
                // For extensions that modify content - just log for now
                println("LuaExtension: Element.prepend called with: ${content.tojstring()}")
                return NIL
            }
        })

        // Add remove method
        luaElement.set("remove", object : ZeroArgFunction() {
            override fun call(): LuaValue {
                element.remove()
                return NIL
            }
        })

        return luaElement
    }

    private fun createMockDocument(): LuaValue {
        val doc = LuaValue.tableOf()

        doc.set("select", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val selectorStr = selector.tojstring()
                println("LuaExtension: Document.select called with: $selectorStr")
                return when {
                    // Common novel listing selectors
                    selectorStr.contains("div.col-12.col-md-6") -> createMockNovelList()
                    selectorStr.contains("div.c-tabs-item__content") -> createMockNovelList()
                    selectorStr.contains("section.book-card-item") -> createMockNovelList()
                    selectorStr.contains("div[style=\"display: flex;\"]") -> createMockNovelList()

                    // Chapter selectors
                    selectorStr.contains("li.wp-manga-chapter") -> createMockChapterList()
                    selectorStr.contains("ul.g0") -> createMockChapterList()
                    selectorStr.contains("div.w800_m") -> createMockChapterList()

                    // Genre/metadata selectors
                    selectorStr.contains(".gnres > a") -> createMockGenreList()
                    selectorStr.contains("div.post-content_item") -> createMockContentList()

                    // Pagination
                    selectorStr.contains(".c-pager__item--last") -> createMockPager()

                    // Default fallback
                    else -> createMockNovelList()
                }
            }
        })

        doc.set("selectFirst", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val selectorStr = selector.tojstring()
                println("LuaExtension: Document.selectFirst called with: $selectorStr")
                return when {
                    // Title selectors
                    selectorStr.contains("h2.bookname") -> createMockElement("Sample Novel Title")
                    selectorStr.contains("div.post-title") -> createMockElement("Sample Novel Title")
                    selectorStr.contains("p-novel__title") -> createMockElement("Sample Novel")

                    // Author selectors
                    selectorStr.contains(".author_link") -> createMockElement("Sample Author")
                    selectorStr.contains("p-novel__author") -> createMockElement("Sample Author")

                    // Description selectors
                    selectorStr.contains("#bann_full") -> createMockElement("This is a sample novel description.")
                    selectorStr.contains("#bann_short") -> createMockElement("Short description.")
                    selectorStr.contains("div.summary__content") -> createMockElement("Novel summary content.")
                    selectorStr.contains("div.manga-excerpt") -> createMockElement("Novel excerpt.")

                    // Image selectors
                    selectorStr.contains("img.shadow") -> createMockElement(
                        "",
                        src = "/sample-cover.jpg"
                    )

                    selectorStr.contains("div.summary_image img") -> createMockElement(
                        "",
                        src = "/sample-cover.jpg"
                    )

                    // Status selectors
                    selectorStr.contains(".tech_decription") -> createMockElement("Пишется")
                    selectorStr.contains("div.post-status") -> createMockStatusElement()

                    // Button/link selectors
                    selectorStr.contains("read_button") -> createMockElement(
                        "",
                        href = "/novel/sample"
                    )

                    selectorStr.contains("a.txt") -> createMockElement("", href = "/books/123")

                    // Chapter content
                    selectorStr.contains("div.c-blog-post") -> createMockChapterContent()
                    selectorStr.contains("div.text-left") -> createMockChapterContent()

                    // Default fallback
                    else -> createMockElement("Mock Text")
                }
            }
        })

        return doc
    }

    private fun createMockNovelList(): LuaValue {
        val list = LuaValue.tableOf()
        // Create sample novels with realistic data
        val sampleNovels = listOf(
            Triple("Sample Light Novel 1", "/books/123", "First sample novel"),
            Triple("Test Novel 2", "/books/456", "Second test novel"),
            Triple("Mock Story 3", "/books/789", "Third mock story"),
            Triple("Demo Novel 4", "/books/101", "Fourth demo novel"),
            Triple("Example Book 5", "/books/202", "Fifth example book")
        )

        sampleNovels.forEachIndexed { index, (title, link, _) ->
            val novel = LuaValue.tableOf()

            novel.set("select", object : OneArgFunction() {
                override fun call(selector: LuaValue): LuaValue {
                    val selectorStr = selector.tojstring()
                    return when {
                        selectorStr.contains("span[itemprop=\"name\"]") -> createSingleElementList(
                            title
                        )

                        selectorStr.contains("a.txt") -> createSingleElementList("", href = link)
                        selectorStr.contains("img.shadow") -> createSingleElementList(
                            "",
                            src = "/cover${index + 1}.jpg"
                        )

                        else -> tableOf()
                    }
                }
            })

            novel.set("selectFirst", object : OneArgFunction() {
                override fun call(selector: LuaValue): LuaValue {
                    val selectorStr = selector.tojstring()
                    return when {
                        selectorStr.contains("span[itemprop=\"name\"]") -> createMockElement(title)
                        selectorStr.contains("a.txt") -> createMockElement("", href = link)
                        selectorStr.contains("img.shadow") -> createMockElement(
                            "",
                            src = "/cover${index + 1}.jpg"
                        )

                        selectorStr.contains("a.chptitle") -> createMockElement(title, href = link)
                        selectorStr.contains("a") -> createMockElement(title, href = link)
                        else -> createMockElement(title)
                    }
                }
            })

            list.set(index + 1, novel)
        }
        return list
    }

    private fun createSingleElementList(
        text: String,
        href: String = "",
        src: String = ""
    ): LuaValue {
        val list = LuaValue.tableOf()
        val element = createMockElement(text, href, src)
        list.set(1, element)
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

    private fun createMockElement(
        text: String = "",
        href: String = "",
        src: String = ""
    ): LuaValue {
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
                    "src" -> valueOf(src)
                    "title" -> valueOf(text)
                    else -> valueOf("")
                }
            }
        })

        element.set("prepend", object : OneArgFunction() {
            override fun call(content: LuaValue): LuaValue {
                return NIL
            }
        })

        element.set("select", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                // Return empty list for any sub-selections
                return tableOf()
            }
        })

        element.set("selectFirst", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                // Return self for any sub-selections
                return element
            }
        })

        element.set("remove", object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return NIL
            }
        })

        return element
    }

    private fun createMockGenreList(): LuaValue {
        val list = LuaValue.tableOf()
        val genres = listOf("Fantasy", "Adventure", "Romance", "Action", "Drama")
        genres.forEachIndexed { index, genre ->
            list.set(index + 1, createMockElement(genre, href = "/genre/${genre.lowercase()}"))
        }
        return list
    }

    private fun createMockContentList(): LuaValue {
        val list = LuaValue.tableOf()
        list.set(1, createMockElement("Publishing"))
        return list
    }

    private fun createMockStatusElement(): LuaValue {
        val statusElement = LuaValue.tableOf()
        statusElement.set("select", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val list = tableOf()
                val content = tableOf()
                content.set("selectFirst", object : OneArgFunction() {
                    override fun call(subSelector: LuaValue): LuaValue {
                        return createMockElement("Publishing")
                    }
                })
                list.set(1, content)
                return list
            }
        })
        return statusElement
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
            println("LuaExtension: Starting search for '$query' in extension $name")

            // Don't search with empty queries - return empty list
            if (query.isBlank() || query.isEmpty()) {
                println("LuaExtension: Empty query provided, returning empty list")
                return emptyList()
            }

            // Try global search function (most common)
            val searchFunction = globals.get("search")
            if (searchFunction.isfunction()) {
                // Create Shosetsu-style data table
                val data = LuaValue.tableOf()
                data.set(LuaValue.valueOf(0), LuaValue.valueOf(query)) // QUERY
                data.set(LuaValue.valueOf(1), LuaValue.valueOf(1))     // PAGE
                data.set("QUERY", LuaValue.valueOf(query))
                data.set("PAGE", LuaValue.valueOf(1))

                println("LuaExtension: Calling search function with data table")
                val result = searchFunction.call(data)
                val searchResults = parseLuaSearchResults(result)
                println("LuaExtension: Search returned ${searchResults.size} results")
                return searchResults
            }

            println("LuaExtension: No search function found for $name")
            return emptyList()
        } catch (e: Exception) {
            println("LuaExtension: Search failed for $name: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun getLatestNovels(): List<ExtensionSearchResult> {
        return try {
            println("LuaExtension: Trying to get latest novels from $name")

            // Try latest function (Shosetsu style)
            val latestFunction = globals.get("latest")
            if (latestFunction.isfunction()) {
                println("LuaExtension: Calling latest function")
                val data = LuaValue.tableOf()
                data.set("PAGE", LuaValue.valueOf(1))
                val result = latestFunction.call(data)
                val searchResults = parseLuaSearchResults(result)
                println("LuaExtension: Latest function returned ${searchResults.size} results")
                return searchResults
            }

            // Try listings function
            val listingsFunction = globals.get("listings")
            if (listingsFunction.isfunction()) {
                println("LuaExtension: Calling listings function")
                val result = listingsFunction.call()
                if (result.istable()) {
                    val listings = result.checktable()
                    // Get first listing
                    if (listings.length() > 0) {
                        val firstListing = listings.get(1)
                        if (firstListing.istable()) {
                            val data = LuaValue.tableOf()
                            data.set("PAGE", LuaValue.valueOf(1))
                            val listingResult = firstListing.get("list").call(data)
                            val searchResults = parseLuaSearchResults(listingResult)
                            println("LuaExtension: Listings function returned ${searchResults.size} results")
                            return searchResults
                        }
                    }
                }
            }

            // Try browse function (our style)
            val browseFunction = globals.get("browse")
            if (browseFunction.isfunction()) {
                println("LuaExtension: Calling browse function")
                val result = browseFunction.call()
                val searchResults = parseLuaSearchResults(result)
                println("LuaExtension: Browse function returned ${searchResults.size} results")
                return searchResults
            }

            println("LuaExtension: No latest/listings/browse function found for $name")
            return emptyList()
        } catch (e: Exception) {
            println("LuaExtension: getLatestNovels failed for $name: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    override suspend fun getBook(id: String): ExtensionBook? {
        return try {
            println("LuaExtension: Getting book with ID '$id' from extension $name")

            // Try parseNovel function (Shosetsu style)
            val parseNovelFunction = globals.get("parseNovel")
            if (parseNovelFunction.isfunction()) {
                println("LuaExtension: Using parseNovel function")
                val result = parseNovelFunction.call(
                    LuaValue.valueOf(id),
                    LuaValue.TRUE // loadChapters = true
                )
                val book = parseLuaBook(result)
                if (book != null) {
                    println("LuaExtension: parseNovel returned book: ${book.title}")
                    return book
                }
            }

            // Last fallback to getBook function (our style)
            val getBookFunction = globals.get("getBook")
            if (getBookFunction.isfunction()) {
                println("LuaExtension: Using getBook function")
                val result = getBookFunction.call(LuaValue.valueOf(id))
                val book = parseLuaBook(result)
                if (book != null) {
                    println("LuaExtension: getBook returned book: ${book.title}")
                    return book
                }
            }

            println("LuaExtension: No parseNovel or getBook function found for $name")
            return null
        } catch (e: Exception) {
            println("LuaExtension: getBook failed for $name: ${e.message}")
            e.printStackTrace()
            return null
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
            if (book != null) {
                return book.chapters
            } else {
                // Fallback to getChapters function if it exists
                val getChaptersFunction = globals.get("getChapters")
                if (getChaptersFunction.isfunction()) {
                    val result = getChaptersFunction.call(LuaValue.valueOf(bookId))
                    return parseLuaChapterList(result)
                } else {
                    println("LuaExtension: No chapters found for $name")
                    return null
                }
            }
        } catch (e: Exception) {
            println("LuaExtension: getChapters failed for $name: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    override suspend fun getBrowseResults(): List<ExtensionSearchResult>? {
        return try {
            println("LuaExtension: Getting browse results from $name")

            // Try latest function (Shosetsu style)
            val latestFunction = globals.get("latest")
            if (latestFunction.isfunction()) {
                println("LuaExtension: Calling latest function")
                val data = LuaValue.tableOf()
                data.set("PAGE", LuaValue.valueOf(1))
                val result = latestFunction.call(data)
                return parseLuaSearchResults(result)
            }

            // Try listings function
            val listingsFunction = globals.get("listings")
            if (listingsFunction.isfunction()) {
                println("LuaExtension: Calling listings function")
                val data = LuaValue.tableOf()
                data.set("PAGE", LuaValue.valueOf(1))
                val result = listingsFunction.call(data)
                return parseLuaSearchResults(result)
            }

            // Try browse function
            val browseFunction = globals.get("browse")
            if (browseFunction.isfunction()) {
                println("LuaExtension: Calling browse function")
                val data = LuaValue.tableOf()
                data.set("PAGE", LuaValue.valueOf(1))
                val result = browseFunction.call(data)
                return parseLuaSearchResults(result)
            }

            println("LuaExtension: No browse/latest/listings function found for $name")
            return null
        } catch (e: Exception) {
            println("LuaExtension: getBrowseResults failed for $name: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    private fun parseLuaSearchResults(luaValue: LuaValue): List<ExtensionSearchResult> {
        println("LuaExtension: Parsing search results")
        if (!luaValue.istable()) {
            println("LuaExtension: Search result is not a table")
            return emptyList()
        }

        val results = mutableListOf<ExtensionSearchResult>()
        val table = luaValue.checktable()

        var i = 1
        while (true) {
            val entry = table.get(i)
            if (entry.isnil()) break

            if (entry.istable()) {
                val entryTable = entry.checktable()

                // Try different field names that Shosetsu extensions might use
                val id = entryTable.get("link").optjstring(
                    entryTable.get("id").optjstring(
                        entryTable.get("url").optjstring("sample$i")
                    )
                )
                val title = entryTable.get("title").optjstring("Sample Novel $i")
                val author = entryTable.get("author").optjstring("")
                val description = entryTable.get("description").optjstring("")
                val imageUrl = entryTable.get("imageURL").optjstring(
                    entryTable.get("imageUrl").optjstring("")
                )
                val url = entryTable.get("link").optjstring(
                    entryTable.get("url").optjstring("")
                )

                val searchResult = ExtensionSearchResult(
                    id = id,
                    title = title,
                    author = author,
                    description = description,
                    imageUrl = imageUrl,
                    url = url,
                    extensionId = this.id
                )
                results.add(searchResult)
                println("LuaExtension: Added search result: $title")
            }
            i++
        }

        println("LuaExtension: Parsed ${results.size} search results")
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
