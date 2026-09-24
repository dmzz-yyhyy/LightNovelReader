package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.licenses

import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsLicensesDestination() {
    entry<Route.Main.Settings.Licenses> {
        val navigator = LocalNavigator.current
        LicensesScreen(
            onClickBack = navigator::popBackStack
        )
    }
}

fun Navigator.navigateToSettingsLicensesDestination() {
    navigate(Route.Main.Settings.Licenses)
}
