package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import dalvik.system.PathClassLoader

class PluginClassLoader(
    dexPath: String,
    librarySearchPath: String?,
    parent: ClassLoader?,
    private val sharedPrefixes: List<String> = DEFAULT_SHARED_PREFIXES
) : PathClassLoader(dexPath, librarySearchPath, parent) {

    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        findLoadedClass(name)?.let { return it }

        if (sharedPrefixes.any { name.startsWith(it) }) {
            val p = parent
            if (p != null) {
                try { return p.loadClass(name) } catch (_: ClassNotFoundException) { }
            }
            return findClass(name)
        }

        try {
            return findClass(name)
        } catch (_: ClassNotFoundException) { }

        val p = parent
        if (p != null) return p.loadClass(name)
        throw ClassNotFoundException(name)
    }

    companion object {
        val DEFAULT_SHARED_PREFIXES = listOf(
            "kotlin.",
            "kotlinx.",
            "androidx.",
            "j$.",
            "io.nightfish.lightnovelreader.api.",
        )
    }
}
