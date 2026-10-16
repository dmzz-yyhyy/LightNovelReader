package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingRule
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SwitchChip
import indi.dmzz_yyhyy.lightnovelreader.ui.components.regexAnnotatedString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTextFormattingRuleBottomSheet(
    rule: FormattingRule,
    matchTextFieldValue: TextFieldValue,
    hasUnsavedChanges: Boolean,
    onNameChange: (String) -> Unit,
    onMatchChange: (TextFieldValue) -> Unit,
    onReplacementChange: (String) -> Unit,
    onIsRegexChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    onDelete: () -> Unit,
) {
    val canHideSheet = remember { mutableStateOf(false) }
    val confirmSheetValueChange = remember {
        { value: SheetValue ->
            value != SheetValue.Hidden || canHideSheet.value
        }
    }
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        confirmValueChange = confirmSheetValueChange
    )
    val contentScrollState = rememberScrollState()
    var shouldCloseSheet by remember { mutableStateOf(false) }
    var showDiscardChangesDialog by remember { mutableStateOf(false) }
    var showMatchRequiredError by remember { mutableStateOf(false) }
    var showDeleteRuleDialog by remember { mutableStateOf(false) }

    val requestDismiss: () -> Unit = {
        if (hasUnsavedChanges) showDiscardChangesDialog = true
        else {
            shouldCloseSheet = true
        }
    }

    val isWrong = remember(rule.match, rule.isRegex) {
        rule.isRegex && runCatching { Regex(rule.match) }.isFailure
    }
    val isMatchMissing = showMatchRequiredError && matchTextFieldValue.text.isBlank()
    val matchVisualTransformation = remember(rule.isRegex) {
        if (rule.isRegex) {
            VisualTransformation { value ->
                TransformedText(
                    text = regexAnnotatedString(value.text),
                    offsetMapping = OffsetMapping.Identity
                )
            }
        } else {
            VisualTransformation.None
        }
    }

    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = true,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false,
            shouldDismissOnClickOutside = false
        )
    ) {
        BackHandler(onBack = requestDismiss)
        val density = LocalDensity.current
        val imeInsets = WindowInsets.ime
        val imeIsAnimating = WindowInsets.imeAnimationSource.getBottom(density) !=
            WindowInsets.imeAnimationTarget.getBottom(density)
        val imeNestedScrollEnabled = contentScrollState.value == 0 || imeIsAnimating
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(shouldCloseSheet) {
            if (!shouldCloseSheet) return@LaunchedEffect

            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            withTimeoutOrNull(300.milliseconds) {
                snapshotFlow { imeInsets.getBottom(density) }.first { it == 0 }
            }

            canHideSheet.value = true
            sheetState.hide()
            onDismissRequest()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 24.dp),
                text = stringResource(R.string.edit_rule),
                style = typography.displayMedium
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(if (imeNestedScrollEnabled) Modifier.imeNestedScroll() else Modifier)
                    .verticalScroll(contentScrollState)
                    .padding(horizontal = 24.dp)
            ) {

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = rule.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.rule_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    value = matchTextFieldValue,
                    onValueChange = onMatchChange,
                    label = { Text(stringResource(R.string.rule_match)) },
                    minLines = 2,
                    isError = isWrong || isMatchMissing,
                    supportingText = if (isMatchMissing) {
                        { Text(stringResource(R.string.text_field_required)) }
                    } else {
                        null
                    },
                    visualTransformation = matchVisualTransformation,
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    value = rule.replacement,
                    onValueChange = onReplacementChange,
                    label = { Text(stringResource(R.string.rule_replacement)) },
                    minLines = 2
                )
                Row(modifier = Modifier.padding(vertical = 12.dp)) {
                    SwitchChip(
                        label = stringResource(R.string.rule_is_regex),
                        selected = rule.isRegex,
                        onClick = { onIsRegexChange(!rule.isRegex) }
                    )
                }
            }

            HorizontalDivider()
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (rule.id != -1) {
                        TextButton(
                            onClick = { showDeleteRuleDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.delete_rule))
                        }
                    }
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = requestDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            if (matchTextFieldValue.text.isBlank()) {
                                showMatchRequiredError = true
                            } else {
                                onConfirmation()
                                shouldCloseSheet = true
                            }
                        },
                        enabled = !isWrong
                    ) {
                        Text(stringResource(R.string.save_rule))
                    }
                }
            }
        }
    }

    if (showDiscardChangesDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardChangesDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.confirm_exit),
                    style = typography.headlineSmall
                )
            },
            text = {
                Text(stringResource(R.string.hint_unsaved_changes_will_be_lost))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardChangesDialog = false
                        shouldCloseSheet = true
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardChangesDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeleteRuleDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteRuleDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.delete_rule),
                    style = typography.headlineSmall
                )
            },
            text = {
                Text(stringResource(R.string.delete_confirm_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteRuleDialog = false
                        onDelete()
                        shouldCloseSheet = true
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteRuleDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
