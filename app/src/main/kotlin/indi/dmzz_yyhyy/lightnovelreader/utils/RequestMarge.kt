package indi.dmzz_yyhyy.lightnovelreader.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

class RequestMarge {
    data class Result(
        val value: Any
    )

    val resultMap: MutableMap<String, Result?> = mutableMapOf()

    suspend inline fun <reified T: Any> margeRequest(id: Int, block: suspend () -> T): T {
        val key = T::class.hashCode().toString() + id

        if (!resultMap.contains(key)) {
            resultMap[key] = null
            try {
                resultMap[key] = Result(block.invoke())
            } catch (_: CancellationException) {
                resultMap.remove(key)
                return block.invoke()
            }
        }
        withTimeoutOrNull(30.seconds) {
            while (resultMap.contains(key) && resultMap[key] == null) {
                delay(16)
            }
        }
        val value = resultMap[key]?.let { it.value as T} ?: block.invoke()
        resultMap.remove(key)
        return value
    }
}