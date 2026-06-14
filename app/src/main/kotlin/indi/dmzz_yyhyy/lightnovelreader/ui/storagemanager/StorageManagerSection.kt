package indi.dmzz_yyhyy.lightnovelreader.ui.storagemanager

import androidx.annotation.StringRes

data class StorageManagerSection(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val size: Long
)
