package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogEntry
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository

@Stable
interface LogcatUiState {
    val selectedLogFile: String?
    val logFiles: List<String>
    val liveEntries: List<LogEntry>
    val fileEntries: List<LogEntry>
    var tabIndex: Int
    var autoScrollEnabled: Boolean
    var wrapText: Boolean
    var textSize: Float
    var stickToBottom: Boolean
    var scrollingToBottom: Boolean
    var menuExpanded: Boolean
    var fileMenuExpanded: Boolean
    var selectorVisible: Boolean
    val liveLazyListState: LazyListState
    val fileLazyListState: LazyListState
    fun refreshFiles()
    fun clearLiveLogs()
    fun shareLiveLogs()
    fun shareSelectedFile()
    fun deleteSelectedFile()
    fun deleteAllFiles()
    fun selectLogFile(fileName: String?)
}

class MutableLogcatUiState(private val loggerRepository: LoggerRepository) : LogcatUiState {
    override var selectedLogFile: String? by mutableStateOf(null)
    override val logFiles = mutableStateListOf<String>()
    override val liveEntries get() = loggerRepository.realTimeLogEntries
    override val fileEntries get() = loggerRepository.fileLogEntries
    override var tabIndex: Int by mutableIntStateOf(0)
    override var autoScrollEnabled: Boolean by mutableStateOf(false)
    override var wrapText: Boolean by mutableStateOf(false)
    override var textSize: Float by mutableFloatStateOf(12f)
    override var stickToBottom: Boolean by mutableStateOf(true)
    override var scrollingToBottom: Boolean by mutableStateOf(false)
    override var menuExpanded: Boolean by mutableStateOf(false)
    override var fileMenuExpanded: Boolean by mutableStateOf(false)
    override var selectorVisible: Boolean by mutableStateOf(false)
    override val liveLazyListState = LazyListState()
    override val fileLazyListState = LazyListState()

    init {
        refreshFiles()
    }

    override fun refreshFiles() {
        logFiles.clear()
        logFiles.addAll(loggerRepository.getAvailableLogFiles())
        if (selectedLogFile != null && selectedLogFile !in logFiles) selectLogFile(null)
    }

    override fun clearLiveLogs() = loggerRepository.refreshLogs()

    override fun shareLiveLogs() {
        loggerRepository.shareLogs()
        refreshFiles()
    }

    override fun shareSelectedFile() {
        selectedLogFile?.let(loggerRepository::shareLogs)
    }

    override fun deleteSelectedFile() {
        selectedLogFile?.let(loggerRepository::deleteLogFile)
        selectLogFile(null)
        refreshFiles()
    }

    override fun deleteAllFiles() {
        loggerRepository.deleteLogFile(":all")
        selectLogFile(null)
        refreshFiles()
    }

    override fun selectLogFile(fileName: String?) {
        selectedLogFile = fileName
        if (fileName == null) loggerRepository.clearLoadedLogFile()
        else loggerRepository.loadLogFile(fileName)
    }
}
