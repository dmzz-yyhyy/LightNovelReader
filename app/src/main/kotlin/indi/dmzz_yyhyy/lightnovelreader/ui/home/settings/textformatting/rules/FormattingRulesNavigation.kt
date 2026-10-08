package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.TextFormattingViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.navigateToEditTextFormattingRuleDialog
import io.nightfish.lightnovelreader.api.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsTextFormattingRulesDestination() {
    composable<Route.Main.Settings.TextFormatting.Rules> { navBackStackEntry ->
        val navController = LocalNavController.current
        val bookId = navBackStackEntry.toRoute<Route.Main.Settings.TextFormatting.Rules>().bookId
        val viewModel = hiltViewModel<TextFormattingViewModel>()
        LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
            viewModel.loadBookFormattingRules(bookId)
        }
        FormattingRulesScreen(
            rules = viewModel.rules,
            isGlobal = bookId.isEmpty(),
            onToggle = viewModel::onToggle,
            onClickBack = navController::popBackStackIfResumed,
            onClickAddRule = { navController.navigateToEditTextFormattingRuleDialog(bookId, -1) },
            onClickEditRule = { navController.navigateToEditTextFormattingRuleDialog(bookId, it) }
        )
    }
}

fun NavController.navigateToSettingsTextFormattingRulesDestination(target: String) {
    if (!this.isResumed()) return
    navigate(Route.Main.Settings.TextFormatting.Rules(target))
}
