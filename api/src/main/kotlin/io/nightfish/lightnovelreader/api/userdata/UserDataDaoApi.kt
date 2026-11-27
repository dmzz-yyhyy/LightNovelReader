package io.nightfish.lightnovelreader.api.userdata

import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据的持久化访问接口
 * 值使用路径所声明的序列化器编码为 JSON
 *
 * @since Api 4
 */
interface UserDataDaoApi {
    /**
     * 写入用户数据, 已存在时覆盖
     *
     * @param id 设置标识符
     * @param value JSON 格式的数据
     *
     * @since Api 4
     */
    suspend fun insert(id: Identifier.SettingPath, value: String)

    /**
     * 读取用户数据
     *
     * @param id 设置标识符
     * @return JSON 格式的数据, 不存在时返回null
     *
     * @since Api 4
     */
    suspend fun get(id: Identifier.SettingPath): String?

    /**
     * 监听用户数据变化
     *
     * @param id 设置标识符
     * @return JSON 数据的可观测流, 不存在时发出null
     *
     * @since Api 4
     */
    fun getFlow(id: Identifier.SettingPath): Flow<String?>

    /**
     * 删除用户数据
     *
     * @param id 设置标识符
     *
     * @since Api 4
     */
    suspend fun remove(id: Identifier.SettingPath)
}
