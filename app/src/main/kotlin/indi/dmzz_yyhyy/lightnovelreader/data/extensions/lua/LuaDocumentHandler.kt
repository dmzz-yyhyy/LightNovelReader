package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import org.luaj.vm2.LuaValue

/**
 * Handles document parsing and element creation for Lua extensions
 */
object LuaDocumentHandler {

    fun createMockElementList(selector: String): LuaValue {
        println("LuaExtension: Creating mock element list for selector: $selector")
        
        val elements = LuaValue.tableOf()
        
        when {
            // Search result selectors
            selector.contains("article") || selector.contains(".result") || 
            selector.contains(".novel-item") || selector.contains(".book-item") -> {
                // Create multiple mock elements for search results
                for (i in 1..3) {
                    val element = createMockElement("Sample Novel $i")
                    elements.set(i, element)
                }
            }
            
            // Chapter list selectors
            selector.contains("chapter") || selector.contains(".chapter-item") -> {
                for (i in 1..5) {
                    val element = createMockElement("Chapter $i")
                    elements.set(i, element)
                }
            }
            
            // Generic list
            else -> {
                val element = createMockElement("Sample Element")
                elements.set(1, element)
            }
        }
        
        return elements
    }

    fun createMockElement(
        text: String = "",
        href: String = "",
        src: String = "",
        selector: String = ""
    ): LuaValue {
        val element = LuaValue.tableOf()
        
        // Set basic properties
        element.set("text", LuaValue.valueOf(text))
        element.set("href", LuaValue.valueOf(href))
        element.set("src", LuaValue.valueOf(src))
        
        // Add selector-specific properties based on context
        when {
            selector.contains("title") || text.contains("Novel") -> {
                element.set("text", LuaValue.valueOf("Sample Novel Title"))
            }
            selector.contains("author") -> {
                element.set("text", LuaValue.valueOf("Sample Author"))
            }
            selector.contains("description") || selector.contains("summary") -> {
                element.set("text", LuaValue.valueOf("This is a sample novel description with interesting plot."))
            }
            selector.contains("img") -> {
                element.set("src", LuaValue.valueOf("/sample-cover.jpg"))
            }
            selector.contains("link") || selector.contains("href") -> {
                element.set("href", LuaValue.valueOf("/novel/sample"))
            }
        }
        
        // Add common element methods
        addElementMethods(element)
        
        return element
    }

    private fun addElementMethods(element: LuaValue) {
        // text() method
        element.set("text", object : org.luaj.vm2.lib.ZeroArgFunction() {
            override fun call(): LuaValue {
                return element.get("text")
            }
        })
        
        // attr(String) method
        element.set("attr", object : org.luaj.vm2.lib.OneArgFunction() {
            override fun call(attr: LuaValue): LuaValue {
                val attrName = attr.tojstring()
                return when (attrName) {
                    "href" -> element.get("href")
                    "src" -> element.get("src")
                    "title" -> element.get("text")
                    else -> LuaValue.valueOf("")
                }
            }
        })
        
        // select(String) method
        element.set("select", object : org.luaj.vm2.lib.OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                return createMockElementList(selector.tojstring())
            }
        })
        
        // selectFirst(String) method
        element.set("selectFirst", object : org.luaj.vm2.lib.OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                return createMockElement(selector = selector.tojstring())
            }
        })
    }
}
