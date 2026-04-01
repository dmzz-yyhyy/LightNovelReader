package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import androidx.compose.runtime.Immutable
import java.util.Base64
import java.util.BitSet

@Immutable
class Count {
    private val bitSet = BitSet(144)

    fun setMinute(hour: Int, minuteCount: Int) {
        require(hour in 0..23) { "Hour must be 0-23, got $hour" }
        require(minuteCount in 0..60) { "Minutes must be 0-60, got $minuteCount" }

        val startBit = hour * 6
        for (i in 5 downTo 0) {
            bitSet.set(startBit + (5 - i), ((minuteCount shr i) and 1) == 1)
        }
    }

    fun getMinute(hour: Int): Int {
        var value = 0
        val startBit = hour * 6
        for (i in 0 until 6) {
            value = (value shl 1) or if (bitSet[startBit + i]) 1 else 0
        }
        return value
    }

    fun toBase64String(): String {
        return Base64.getEncoder().encodeToString(toByteArray())
    }

    fun toByteArray(): ByteArray {
        val bytes = ByteArray(18)
        for (i in 0 until 144) {
            if (bitSet[i]) {
                bytes[i / 8] = (bytes[i / 8].toInt() or (0x80 shr (i % 8))).toByte()
            }
        }
        return bytes
    }

    companion object {
        fun fromBase64String(base64: String): Count {
            val bytes = Base64.getDecoder().decode(base64)
            return fromByteArray(bytes)
        }

        fun fromByteArray(bytes: ByteArray): Count {
            require(bytes.size == 18) { "Invalid byte array size" }
            val count = Count()
            for (hour in 0 until 24) {
                var value = 0
                for (i in 0 until 6) {
                    val bitIndex = hour * 6 + i
                    val byte = bytes[bitIndex / 8].toInt() and 0xFF
                    val bit = (byte shl (bitIndex % 8)) and 0x80
                    value = (value shl 1) or if (bit != 0) 1 else 0
                }
                count.setMinute(hour, value)
            }
            return count
        }
    }

    fun getHourStatistics() = (0..23).associateWith { getMinute(it) }
    fun getTotalMinutes() = (0..23).sumOf { getMinute(it) }

    operator fun plus(other: Count): Count {
        val count = Count()
        for (hour in 0..23) {
            count.setMinute(hour, other.getMinute(hour).coerceAtLeast(this.getMinute(hour)))
        }
        return count
    }
}