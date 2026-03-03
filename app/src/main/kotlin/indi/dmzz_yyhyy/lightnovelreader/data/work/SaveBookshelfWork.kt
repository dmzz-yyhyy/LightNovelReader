package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalDataManager
import indi.dmzz_yyhyy.lightnovelreader.data.local.cbor.AppLocalData
import indi.dmzz_yyhyy.lightnovelreader.data.local.cbor.LocalData
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookshelfDao
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.utils.writeAppLocalData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import java.io.FileOutputStream

@HiltWorker
class SaveBookshelfWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localDataManager: LocalDataManager,
    private val bookshelfDao: BookshelfDao
) : Worker(appContext, workerParams) {
    companion object {
        const val TAG = "ExportDataWork"
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun doWork(): Result {
        val id = inputData.getInt("bookshelfId", -1)
        val uri = inputData.getString("uri")?.let(Uri::parse) ?: return Result.failure()
        val bookshelfEntityList =
            if (id != -1) bookshelfDao.getBookshelf(id)?.let(::listOf) ?: emptyList()
            else bookshelfDao.getAllBookshelves()
        if (bookshelfEntityList.isEmpty() && id != -1) {
            Log.e(TAG, "Bookshelf doesn't exit (id=$id)")
            return Result.failure()
        }
        val bookshelfIds = bookshelfEntityList.map { it.id }
        val bookshelfBookMetadataEntities = mutableListOf<String>().apply {
                for (entity in bookshelfEntityList) {
                    this.addAll(entity.allBookIds)
                }
            }.distinct().mapNotNull(bookshelfDao::getBookshelfBookMetadataEntity).map { entity ->
                entity.copy(
                    bookShelfIds = entity.bookShelfIds.filter { bookshelfIds.contains(it) }
                )
            }
        val appLocalData = AppLocalData(
            version = localDataManager.currentAppDataVersion,
            localDataList = listOf(
                LocalData.empty().copy(
                    webBookDataSourceId = webBookDataSourceProvider.default.id,
                    bookshelfEntities = bookshelfEntityList,
                    bookshelfBookMetadataEntities = bookshelfBookMetadataEntities
                )
            ),
            globalLocalData = LocalData.empty()
        )
        try {
            applicationContext.contentResolver.openFileDescriptor(uri, "w")
                ?.use { parcelFileDescriptor ->
                    FileOutputStream(parcelFileDescriptor.fileDescriptor).use {
                        it.writeAppLocalData(Cbor.encodeToByteArray(appLocalData))
                    }
                }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save file")
            e.printStackTrace()
            return Result.failure()
        }
    }
}