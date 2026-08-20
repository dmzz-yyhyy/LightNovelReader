package io.nightfish.lightnovelreader.api.util

import kotlin.reflect.KClass

/**
 * 通用内存缓存工具
 * 支持多种类型的缓存, 并自动过期清除
 *
 * @param maxCountEachType 每种类型最多缓存条数, 超出时会清除最早的缓存
 * @param timeout 缓存过期时间(毫秒), 默认为30秒
 *
 * @since Api 2
 */
class Cache(
    val maxCountEachType: Int = 10,
    val timeout: Int = 30_000
) {
    /**
     * 缓存条目，包含缓存时间与数据
     *
     * @param T 缓存数据类型
     * @param time 写入缓存的时间戳(毫秒)
     * @param data 缓存的数据
     *
     * @since Api 2
     */
    data class CacheData<T>(
        val time: Long,
        val data: T
    )

    /**
     * 按类型分组的缓存数据映射表
     *
     * @since Api 2
     */
    val cacheMap = mutableMapOf<KClass<*>, MutableMap<Int, CacheData<Any>>>()

    /**
     * 将数据写入缓存
     * 如果同类型缓存已达上限, 则删除最早写入的缓存
     *
     * @param T 缓存数据类型
     * @param id 缓存条目的唯一标识整数
     * @param t 需要缓存的数据, 为null时忽略
     *
     * @since Api 2
     */
    inline fun <reified T> cache(id: Int, t: T) {
        t ?: return
        val tClass = T::class
        if (cacheMap.contains(tClass)) {
            val map = cacheMap[tClass] ?: return
            if (map.size >= maxCountEachType)
                map.remove(map.entries.minByOrNull { it.value.time }?.key ?: return)
            map[id] = CacheData(System.currentTimeMillis(), t)
        } else {
            cacheMap[tClass] = mutableMapOf(Pair(id, CacheData(System.currentTimeMillis(), t)))
        }
    }

    /**
     * 获取缓存数据
     * 如果缓存已过期或不存在则返回null
     *
     * @param T 缓存数据类型
     * @param id 缓存条目的唯一标识整数
     *
     * @return 缓存的数据, 不存在或已过期则返回null
     *
     * @since Api 2
     */
    inline fun <reified T> getCache(id: Int): T? {
        val tClass = T::class
        if (!cacheMap.contains(tClass)) return null
        val map = cacheMap[tClass] ?: return null
        val data = map[id] ?: return null
        if (System.currentTimeMillis() - data.time > timeout) {
            map.remove(id)
            return null
        }
        return data.data as T
    }
}