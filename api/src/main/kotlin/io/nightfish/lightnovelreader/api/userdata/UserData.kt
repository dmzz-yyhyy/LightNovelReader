package io.nightfish.lightnovelreader.api.userdata

import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * 带有声明式路径和 JSON 格式的用户数据
 * 提供对单条用户数据的读写和监听能力
 *
 * @param T 该用户数据存储的值类型
 * @property definition 路径及其序列化格式
 * @property id 已绑定归属的设置标识符
 * @param dao 持久化访问接口
 *
 * @since Api 4
 */
class UserData<T>(
    val definition: UserDataPath<T>,
    val id: Identifier.SettingPath,
    private val dao: UserDataDaoApi
) {
    /** 该条数据声明的路径 @since Api 4 */
    val path get() = definition.path
    private val json = Json

    /**
     * 写入用户数据
     * 此函数为阻塞函数, 请务必不要在初始化阶段或主线程上调用
     *
     * @param value 需要写入的值
     *
     * @since Api 4
     */
    suspend fun set(value: T) = dao.insert(id, json.encodeToString(definition.serializer, value))

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
    suspend fun get(): T? = dao.get(id)?.let { json.decodeFromString(definition.serializer, it) }

    /**
     * 获取用户数据的可观测流
     *
     * @return 用户数据可空值的[Flow]
     *
     * @since Api 2
     */
    fun getFlow(): Flow<T?> = dao.getFlow(id)
        .map { value -> value?.let { json.decodeFromString(definition.serializer, it) } }

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

/**
 * 读取并修改列表数据
 * 未设置数据时使用空列表
 *
 * @param T 列表元素类型
 * @param updater 接收当前列表并返回新列表的函数
 *
 * @since Api 4
 */
suspend fun <T> UserData<List<T>>.update(updater: (List<T>) -> List<T>) =
    update(updater, emptyList())
