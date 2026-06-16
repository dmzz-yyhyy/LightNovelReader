package io.nightfish.lightnovelreader.api.plugin

import io.nightfish.lightnovelreader.api.identifier.Identifier
import java.io.File

/**
 * 插件运行时的上下文对象
 * 宿主会将此对象注入到插件中，供插件访问它的资源和文件目录
 *
 * @property dataDir 插件的私有数据目录
 * @property pluginFile 插件自身的 APK 文件
 *
 * @since Api 2
 */
class PluginContext(
    val packageName: String,
    val dataDir: File,
    val pluginFile: File,
    private val assetDir: File
) {
    /**
     * 获取插件资源目录中的文件
     *
     * @param path 相对于资源目录的文件路径
     *
     * @return 该路径对应的[File]对象
     *
     * @since Api 2
     */
    fun getAsset(path: String) = assetDir.resolve(path)

    /**
     * 创建插件Id, 用于部分地方的注册
     *
     * @param id id名
     *
     * @since Api 4
     */
    fun ofId(id: String): Identifier = Identifier(packageName, id)
}