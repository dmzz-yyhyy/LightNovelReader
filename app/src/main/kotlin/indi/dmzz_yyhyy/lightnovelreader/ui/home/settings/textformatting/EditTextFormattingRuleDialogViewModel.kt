package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormatRepository
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditTextFormattingRuleDialogViewModel @Inject constructor(
    private val formattingRepository: FormatRepository,
) : ViewModel() {
    private var bookId: String? = null
    private var initialRuleHash: Int? by mutableStateOf(null)
    var formattingRule: FormattingRule? by mutableStateOf(null)
        private set
    var matchTextFieldValue by mutableStateOf(TextFieldValue())
        private set

    val hasUnsavedChanges: Boolean
        get() = formattingRule?.hashCode() != initialRuleHash

    fun load(bookId: String, ruleId: Int) {
        this.bookId = bookId
        formattingRule = null
        initialRuleHash = null
        matchTextFieldValue = TextFieldValue()
        if (ruleId == -1) {
            val newRule = FormattingRule(
                id = -1,
                name = "",
                match = "",
                replacement = "",
                isRegex = false,
                isEnabled = true
            )
            initialRuleHash = newRule.hashCode()
            formattingRule = newRule
            return
        }
        viewModelScope.launch {
            val rule = withContext(Dispatchers.IO) {
                formattingRepository.getFormattingRules(ruleId)
            }
            initialRuleHash = rule?.hashCode()
            formattingRule = rule
            rule?.match?.let {
                updateMatch(TextFieldValue(it))
            }
        }
    }

    fun updateName(name: String) {
        formattingRule = formattingRule?.copy(
            name = name
        )
    }

    fun updateMatch(match: TextFieldValue) {
        if (formattingRule?.match != match.text) {
            formattingRule = formattingRule?.copy(match = match.text)
        }
        matchTextFieldValue = match
    }

    fun updateReplacement(replacement: String) {
        formattingRule = formattingRule?.copy(
            replacement = replacement
        )
    }

    fun updateIsRegex(isRegex: Boolean) {
        formattingRule = formattingRule?.copy(
            isRegex = isRegex
        )
    }

    fun onConfirmation() {
        val rule = formattingRule ?: return
        val currentBookId = bookId ?: return
        if (rule.match.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            if (rule.id == -1)
                formattingRepository.insertRule(currentBookId, rule)
            else
                formattingRepository.updateRule(currentBookId, rule)
        }
    }

    fun onDelete() {
        val ruleId = formattingRule?.id?.takeIf { it != -1 } ?: return
        CoroutineScope(Dispatchers.IO).launch {
            formattingRepository.deleteRule(ruleId = ruleId)
        }
    }
}
