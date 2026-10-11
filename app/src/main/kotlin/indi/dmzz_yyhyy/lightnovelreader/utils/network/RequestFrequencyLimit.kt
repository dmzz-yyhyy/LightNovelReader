package indi.dmzz_yyhyy.lightnovelreader.utils.network

import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class RequestFrequencyLimitConfig {
    var minimumInterval: Duration = 500.milliseconds
}

val RequestFrequencyLimit = createClientPlugin(
    "RequestFrequencyLimit",
    ::RequestFrequencyLimitConfig
) {
    val minimumInterval = pluginConfig.minimumInterval
    require(minimumInterval >= Duration.ZERO) {
        "minimumInterval must not be negative"
    }

    val mutex = Mutex()
    var nextAllowedAt = TimeSource.Monotonic.markNow()

    on(SendingRequest) { _, _ ->
        mutex.withLock {
            val wait = nextAllowedAt - TimeSource.Monotonic.markNow()
            if (wait > Duration.ZERO) {
                delay(wait)
            }
            nextAllowedAt = TimeSource.Monotonic.markNow() + minimumInterval
        }
    }
}
