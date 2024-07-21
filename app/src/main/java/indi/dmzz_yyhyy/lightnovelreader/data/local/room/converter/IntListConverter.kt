package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter

class IntListConverter {
    @TypeConverter
    fun intListToString(intList: List<Int>): String {
        return intList.joinToString(",")
    }

    @TypeConverter
    fun stringToIntList(string: String): List<Int> {
        return string.split(",").map { it.toInt() }
    }
}