package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count

class CountConverter {
    @TypeConverter
    fun fromCount(count: Count?): ByteArray? = count?.toByteArray()

    @TypeConverter
    fun toCount(bytes: ByteArray?): Count? = bytes?.let { Count.fromByteArray(it) }
}