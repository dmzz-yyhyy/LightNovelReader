package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter
import indi.dmzz_yyhyy.lightnovelreader.utils.ifEquals
import io.nightfish.lightnovelreader.api.book.WordCount

object WorldCountConverter {
    @TypeConverter
    fun worldCountToString(wordCount: WordCount) = "${wordCount.count}|.:.|${wordCount.unit}|.:.|${wordCount.unitResId}"

    @TypeConverter
    fun stringToWorld(string: String) = string.split("|.:.|").let {
        WordCount(it[0].toInt(), it[1].ifEquals("null") { null }, it[2].ifEquals("null") { null }?.toInt())
    }
}