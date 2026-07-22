package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow

/**
 * 用户数据的数据访问对象接口
 * 提供对持久化存储的底层操作
 *
 * @since Api 2
 */
interface UserDataDaoApi {
    /**
     * 写入或更新一条用户数据
     *
     * @param path 数据的完整路径
     * @param group 数据所属的组路径
     * @param type 数据类型名称字符串
     * @param value 序列化后的字符串值
     *
     * @since Api 4
     */
    suspend fun insert(path: String, group: String, type: String, value: String)

    /**
     * 通过路径读取用户数据的字符串值
     *
     * @param path 数据的完整路径
     *
     * @return 存储的字符串值, 如果不存在则返回null
     *
     * @since Api 4
     */
    suspend fun get(path: String): String?

    /**
     * 获取用户数据字符串值的可观测流
     *
     * @param path 数据的完整路径
     *
     * @return 存储字符串可空值的[Flow]
     *
     * @since Api 2
     */
    fun getFlow(path: String): Flow<String?>

    /**
     * 删除指定路径的用户数据
     *
     * @param path 要删除的数据路径
     *
     * @since Api 4
     */
    suspend fun remove(path: String)
}