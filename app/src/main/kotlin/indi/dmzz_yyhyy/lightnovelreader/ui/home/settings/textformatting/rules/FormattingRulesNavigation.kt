package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.FormattingViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.navigateToEditTextFormattingRuleDialog
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsTextFormattingRulesDestination() {
    entry<Route.Main.Settings.TextFormatting.Rules> { navBackStackEntry ->
        val navigator = LocalNavigator.current
        val bookId = navBackStackEntry.bookId
        val viewModel = hiltViewModel<FormattingViewModel>()
        LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
            viewModel.loadBookFormattingRules(bookId)
        }
        FormattingRulesScreen(
            rules = viewModel.rules,
            onToggle = viewModel::onToggle,
            onClickBack = navigator::popBackStack,
            onClickAddRule = { navigator.navigateToEditTextFormattingRuleDialog(bookId, -1) },
            onClickEditRule = { navigator.navigateToEditTextFormattingRuleDialog(bookId, it) }
        )
    }
}

fun Navigator.navigateToSettingsTextFormattingRulesDestination(target: String) {
    navigate(Route.Main.Settings.TextFormatting.Rules(target))
}
