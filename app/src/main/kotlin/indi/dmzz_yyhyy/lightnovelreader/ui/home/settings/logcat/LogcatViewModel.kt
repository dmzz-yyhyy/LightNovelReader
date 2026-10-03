package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository
import javax.inject.Inject

@HiltViewModel
class LogcatViewModel @Inject constructor(
    loggerRepository: LoggerRepository
) : ViewModel() {
    val uiState: LogcatUiState = MutableLogcatUiState(loggerRepository)

    init {
        loggerRepository.startLogging()
    }
}
