package io.nightfish.lightnovelreader.api.util

import kotlin.reflect.KClass

class Cache(
    val maxCountEachType: Int = 10,
    val timeout: Int = 30_000
) {
    data class CacheData<T> (
        val time: Long,
        val data: T
    )

    val cacheMap = mutableMapOf<KClass<*>, MutableMap<Int, CacheData<Any>>>()

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