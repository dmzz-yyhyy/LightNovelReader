package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

@Entity(tableName = "user_data")
@BackupTable(BackupGroup.USER_DATA, "id", "setting_path")
data class UserDataEntity(@PrimaryKey val id: Identifier.SettingPath, val value: String) :
    Mergeable<UserDataEntity> {
    init {
        Json.parseToJsonElement(value)
    }
    override fun merge(new: UserDataEntity): UserDataEntity {
        val oldValue = Json.parseToJsonElement(value)
        val newValue = Json.parseToJsonElement(new.value)
        return if (oldValue is JsonArray && newValue is JsonArray && oldValue.all { it is JsonPrimitive && it.isString } && newValue.all { it is JsonPrimitive && it.isString } && id.id != "bookshelf_order") new.copy(
            value = JsonArray((oldValue + newValue).distinct()).toString()
        ) else new
    }
}
