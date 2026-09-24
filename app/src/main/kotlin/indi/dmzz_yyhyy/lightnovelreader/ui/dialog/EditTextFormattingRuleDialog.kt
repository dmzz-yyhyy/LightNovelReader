package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingRule
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SwitchChip

@Composable
fun EditTextFormattingRuleDialog(
    rule: FormattingRule,
    matchTextFieldValue: TextFieldValue,
    onNameChange: (String) -> Unit,
    onMatchChange: (TextFieldValue) -> Unit,
    onReplacementChange: (String) -> Unit,
    onIsRegexChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    onDelete: () -> Unit,
) {
    LaunchedEffect(rule.isRegex) {
        onMatchChange.invoke(matchTextFieldValue)
    }
    val isWrong = remember(rule.match, rule.isRegex) {
        if (!rule.isRegex) return@remember false
        var v = true
        try {
            Regex(rule.match)
            v = false
        } catch (_: Exception) {
        }
        return@remember v
    }
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.edit_rule),
                style = typography.displayMedium,
                color = colorScheme.onSurface,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = rule.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.rule_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = matchTextFieldValue,
                    onValueChange = onMatchChange,
                    label = { Text(stringResource(R.string.rule_match)) },
                    singleLine = false,
                    isError = isWrong,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = rule.replacement,
                    onValueChange = onReplacementChange,
                    label = { Text(stringResource(R.string.rule_replacement)) },
                    singleLine = false
                )
                SwitchChip(
                    label = stringResource(R.string.rule_is_regex),
                    selected = rule.isRegex,
                    onClick = {
                        onIsRegexChange(!rule.isRegex)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmation, enabled = !isWrong) {
                Text(text = stringResource(R.string.save_rule))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (rule.id != -1)
                    TextButton(onClick = onDelete) {
                        Text(text = stringResource(R.string.delete_rule), color = colorScheme.error)
                    }
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        }
    )
}

