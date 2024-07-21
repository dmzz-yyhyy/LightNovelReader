package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.IntListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity.VolumeEntity

@Dao
interface BookVolumesDao {
    @TypeConverters(IntListConverter::class)
    @Query("replace into volume (book_id, volume_id, volume_title, chapter_id_list)" +
            " values (:bookId, :volumeId, :volumeTitle, :chapterIds)")
    suspend fun update(bookId: Int, volumeId: Int, volumeTitle: String, chapterIds: String)

    @Query("replace into chapter_information (id, title) values (:id, :title)")
    suspend fun updateChapterInformation(id: Int, title: String)

    @Query("select * from chapter_information where id = :id")
    suspend fun getChapterInformation(id: Int): ChapterInformation?

    @Transaction
    suspend fun update(bookId: Int, volumes: BookVolumes) {
        volumes.volumes.forEach { volume ->
            update(bookId, volume.volumeId, volume.volumeTitle, volume.chapters.map { it.id }.joinToString(","))
            volume.chapters.forEach {
                updateChapterInformation(it.id, it.title)
            }
        }
    }

    @Query("select * from volume where volume_id = :volumeId")
    suspend fun getVolumeEntity(volumeId: Int): VolumeEntity?

    @Query("select * from volume where book_id = :bookId")
    suspend fun getVolumeEntitiesByBookId(bookId: Int): List<VolumeEntity>?

    @Transaction
    suspend fun getBookVolumes(bookId: Int): BookVolumes? {
        return getVolumeEntitiesByBookId(bookId)?.let { volumeEntities ->
            BookVolumes(volumeEntities.map { volumeEntity ->
                Volume(volumeEntity.volumeId, volumeEntity.volumeTitle, volumeEntity.chapterIds.map {
                    getChapterInformation(it) ?: ChapterInformation(0, "")
                })
            })
        }
    }
}