package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormatRepository
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingGroup
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingRule
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextFormattingViewModel @Inject constructor(
    private val formattingRepository: FormatRepository,
    bookRepository: BookRepository
) : ViewModel() {
    var formattingGroups by mutableStateOf(emptyList<FormattingGroup>())
        private set
    var bookId = ""
    var rules by mutableStateOf(emptyList<FormattingRule>())
        private set
    private var rulesJob: Job? = null
    private val bookInformationFlowCache =
        mutableMapOf<String, Flow<Result<BookInformation, WebRequestError>>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            formattingRepository.getFormattingMapFlow().collect { map ->
                formattingGroups = map.map { (bookId, rules) ->
                    FormattingGroup(
                        bookId,
                        bookInformationFlowCache.getOrPut(bookId) {
                            bookRepository.getBookInformationFlow(bookId)
                        },
                        rules.size
                    )
                }
            }
        }
    }

    fun loadBookFormattingRules(bookId: String) {
        this.bookId = bookId
        rules = emptyList()
        rulesJob?.cancel()
        rulesJob = viewModelScope.launch(Dispatchers.IO) {
            formattingRepository.getFormattingRulesFlow(bookId).collect { bookRules ->
                rules = bookRules
            }
        }
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
