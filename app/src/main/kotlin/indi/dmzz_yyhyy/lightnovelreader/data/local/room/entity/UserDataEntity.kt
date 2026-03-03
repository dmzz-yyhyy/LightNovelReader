package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_data")
data class UserDataEntity(
    @PrimaryKey
    val path: String,
    val group: String,
    val type: String,
    val value: String
): Mergeable<UserDataEntity> {
    override fun merge(new: UserDataEntity): UserDataEntity = new
}
