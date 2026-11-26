package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class UserDataEntity(

    val path: String,
    val group: String,
    val type: String,
    val value: String
)
