package io.nightfish.lightnovelreader.api

/**
 * API兼容性工具类
 * 提供用于检查插件API版本与宿主版本是否兼容的方法
 *
 * @since Api 2
 */
object ApiCompat {
    /**
     * 版本分组。按发布维护
     *
     * 需要调整时，修改此列表并随宿主发布
     */
    private val groups: List<Set<Int>> = listOf(
        setOf(1),
        setOf(2, 3),
        setOf(4)
    )

    private fun groupOf(v: Int): Int? =
        groups.indexOfFirst { v in it }.takeIf { it >= 0 }

    /**
     * 检查插件API版本是否被宿主支持
     *
     * 规则：
     * - 同组且 pluginApi <= hostApi 视为支持
     * - 不同组之间一律视为不兼容
     * - pluginApi > hostApi 视为不兼容
     *
     * @param pluginApi 插件声明使用的API版本
     * @param hostApi 宿主支持的API版本，默认为[ApiMetadata.API_VERSION]
     *
     * @return 插件是否与宿主兼容
     *
     * @since Api 2
     */
    fun isSupported(pluginApi: Int, hostApi: Int = ApiMetadata.API_VERSION): Boolean {
        val gh = groupOf(hostApi) ?: return false
        val gp = groupOf(pluginApi) ?: return false
        if (gh != gp) return false
        return pluginApi <= hostApi
    }
}