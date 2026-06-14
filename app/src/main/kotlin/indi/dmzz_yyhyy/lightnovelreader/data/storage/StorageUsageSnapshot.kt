package indi.dmzz_yyhyy.lightnovelreader.data.storage

import kotlinx.serialization.Serializable

@Serializable
    data class StorageUsageSnapshot(
    val totalBytes: Long = 0L,
    val appBytes: Long = 0L,
    val databaseDiskBytes: Long = 0L,
    val pluginBytes: Long = 0L,
    val cacheBytes: Long = 0L,
    val otherFileBytes: Long = 0L,
    val allBookMetadataBytes: Long = 0L,
    val orphanChapterInfoBytes: Long = 0L,
    val orphanChapterContentBytes: Long = 0L,
    val books: List<BookStorageUsage> = emptyList(),
    val calculatedAt: Long = 0L
)