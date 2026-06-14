package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query

data class StorageBytes(
    val id: String,
    val bytes: Long
)

data class VolumeStorageRow(
    val bookId: String,
    val chapterIdList: String,
    val bytes: Long
)

@Dao
interface StorageStatsDao {
    @Query(
        """
        select
            id as id,
            (
                ifnull(length(cast(id as blob)), 0) +
                ifnull(length(cast(title as blob)), 0) +
                ifnull(length(cast(subtitle as blob)), 0) +
                ifnull(length(cast(cover_uri as blob)), 0) +
                ifnull(length(cast(author as blob)), 0) +
                ifnull(length(cast(description as blob)), 0) +
                ifnull(length(cast(tags as blob)), 0) +
                ifnull(length(cast(publishing_house as blob)), 0) +
                ifnull(length(cast(word_count as blob)), 0) +
                ifnull(length(cast(last_update as blob)), 0) +
                1
            ) as bytes
        from book_information
        """
    )
    suspend fun getBookInformationBytes(): List<StorageBytes>

    @Query(
        """
        select
            book_id as bookId,
            chapter_id_list as chapterIdList,
            (
                ifnull(length(cast(book_id as blob)), 0) +
                ifnull(length(cast(volume_id as blob)), 0) +
                ifnull(length(cast(volume_title as blob)), 0) +
                ifnull(length(cast(chapter_id_list as blob)), 0) +
                4
            ) as bytes
        from volume
        """
    )
    suspend fun getVolumeStorageRows(): List<VolumeStorageRow>

    @Query(
        """
        select
            id as id,
            (
                ifnull(length(cast(id as blob)), 0) +
                ifnull(length(cast(title as blob)), 0)
            ) as bytes
        from chapter_information
        """
    )
    suspend fun getChapterInformationBytes(): List<StorageBytes>

    @Query(
        """
        select
            id as id,
            (
                ifnull(length(cast(id as blob)), 0) +
                ifnull(length(cast(title as blob)), 0) +
                ifnull(length(cast(content as blob)), 0) +
                ifnull(length(cast(lastChapter as blob)), 0) +
                ifnull(length(cast(nextChapter as blob)), 0)
            ) as bytes
        from chapter_content
        """
    )
    suspend fun getChapterContentBytes(): List<StorageBytes>

    @Query(
        """
        select
            id as id,
            (
                ifnull(length(cast(id as blob)), 0) +
                ifnull(length(cast(last_read_time as blob)), 0) +
                ifnull(length(cast(total_read_time as blob)), 0) +
                ifnull(length(cast(reading_progress as blob)), 0) +
                ifnull(length(cast(last_read_chapter_id as blob)), 0) +
                ifnull(length(cast(last_read_chapter_title as blob)), 0) +
                ifnull(length(cast(current_chapter_reading_progress_map as blob)), 0) +
                ifnull(length(cast(max_chapter_reading_progress_map as blob)), 0)
            ) as bytes
        from user_reading_data
        """
    )
    suspend fun getUserReadingBytes(): List<StorageBytes>

    @Query(
        """
        select ifnull(sum(
            ifnull(length(cast(id as blob)), 0) +
            ifnull(length(cast(title as blob)), 0) +
            ifnull(length(cast(subtitle as blob)), 0) +
            ifnull(length(cast(cover_uri as blob)), 0) +
            ifnull(length(cast(author as blob)), 0) +
            ifnull(length(cast(description as blob)), 0) +
            ifnull(length(cast(tags as blob)), 0) +
            ifnull(length(cast(publishing_house as blob)), 0) +
            ifnull(length(cast(word_count as blob)), 0) +
            ifnull(length(cast(last_update as blob)), 0) +
            1
        ), 0) as bytes
        from book_information
        """
    )
    suspend fun totalBookInformationBytes(): Long?

    @Query(
        """
        select ifnull(sum(
            ifnull(length(cast(book_id as blob)), 0) +
            ifnull(length(cast(volume_id as blob)), 0) +
            ifnull(length(cast(volume_title as blob)), 0) +
            ifnull(length(cast(chapter_id_list as blob)), 0) +
            4
        ), 0) as bytes
        from volume
        """
    )
    suspend fun totalVolumeBytes(): Long?

    @Query(
        """
        select ifnull(sum(
            ifnull(length(cast(id as blob)), 0) +
            ifnull(length(cast(title as blob)), 0)
        ), 0) as bytes
        from chapter_information
        """
    )
    suspend fun totalChapterInformationBytes(): Long?
}
