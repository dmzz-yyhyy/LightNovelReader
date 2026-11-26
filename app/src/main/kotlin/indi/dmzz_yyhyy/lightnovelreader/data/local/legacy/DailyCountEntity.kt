package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class DailyCountEntity(


    val date: String,

    val timeCount: String,
)
