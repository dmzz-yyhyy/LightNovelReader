package indi.dmzz_yyhyy.lightnovelreader.data.local.room

import android.content.ContentValues
import androidx.sqlite.db.SupportSQLiteDatabase

/** 为表名与列名加上引号, 所有值通过参数绑定写入 */
internal fun SupportSQLiteDatabase.insertRow(table: String, row: ContentValues) {
    fun quote(name: String) = "`" + name.replace("`", "``") + "`"
    val columns = row.keySet().toList()
    execSQL(
        "INSERT INTO ${quote(table)} (${columns.joinToString { quote(it) }}) VALUES (${columns.joinToString { "?" }})",
        columns.map { row.get(it) }.toTypedArray()
    )
}
