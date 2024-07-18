package indi.dmzz_yyhyy.lightnovelreader.data.loacltion.room.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime

class LocalDataTimeConverter {
    @TypeConverter
    fun toDate(dateString: String?): LocalDateTime? {
        return if (dateString == null) {
            null
        } else {
            LocalDateTime.parse(dateString)
        }
    }

    @TypeConverter
    fun toDateString(date: LocalDateTime?): String? {
        return date?.toString()
    }
}