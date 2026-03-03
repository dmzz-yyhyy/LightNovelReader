package indi.dmzz_yyhyy.lightnovelreader.data.local.cbor

import kotlinx.serialization.Serializable

@Serializable
data class AppLocalData(
    val version: Int = 0,
    val localDataList: List<LocalData>,
    val globalLocalData: LocalData
)