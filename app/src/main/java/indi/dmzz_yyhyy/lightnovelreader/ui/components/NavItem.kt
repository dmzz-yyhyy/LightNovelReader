package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class NavItem(
    val route: String,
    @DrawableRes val drawable: Int,
    @StringRes val label: Int
)
