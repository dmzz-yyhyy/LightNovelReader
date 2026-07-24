package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 用户数据抽象基类
 * 提供对单条用户数据的读写和监听能力
 *
 * @param T 该用户数据存储的值类型
 * @param path 用户数据的完整路径字符串(如"reader.fontSize")
 *
 * @since Api 4
 */
abstract class UserData<T> (
    open val path: String
) {
    /** 该条数据所属的组路径 */
    val group get() = path.split(".").dropLast(1).joinToString(".")

    /**
     * 写入用户数据
     * 此函数为阻塞函数, 请务必不要在初始化阶段或主线程上调用
     *
     * @param value 需要写入的值
     *
     * @since Api 4
     */
    abstract suspend fun set(value: T)

    /**
     * 异步写入用户数据
     * 内部会在IO协程中执行[set]
     *
     * @param value 需要写入的值
     *
     * @since Api 2
     */
    fun asynchronousSet(value: T) {
        CoroutineScope(Dispatchers.IO).launch {
            set(value)
        }
    }

    /**
     * 读取用户数据
     * 此函数为阻塞函数, 请务必不要在初始化阶段或主线程上调用
     *
     * @return 包含当前数据的可空对象, 如果未设置过则返回null
     *
     * @since Api 4
     */
    abstract suspend fun get(): T?

    /**
     * 获取用户数据的可观测流
     *
     * @return 用户数据可空值的[Flow]
     *
     * @since Api 2
     */
    abstract fun getFlow(): Flow<T?>

    /**
     * 获取用户数据的可观测流, 并提供默认值
     *
     * @param default 当值为null时的默认值
     *
     * @return 用户数据非空值的[Flow]
     *
     * @since Api 2
     */
    fun getFlowWithDefault(default: T): Flow<T> = getFlow().map { it ?: default }

    /**
     * 读取用户数据, 如果未设置则返回默认值
     * 此函数为阻塞函数, 请务必不要在初始化阶段或主线程上调用
     *
     * @param default 当值为null时的默认值
     *
     * @return 当前值或默认值
     *
     * @since Api 4
     */
    suspend fun getOrDefault(default: T): T {
        return get() ?: default
    }

    /**
     * 读取并修改用户数据
     * 此函数为阻塞函数, 请务必不要在初始化阶段或主线程上调用
     *
     * @param updater 接收当前值并返回新值的函数
     * @param default 当前值为null时使用的默认值
     *
     * @since Api 4
     */
    suspend fun update(updater: (T) -> T, default: T) {
        set(updater(getOrDefault(default)))
    }
}