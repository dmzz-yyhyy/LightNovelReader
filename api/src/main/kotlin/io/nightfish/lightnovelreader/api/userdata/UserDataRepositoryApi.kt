package io.nightfish.lightnovelreader.api.userdata

import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 用户数据仓库接口
 * 根据路径声明创建相应类型的用户数据访问对象
 *
 * @since Api 4
 */
interface UserDataRepositoryApi {
    /**
     * 获取指定路径的用户数据
     *
     * @param T 路径所存储的值类型
     * @param path 路径及序列化格式
     * @param source 需要隔离的数据源, 省略时使用当前书源
     * @return 已绑定归属的用户数据访问对象
     *
     * @since Api 4
     */
    fun <T> userData(path: UserDataPath<T>, source: Identifier.DataSource? = null): UserData<T>

    /**
     * 删除指定路径的用户数据
     *
     * @param path 用户数据路径
     * @param source 需要隔离的数据源, 省略时使用当前书源
     *
     * @since Api 4
     */
    suspend fun remove(path: UserDataPath<*>, source: Identifier.DataSource? = null)
}
