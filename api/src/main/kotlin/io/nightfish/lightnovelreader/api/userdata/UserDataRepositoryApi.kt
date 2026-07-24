package io.nightfish.lightnovelreader.api.userdata

/**
 * 用户数据仓库接口
 * 提供创建各类型[UserData]对象的工厂方法, 以及删除数据的能力
 *
 * @since Api 2
 */
interface UserDataRepositoryApi {
    /**
     * 创建字符串类型的用户数据
     *
     * @param path 用户数据路径
     *
     * @return [StringUserData]实例
     *
     * @since Api 2
     */
    fun stringUserData(path: String): StringUserData

    /**
     * 创建浮点数类型的用户数据
     *
     * @param path 用户数据路径
     *
     * @return [FloatUserData]实例
     *
     * @since Api 2
     */
    fun floatUserData(path: String): FloatUserData

    /**
     * 创建整数类型的用户数据
     *
     * @param path 用户数据路径
     *
     * @return [IntUserData]实例
     *
     * @since Api 2
     */
    fun intUserData(path: String): IntUserData

    /**
     * 创建布尔类型的用户数据
     *
     * @param path 用户数据路径
     *
     * @return [BooleanUserData]实例
     *
     * @since Api 2
     */
    fun booleanUserData(path: String): BooleanUserData

    /**
     * 创建整数列表类型的用户数据
     *
     * @param path 用户数据路径
     *
     * @return [IntListUserData]实例
     *
     * @since Api 2
     */
    fun intListUserData(path: String): IntListUserData

    /**
     * 创建字符串列表类型的用户数据
     *
     * @param path 用户数据路径
     *
     * @return [StringListUserData]实例
     *
     * @since Api 2
     */
    fun stringListUserData(path: String): StringListUserData

    /**
     * 创建颜色类型的用户数据
     *
     * @param path 用户数据路径
     *
     * @return [ColorUserData]实例
     *
     * @since Api 2
     */
    fun colorUserData(path: String): ColorUserData

    /**
     * 创建 URI 类型的用户数据
     *
     * @param path 用户数据路径
     *
     * @return [UriUserData]实例
     *
     * @since Api 2
     */
    fun uriUserData(path: String): UriUserData

    /**
     * 删除指定路径的用户数据
     *
     * @param path 要删除的数据路径
     *
     * @since Api 4
     */
    suspend fun remove(path: String)
}