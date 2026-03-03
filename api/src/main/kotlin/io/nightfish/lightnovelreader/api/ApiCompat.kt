package io.nightfish.lightnovelreader.api

object ApiCompat {
    /**
     * 版本分组。按发布维护
     *
     * 需要调整时，修改此列表并随宿主发布
     */
    private val groups: List<Set<Int>> = listOf(
        setOf(1),
        setOf(2)
    )

    private fun groupOf(v: Int): Int? =
        groups.indexOfFirst { v in it }.takeIf { it >= 0 }

    /**
     * 规则：
     * - 同组且 pluginApi <= hostApi 视为支持
     * - 不同组之间一律视为不兼容
     * - pluginApi > hostApi 视为不兼容
     */
    fun isSupported(pluginApi: Int, hostApi: Int = ApiMetadata.API_VERSION): Boolean {
        val gh = groupOf(hostApi) ?: return false
        val gp = groupOf(pluginApi) ?: return false
        if (gh != gp) return false
        return pluginApi <= hostApi
    }
}