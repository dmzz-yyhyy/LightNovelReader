package io.nightfish.lightnovelreader.api.plugin

/**
 * 用于标注插件的元数据。
 *
 * @property name 插件显示名称
 * @property version 插件版本号（整数）
 * @property versionName 插件版本名称（字符串）
 * @property author 插件作者
 * @property description 插件的功能或用途描述
 * @property updateUrl 插件更新的基础 URL，用于检查和下载更新
 *
 * 该地址下应包含 `metadata.json` 与 `plugin.apk.lnrp` 文件，
 * 以确保插件更新解析和下载流程能正常工作
 *
 * @property apiVersion 插件编译和运行所依赖的宿主 API 版本
 *
 * 插件安装与运行时会验证该字段：
 * - 同组，且 `apiVersion <= 宿主版本` 时兼容
 * - 不同组，或高于宿主版本时视为不兼容
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Plugin(
    val name: String,
    val version: Int,
    val versionName: String,
    val author: String,
    val description: String,
    val updateUrl: String,
    val apiVersion: Int
)
