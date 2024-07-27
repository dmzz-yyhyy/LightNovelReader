package indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class UserDataEntity(
    @PrimaryKey
    val path: String,
    val group: String,
    val type: String,
    val value: String
)
