package indi.dmzz_yyhyy.lightnovelreader.data.logging

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.utils.buildReportHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val logsDir = File(context.cacheDir, "logs")
    var logLevel: LogLevel = LogLevel.NONE

    val fileLogEntries = mutableStateListOf<LogEntry>()
    val realTimeLogEntries = mutableStateListOf<LogEntry>()

    private var loggingJob: Job? = null
    private val currentPid = Process.myPid()

    fun startLogging() {
        if (loggingJob?.isActive == true) return
        if (logLevel == LogLevel.NONE) return

        loggingJob = coroutineScope.launch(Dispatchers.IO) {
            Runtime.getRuntime().exec("logcat -c").waitFor()

            val process = Runtime.getRuntime().exec("logcat -v threadtime --pid=$currentPid")
            val reader = process.inputStream.bufferedReader()

            try {
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.isBlank()) continue

                    parseLine(line)?.let { entry ->
                        withContext(Dispatchers.Main.immediate) {
                            realTimeLogEntries.apply {
                                add(entry)
                                if (size > 1000) removeAt(0)
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
            }
        }
    }

    private fun parseLine(line: String): LogEntry? {
        if (line.isBlank()) return null

        val logcatRegex = Regex("""\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}""")
        return if (logcatRegex.containsMatchIn(line)) {
            val parts = line.trim().split("\\s+".toRegex())
            if (parts.size < 5) return null

            val priority = when (parts[4].firstOrNull()?.uppercaseChar()) {
                'V' -> LogLevel.VERBOSE
                'D' -> LogLevel.DEBUG
                'I' -> LogLevel.INFO
                'W' -> LogLevel.WARNING
                'E', 'F' -> LogLevel.ERROR
                else -> return null
            }

            if (priority.level > logLevel.level) return null

            LogEntry(text = line, logLevel = priority)
        } else {
            LogEntry(text = line, logLevel = LogLevel.SYSTEM)
        }
    }


    fun refreshLogs() {
        stopLogging()
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


    fun stopLogging() {
        loggingJob?.cancel()
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
        val file = File(logsDir, fileName)
        if (!file.exists()) return

        coroutineScope.launch(Dispatchers.IO) {
            val newEntries = file.readLines()
                .mapNotNull { parseLine(it) }
                .toMutableList()

            newEntries += LogEntry("----- EOF", LogLevel.SYSTEM)

            withContext(Dispatchers.Main.immediate) {
                fileLogEntries.clear()
                fileLogEntries.addAll(newEntries)
            }
        }
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
