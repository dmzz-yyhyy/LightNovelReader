package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormatRepository
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingGroup
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormattingViewModel @Inject constructor(
    private val formattingRepository: FormatRepository,
    bookRepository: BookRepository
) : ViewModel() {
    var formattingGroups by mutableStateOf(emptyList<FormattingGroup>())
        private set
    var bookId = ""
    var rules by mutableStateOf(emptyList<FormattingRule>())
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { formattingRepository.getFormattingMap() }.collect { map ->
                formattingGroups = map.map {
                    FormattingGroup(it.key, bookRepository.getBookInformationFlow(it.key), it.value.size)
                }
            }
        }
    }

    fun loadBookFormattingRules(bookId: String) {
        this.bookId = bookId
        rules = formattingRepository.getStateBookFormattingRules(bookId)
    }
    fun onToggle(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            formattingRepository.updateRule(
                bookId = bookId,
                formattingRule = rules
                    .firstOrNull { it.id == id }
                    ?.let {
                        it.copy(
                            isEnabled = !it.isEnabled
                        )
                    } ?: return@launch
            )
        }
    }
}
