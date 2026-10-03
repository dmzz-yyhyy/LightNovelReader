package indi.dmzz_yyhyy.lightnovelreader.data.logging

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.utils.buildReportHeader
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class LoggerRepository @Inject constructor(
    @field:ApplicationContext private val context: Context,
    private val userDataRepository: UserDataRepository,
) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val logsDir = File(context.cacheDir, "logs")
    @Volatile private var logLevel: LogLevel = LogLevel.NONE
    @Volatile private var loggingGeneration = 0L

    val fileLogEntries = mutableStateListOf<LogEntry>()
    val realTimeLogEntries = mutableStateListOf<LogEntry>()

    private var settingsJob: Job? = null
    private var loggingJob: Job? = null
    private var fileLoadJob: Job? = null
    @Volatile private var loggingProcess: java.lang.Process? = null
    private val currentPid = Process.myPid()

    @Synchronized
    fun startLogging() {
        if (settingsJob?.isActive == true) return
        settingsJob = coroutineScope.launch {
            userDataRepository.stringUserData(UserDataPath.Settings.Data.LogLevel.path)
                .getFlowWithDefault("none")
                .map(LogLevel::from)
                .distinctUntilChanged()
                .collect { level ->
                    logLevel = level
                    loggingGeneration++
                    withContext(Dispatchers.Main.immediate) {
                        realTimeLogEntries.clear()
                    }
                    if (level == LogLevel.NONE) stopCapture() else startCapture()
                }
        }
    }

    private fun startCapture() {
        if (loggingJob?.isActive == true) return
        loggingJob = coroutineScope.launch(Dispatchers.IO) {
            val process = Runtime.getRuntime().exec("logcat -v threadtime --pid=$currentPid")
            loggingProcess = process
            val reader = process.inputStream.bufferedReader()

            try {
                coroutineContext.ensureActive()
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.isBlank()) continue

                    val generation = loggingGeneration
                    parseLine(line)?.let { entry ->
                        withContext(Dispatchers.Main.immediate) {
                            if (generation == loggingGeneration && logLevel != LogLevel.NONE) {
                                realTimeLogEntries.apply {
                                    add(entry)
                                    if (size > 8000) removeAt(0)
                                }
                            }
                        }
                    }
                }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                reader.close()
                process.destroy()
                if (loggingProcess === process) loggingProcess = null
            }
        }
    }

    private fun parseLine(line: String, filterByLevel: Boolean = true): LogEntry? {
        if (line.isBlank()) return null

        val logcatRegex = Regex("""\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}""")
        return if (logcatRegex.containsMatchIn(line)) {
            val parts = line.trim().split("\\s+".toRegex())
            if (parts.size < 5) return null

            // INDEX: date, time, pid, tid, priority, tag, message
            val priority = when (parts[4].firstOrNull()?.uppercaseChar()) {
                'V' -> LogLevel.VERBOSE
                'D' -> LogLevel.DEBUG
                'I' -> LogLevel.INFO
                'W' -> LogLevel.WARNING
                'E', 'F' -> LogLevel.ERROR
                else -> return null
            }

            if (filterByLevel && priority.level > logLevel.level) return null

            LogEntry(text = line, level = priority)
        } else {
            LogEntry(text = line, level = LogLevel.SYSTEM)
        }
    }


    fun refreshLogs() {
        realTimeLogEntries.clear()
        startLogging()
    }

    fun shareLogs(fileName: String? = null) {
        val logFile = if (fileName != null) {
            File(logsDir, fileName).takeIf { it.exists() }
        } else {
            val logText = buildString {
                append(buildReportHeader())
                append("\n")
                append("\n----- beginning of logs\n\n")
                append(realTimeLogEntries.joinToString("\n") { it.text })
                append("\n----- end of logcat")
            }
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val exportFile =
                File(logsDir.also { it.mkdirs() }, "lnr_export_${sdf.format(Date())}.log")
            exportFile.writeText(logText)
            exportFile
        }

        if (logFile == null || !logFile.exists()) {
            Log.w("LoggerRepository", "Log file doesn't exist")
            return
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", logFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setType("*/*")
            putExtra(Intent.EXTRA_STREAM, uri)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }


    private fun stopCapture() {
        loggingJob?.cancel()
        loggingProcess?.destroy()
        loggingJob = null
    }

    fun getAvailableLogFiles(): List<String> {
        if (!logsDir.exists()) return emptyList()
        return logsDir.listFiles()
            ?.filter { it.extension == "log" }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }

    fun loadLogFile(fileName: String) {
        fileLoadJob?.cancel()
        fileLogEntries.clear()
        val file = File(logsDir, fileName)
        if (!file.exists()) return

        fileLoadJob = coroutineScope.launch(Dispatchers.IO) {
            val newEntries = file.readLines()
                .mapNotNull { parseLine(it, filterByLevel = false) }
                .toMutableList()

            newEntries += LogEntry("----- EOF", LogLevel.SYSTEM)

            withContext(Dispatchers.Main.immediate) {
                fileLogEntries.clear()
                fileLogEntries.addAll(newEntries)
            }
        }
    }

    fun clearLoadedLogFile() {
        fileLoadJob?.cancel()
        fileLogEntries.clear()
    }

    fun deleteLogFile(fileName: String) {
        if (fileName == ":all") {
            logsDir.listFiles()?.forEach { it.delete() }
            return
        }

        val file = File(logsDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }
}
