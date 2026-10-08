package indi.dmzz_yyhyy.lightnovelreader.data.logging

import java.util.concurrent.atomic.AtomicLong

data class LogEntry(
    val text: String,
    val level: LogLevel,
    val id: Long = nextId.getAndIncrement()
) {
    companion object {
        private val nextId = AtomicLong()
    }
}
