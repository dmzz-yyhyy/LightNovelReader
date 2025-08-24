package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionBook
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionChapter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.model.ExtensionSearchResult
import org.luaj.vm2.LuaValue

/**
 * Handles parsing of Lua values into extension data structures
 */
object LuaResultParser {

    fun parseSearchResults(luaValue: LuaValue, extensionId: String): List<ExtensionSearchResult> {
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
                    extensionId = extensionId
                )
                results.add(searchResult)
                println("LuaExtension: Added search result: $title")
            }
            i++
        }

        println("LuaExtension: Parsed ${results.size} search results")
        return results
    }

    fun parseBook(luaValue: LuaValue): ExtensionBook? {
        if (!luaValue.istable()) return null

        val table = luaValue.checktable()

        val id = table.get("id").optjstring(
            table.get("link").optjstring("sample_book")
        )
        val title = table.get("title").optjstring("Sample Book")
        val author = table.get("author").optjstring("Sample Author")
        val description = table.get("description").optjstring("Sample description")
        val imageUrl = table.get("imageURL").optjstring(
            table.get("imageUrl").optjstring("")
        )
        val url = table.get("link").optjstring(
            table.get("url").optjstring("")
        )

        // Parse chapters if available
        val chapters = mutableListOf<ExtensionChapter>()
        val chaptersValue = table.get("chapters")
        if (chaptersValue.istable()) {
            val chaptersTable = chaptersValue.checktable()
            var i = 1
            while (true) {
                val chapterValue = chaptersTable.get(i)
                if (chapterValue.isnil()) break

                if (chapterValue.istable()) {
                    val chapterTable = chapterValue.checktable()
                    val chapter = ExtensionChapter(
                        id = chapterTable.get("id").optjstring("$i"),
                        title = chapterTable.get("title").optjstring("Chapter $i"),
                        url = chapterTable.get("link").optjstring(""),
                        content = chapterTable.get("content").optjstring("")
                    )
                    chapters.add(chapter)
                }
                i++
            }
        }

        return ExtensionBook(
            id = id,
            title = title,
            author = author,
            description = description,
            imageUrl = imageUrl,
            url = url,
            chapters = chapters
        )
    }

    fun parseChapterList(luaValue: LuaValue): List<ExtensionChapter>? {
        if (!luaValue.istable()) return null

        val chapters = mutableListOf<ExtensionChapter>()
        val table = luaValue.checktable()

        var i = 1
        while (true) {
            val chapterValue = table.get(i)
            if (chapterValue.isnil()) break

            if (chapterValue.istable()) {
                val chapterTable = chapterValue.checktable()
                val chapter = ExtensionChapter(
                    id = chapterTable.get("id").optjstring("$i"),
                    title = chapterTable.get("title").optjstring("Chapter $i"),
                    url = chapterTable.get("link").optjstring(""),
                    content = chapterTable.get("content").optjstring("")
                )
                chapters.add(chapter)
            }
            i++
        }

        return chapters
    }

    fun parseChapter(luaValue: LuaValue): ExtensionChapter? {
        if (!luaValue.istable()) return null

        val table = luaValue.checktable()

        return ExtensionChapter(
            id = table.get("id").optjstring("sample_chapter"),
            title = table.get("title").optjstring("Sample Chapter"),
            url = table.get("link").optjstring(""),
            content = table.get("content").optjstring("Sample chapter content")
        )
    }

    fun parseStringList(luaValue: LuaValue): List<String> {
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
