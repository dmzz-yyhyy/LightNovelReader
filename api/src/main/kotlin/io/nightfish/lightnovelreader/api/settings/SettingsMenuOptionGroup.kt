package io.nightfish.lightnovelreader.api.settings

/**
 * 设置项菜单选项容器
 *
 * @since Api 4
 */
interface SettingsMenuOptionGroup {
    /** 全部选项列表 */
    val optionList: List<SettingsMenuOption>

    /**
     * 按照键值获取选项, 若不存在则返回 null
     *
     * @param key 选项的唯一标识键
     */
    fun getOrNull(key: String): SettingsMenuOption? =
        optionList.firstOrNull { it.key == key }

    /**
     * 按照键值获取选项, 若不存在则抛出 [NoSuchElementException]
     *
     * @param key 选项的唯一标识键
     */
    fun get(key: String): SettingsMenuOption =
        getOrNull(key) ?: throw NoSuchElementException("MenuOption '$key' not found")

    /**
     * 按照键值获取选项, 若不存在则返回默认选项
     *
     * @param key 选项的唯一标识键
     * @param default 默认选项
     */
    fun getOrDefault(key: String, default: SettingsMenuOption): SettingsMenuOption =
        getOrNull(key) ?: default
}