package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter

class ListConverter {
    @TypeConverter
    fun intListToString(intList: List<Int>): String {
        return intList.joinToString(",")
    }

    @TypeConverter
    fun stringToIntList(string: String): List<Int> {
        return string.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun stringListToString(stringList: List<String>): String {
        return stringList.joinToString(",")
    }

    @TypeConverter
    fun stringToStringList(string: String): List<String> {
        return string.split(",").map { it }
    }
}