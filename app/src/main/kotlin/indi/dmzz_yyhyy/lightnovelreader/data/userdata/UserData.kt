package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import kotlinx.coroutines.flow.Flow

abstract class UserData<T> (
    open val path: String
) {
    val group get() = path.split(".").dropLast(1).joinToString(".")
    abstract fun set(value: T)
    abstract fun get(): T?
    abstract fun getFlow(): Flow<T?>
    fun getOrDefault(default: T): T {
        return get() ?: default
    }
    fun update(updater: (T) -> T, default: T) {
        set(updater(getOrDefault(default)))
    }
}