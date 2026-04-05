package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import io.nightfish.lightnovelreader.api.book.BookInformation
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
    private val _bookInformationMap = mutableStateMapOf<String, BookInformation>()
    val bookInformationMap: Map<String, BookInformation> = _bookInformationMap.toMap()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { formattingRepository.getFormattingMap() }.collect { map ->
                formattingGroups = map.map {
                    FormattingGroup(it.key, it.value.size)
                }
                for (group in formattingGroups) {
                    if (group.id.isBlank()) continue
                    _bookInformationMap[group.id] = bookRepository.getStateBookInformation(group.id, viewModelScope)
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
