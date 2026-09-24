package io.nightfish.lightnovelreader.api.settings

/**
 * 设置项菜单选项
 *
 * - [key] 选项的唯一标识键
 * - [nameId] 选项名称的字符串资源ID
 *
 * @since Api 4
 */
interface SettingsMenuOption {
    val key: String
    val nameId: Int
}
