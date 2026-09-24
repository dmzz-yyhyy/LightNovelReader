package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogEntry
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository
import javax.inject.Inject

@HiltViewModel
class LogcatViewModel @Inject constructor(
    private val loggerRepository: LoggerRepository
) : ViewModel() {

    private val _uiState = MutableLogcatUiState()
    val uiState: LogcatUiState = _uiState

    fun startLogging() {
        loggerRepository.startLogging()
        _uiState.isFileMode = false
        _uiState.displayedLogEntries = loggerRepository.realTimeLogEntries
        Log.i("Logger", "----- history")
    }

    fun clearLogs() = loggerRepository.refreshLogs()

    fun shareLogs() {
        if (_uiState.isFileMode) {
            loggerRepository.shareLogs(_uiState.selectedLogFile)
        } else {
            loggerRepository.shareLogs()
        }
    }

    val displayedLogEntries: List<LogEntry>
        get() = if (_uiState.isFileMode) {
            loggerRepository.fileLogEntries
        } else {
            loggerRepository.realTimeLogEntries
        }

    fun deleteLogFile(fileName: String) {
        loggerRepository.deleteLogFile(fileName)
        onSelectLogFile("实时")
    }

    fun onSelectLogFile(fileName: String) {
        _uiState.isFileMode = fileName.startsWith("lnr")
        _uiState.selectedLogFile = fileName
        loggerRepository.loadLogFile(fileName)
    }

    val logFilenameList: List<String>
        get() = loggerRepository.getAvailableLogFiles() + "实时"
}
