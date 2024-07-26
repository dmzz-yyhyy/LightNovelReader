package indi.dmzz_yyhyy.lightnovelreader.data.loacltion.room.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime

object LocalDataTimeConverter {
    @TypeConverter
    fun stringToDate(dateString: String?): LocalDateTime? {
        return if (dateString == null) {
            null
        } else {
            LocalDateTime.parse(dateString)
        }
    }

    @TypeConverter
    fun dateToString(date: LocalDateTime?): String? {
        return date?.toString()
    }
}