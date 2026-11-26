package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.json.Json

object ListConverter {
    @TypeConverter
    fun bookListToString(value: List<Identifier.Book>): String = Json.encodeToString(value)
    @TypeConverter
    fun stringToBookList(value: String): List<Identifier.Book> = Json.decodeFromString(value)
    @TypeConverter
    fun chapterListToString(value: List<Identifier.Chapter>): String = Json.encodeToString(value)
    @TypeConverter
    fun stringToChapterList(value: String): List<Identifier.Chapter> = Json.decodeFromString(value)
    @TypeConverter
    fun bookshelfListToString(value: List<Identifier.Bookshelf>): String =
        Json.encodeToString(value)

    @TypeConverter
    fun stringToBookshelfList(value: String): List<Identifier.Bookshelf> =
        Json.decodeFromString(value)

    @TypeConverter
    fun intListToString(intList: List<Int>): String {
        return intList.joinToString(",")
    }

    @TypeConverter
    fun stringToIntList(string: String): List<Int> {
        if (string.isEmpty()) return emptyList()
        return string.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun stringListToString(stringList: List<String>): String {
        return stringList.joinToString(",")
    }

    @TypeConverter
    fun stringToStringList(string: String): List<String> {
        return string.ifBlank { return emptyList() }.split(",").map { it }
    }
}
