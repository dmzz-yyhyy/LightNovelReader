package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.overlay.overlayEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.EditTextFormattingRuleDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules.navigateToSettingsTextFormattingRulesDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules.settingsTextFormattingRulesDestination
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.editTextFormattingRuleDialog() {
    overlayEntry<Route.Main.EditTextFormattingRuleDialog> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<EditTextFormattingRuleDialogViewModel>()
        LaunchedEffect(it.bookId, it.ruleId) {
            viewModel.load(it.bookId, it.ruleId)
        }
        viewModel.formattingRule?.let { rule ->
            EditTextFormattingRuleDialog(
                rule = rule,
                matchTextFieldValue = viewModel.matchTextFieldValue,
                onDismissRequest = { navigator.popBackStack() },
                onConfirmation = {
                    viewModel.onConfirmation()
                    navigator.popBackStack()
                },
                onDelete = {
                    viewModel.onDelete()
                    navigator.popBackStack()
                },
                onNameChange = viewModel::updateName,
                onMatchChange = viewModel::updateMatch,
                onReplacementChange = viewModel::updateReplacement,
                onIsRegexChange = viewModel::updateIsRegex
            )
        }
    }
}

fun Navigator.navigateToEditTextFormattingRuleDialog(bookId: String, ruleId: Int) {
    navigate(Route.Main.EditTextFormattingRuleDialog(bookId, ruleId))
}

fun NavEntryScope.settingsTextFormattingNavigation() {
    settingsTextFormattingManagerDestination()
    settingsTextFormattingRulesDestination()
}

fun NavEntryScope.settingsTextFormattingManagerDestination() {
    entry<Route.Main.Settings.TextFormatting.Manager> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<FormattingViewModel>()
        TextFormattingScreen(
            onClickBack = navigator::popBackStack,
            onClickGroup = navigator::navigateToSettingsTextFormattingRulesDestination,
            groups = viewModel.formattingGroups
        )
    }
}

fun Navigator.navigateToSettingsTextFormattingManagerDestination() {
    navigate(Route.Main.Settings.TextFormatting.Manager)
}
