package indi.dmzz_yyhyy.lightnovelreader.data.room.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import indi.dmzz_yyhyy.lightnovelreader.utils.GsonInstance


class StringListConverter {
    @TypeConverter
    fun listToString(list: List<String?>?): String {
        return GsonInstance.getInstance().getGson().toJson(list)
    }

    @TypeConverter
    fun stringToList(json: String?): List<String> {
        val listType = object : TypeToken<List<String?>?>() {}.type
        return GsonInstance.getInstance().getGson().fromJson(json, listType)
    }
}