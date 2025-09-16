package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.importer.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class Wenku8CloudImportViewModel @Inject constructor(
    private val importer: Wenku8CloudImporter
): ViewModel() {
    private val _state = MutableStateFlow<Wenku8CloudImportState>(Wenku8CloudImportState.Idle)
    val state: StateFlow<Wenku8CloudImportState> = _state

    private var job: Job? = null
    private val cancelled = AtomicBoolean(false)

    fun prepare(targetBookshelfId: Int) {
        if (job?.isActive == true) return
        cancelled.set(false)
        _state.value = Wenku8CloudImportState.Running(total = 0, current = 0)
        job = viewModelScope.launch {
            _state.value = importer.prepare(targetBookshelfId)
        }
    }

    fun execute() {
        val prepared = _state.value as? Wenku8CloudImportState.Prepared ?: return
        if (job?.isActive == true) return
        cancelled.set(false)
        job = viewModelScope.launch {
            val result = importer.execute(
                prepared = prepared,
                isCancelled = { cancelled.get() },
                onProgress = { running -> _state.value = running }
            )
            _state.value = result
        }
    }

    fun cancel() { cancelled.set(true) }

    fun reset() { _state.value = Wenku8CloudImportState.Idle }

    fun needLoginRetry(targetBookshelfId: Int) {
        if (_state.value == Wenku8CloudImportState.NeedLogin) {
            prepare(targetBookshelfId)
        }
    }
}
