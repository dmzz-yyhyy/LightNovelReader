package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.JsonSyntaxException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormatRepository
import indi.dmzz_yyhyy.lightnovelreader.data.json.AppUserDataContent
import indi.dmzz_yyhyy.lightnovelreader.data.json.AppUserDataJson
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.zip.ZipInputStream

@HiltWorker
class ImportDataWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webBookDataSource: WebBookDataSource,
    private val bookshelfRepository: BookshelfRepository,
    private val bookRepository: BookRepository,
    private val userDataRepository: UserDataRepository,
    private val statsRepository: StatsRepository,
    private val formatRepository: FormatRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val fileUri = inputData.getString("uri")?.let(Uri::parse) ?: return Result.failure()
        val ignoreDataIdCheck  = inputData.getBoolean("ignoreDataIdCheck", false)
        var jsonText: String? = null
        var data: AppUserDataContent? = null
        try {
            applicationContext.contentResolver.openFileDescriptor(fileUri, "r")?.use { parcelFileDescriptor ->
                jsonText = FileInputStream(parcelFileDescriptor.fileDescriptor).use { fileInputStream ->
                    ZipInputStream(fileInputStream).use {
                        it.nextEntry
                        it.bufferedReader().readText()
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return Result.failure()
        } catch (e: IOException) {
            e.printStackTrace()
            return Result.failure()
        }
        if (jsonText == null) return Result.failure()
        try {
            val appUserDataJson = AppUserDataJson.fromJson(jsonText!!)
            if (appUserDataJson.type == "light novel reader data file")
                data = appUserDataJson.data.firstOrNull { it.webDataSourceId == webBookDataSource.id }
            if (data == null) {
                if(!ignoreDataIdCheck) {
                    Log.e(
                        "Data Importer",
                        "failed to import the data into app, the data file's web source id is different from app(AppWebSourceId: ${webBookDataSource.id})"
                    )
                    return Result.failure()
                } else {
                    data = appUserDataJson.data.first()
                }
            }
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            return Result.failure()
        }
        bookshelfRepository.importBookshelf(data)
        bookRepository.importUserReadingData(data)
        userDataRepository.importUserData(data)
        statsRepository.importReadingStats(data)
        formatRepository.importFormattingRules(data)
        return Result.success()
    }
}