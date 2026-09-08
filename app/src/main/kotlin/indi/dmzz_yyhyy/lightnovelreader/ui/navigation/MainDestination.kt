package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.nightfish.lightnovelreader.api.Route

enum class MainDestination(
    val index: Int,
    val route: NavKey,
) {
    Reading(0, Route.Main.Reading.Home),
    Bookshelf(1, Route.Main.Bookshelf.Home),
    Explore(2, Route.Main.Explore.Home),
    Settings(3, Route.Main.Settings.Home);

    companion object {
        fun from(route: NavKey): MainDestination? = entries.firstOrNull { it.route == route }
    }
}