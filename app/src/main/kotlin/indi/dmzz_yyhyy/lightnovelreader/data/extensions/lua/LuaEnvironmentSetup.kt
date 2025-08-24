package indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction
import org.luaj.vm2.lib.jse.JsePlatform

/**
 * Handles the setup of the Lua environment for extensions
 */
class LuaEnvironmentSetup {

    fun createGlobals(): Globals {
        val globals = JsePlatform.standardGlobals()
        
        // Set up Shosetsu-compatible environment
        setupRequireFunction(globals)
        setupUtilityFunctions(globals)
        setupDocumentFunctions(globals)
        setupHttpFunctions(globals)
        
        return globals
    }

    private fun setupRequireFunction(globals: Globals) {
        globals.set("Require", object : OneArgFunction() {
            override fun call(moduleName: LuaValue): LuaValue {
                val moduleNameStr = moduleName.tojstring()
                println("LuaExtension: Require called for module: $moduleNameStr")

                return when (moduleNameStr) {
                    "WrapperObject" -> createWrapperObjectModule()
                    "json" -> createJsonModule()
                    "Filters" -> createFiltersModule()
                    "common.lua" -> createCommonModule()
                    else -> {
                        println("LuaExtension: Unknown module: $moduleNameStr, returning empty table")
                        LuaValue.tableOf()
                    }
                }
            }
        })

        globals.set("require", globals.get("Require"))
    }

    private fun createWrapperObjectModule(): LuaValue {
        val module = LuaValue.tableOf()
        
        module.set("ShosetsuFilter", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                val filter = LuaValue.tableOf()
                if (args.narg() > 0) {
                    filter.set("name", args.arg1())
                    filter.set("id", if (args.narg() > 1) args.arg(2) else LuaValue.valueOf(""))
                    filter.set("state", if (args.narg() > 2) args.arg(3) else LuaValue.valueOf(0))
                }
                return filter
            }
        })

        module.set("NovelInfo", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                val info = LuaValue.tableOf()
                if (args.narg() > 0) {
                    info.set("title", args.arg1())
                    info.set("link", if (args.narg() > 1) args.arg(2) else LuaValue.valueOf(""))
                    info.set("imageURL", if (args.narg() > 2) args.arg(3) else LuaValue.valueOf(""))
                }
                return info
            }
        })

        return module
    }

    private fun createJsonModule(): LuaValue {
        val module = LuaValue.tableOf()
        
        module.set("parse", object : OneArgFunction() {
            override fun call(jsonStr: LuaValue): LuaValue {
                println("LuaExtension: JSON parse called")
                return LuaValue.tableOf()
            }
        })

        module.set("stringify", object : OneArgFunction() {
            override fun call(table: LuaValue): LuaValue {
                println("LuaExtension: JSON stringify called")
                return LuaValue.valueOf("{}")
            }
        })

        return module
    }

    private fun createFiltersModule(): LuaValue {
        val module = LuaValue.tableOf()
        
        module.set("Text", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                val filter = LuaValue.tableOf()
                filter.set("type", LuaValue.valueOf("text"))
                return filter
            }
        })

        module.set("Switch", object : VarArgFunction() {
            override fun invoke(args: Varargs): LuaValue {
                val filter = LuaValue.tableOf()
                filter.set("type", LuaValue.valueOf("switch"))
                return filter
            }
        })

        return module
    }

    private fun createCommonModule(): LuaValue {
        val module = LuaValue.tableOf()
        
        module.set("shrinkURL", object : OneArgFunction() {
            override fun call(url: LuaValue): LuaValue {
                return url
            }
        })

        module.set("expandURL", object : OneArgFunction() {
            override fun call(url: LuaValue): LuaValue {
                return url
            }
        })

        return module
    }

    private fun setupUtilityFunctions(globals: Globals) {
        // Map function
        globals.set("map", object : TwoArgFunction() {
            override fun call(list: LuaValue, func: LuaValue): LuaValue {
                if (!list.istable() || !func.isfunction()) {
                    return LuaValue.tableOf()
                }

                val result = LuaValue.tableOf()
                val table = list.checktable()
                var i = 1
                
                while (true) {
                    val item = table.get(i)
                    if (item.isnil()) break
                    
                    val mappedItem = func.call(item)
                    if (!mappedItem.isnil()) {
                        result.set(result.length() + 1, mappedItem)
                    }
                    i++
                }
                
                return result
            }
        })

        // MapNotNil function
        globals.set("mapNotNil", object : TwoArgFunction() {
            override fun call(list: LuaValue, func: LuaValue): LuaValue {
                return globals.get("map").call(list, func)
            }
        })

        // Filter function
        globals.set("filter", object : TwoArgFunction() {
            override fun call(list: LuaValue, predicate: LuaValue): LuaValue {
                if (!list.istable() || !predicate.isfunction()) {
                    return LuaValue.tableOf()
                }

                val result = LuaValue.tableOf()
                val table = list.checktable()
                var i = 1
                
                while (true) {
                    val item = table.get(i)
                    if (item.isnil()) break
                    
                    if (predicate.call(item).toboolean()) {
                        result.set(result.length() + 1, item)
                    }
                    i++
                }
                
                return result
            }
        })
    }

    private fun setupDocumentFunctions(globals: Globals) {
        // Document mock
        val document = LuaValue.tableOf()
        
        document.set("select", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val selectorStr = selector.tojstring()
                println("LuaExtension: Document.select called with: $selectorStr")
                return LuaDocumentHandler.createMockElementList(selectorStr)
            }
        })

        document.set("selectFirst", object : OneArgFunction() {
            override fun call(selector: LuaValue): LuaValue {
                val selectorStr = selector.tojstring()
                println("LuaExtension: Document.selectFirst called with: $selectorStr")
                return LuaDocumentHandler.createMockElement(selectorStr)
            }
        })

        globals.set("Document", document)
    }

    private fun setupHttpFunctions(globals: Globals) {
        // HTTP mock
        globals.set("GET", object : OneArgFunction() {
            override fun call(url: LuaValue): LuaValue {
                val urlStr = url.tojstring()
                println("LuaExtension: GET called for URL: $urlStr")
                return LuaValue.valueOf("<html><body>Mock response</body></html>")
            }
        })

        globals.set("POST", object : TwoArgFunction() {
            override fun call(url: LuaValue, data: LuaValue): LuaValue {
                val urlStr = url.tojstring()
                println("LuaExtension: POST called for URL: $urlStr")
                return LuaValue.valueOf("<html><body>Mock response</body></html>")
            }
        })
    }
}
