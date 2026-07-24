package indi.dmzz_yyhyy.lightnovelreader.utils

import dalvik.system.PathClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginClassLoader

fun classLoader(
    dexPath: String,
    librarySearchPath: String?,
    parent: ClassLoader?
): PathClassLoader = PluginClassLoader(dexPath, librarySearchPath, parent)