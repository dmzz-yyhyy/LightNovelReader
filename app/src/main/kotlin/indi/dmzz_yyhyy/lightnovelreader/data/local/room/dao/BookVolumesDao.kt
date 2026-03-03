package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.VolumeEntity
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume

@Dao
interface BookVolumesDao {
    @Query("replace into volume (book_id, volume_id, volume_title, chapter_id_list, volume_index)" +
            " values (:bookId, :volumeId, :volumeTitle, :chapterIds, :index)")
    fun insertVolume(bookId: String, volumeId: String, volumeTitle: String, chapterIds: String, index: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVolume(entity: VolumeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChapterInformation(chapterInformationEntity: ChapterInformationEntity)

    @Query("select * from chapter_information where id = :id")
    suspend fun getChapterInformation(id: String): ChapterInformation?

    @Query("select * from chapter_information where id = :id")
    suspend fun getChapterInformationEntity(id: String): ChapterInformationEntity?

    @Transaction
    fun insertVolume(bookId: String, volumes: BookVolumes) {
        volumes.volumes.forEachIndexed { index, volume ->
            insertVolume(bookId, volume.volumeId, volume.volumeTitle, ListConverter.stringListToString(volume.chapters.map { it.id }), index)
            volume.chapters.forEach {
                insertChapterInformation(ChapterInformationEntity(it.id, it.title))
            }
        }
    }

    @Query("select * from volume where volume_id = :volumeId")
    suspend fun getVolumeEntity(volumeId: String): VolumeEntity?

    @Query("select * from volume where book_id = :bookId")
    suspend fun getVolumeEntitiesByBookId(bookId: String): List<VolumeEntity>

    @Transaction
    suspend fun getBookVolumes(bookId: String): BookVolumes? {
        return BookVolumes(
            bookId,
            getVolumeEntitiesByBookId(bookId)
            .sortedBy { it.index }
            .map { volumeEntity ->
                Volume(
                    volumeEntity.volumeId,
                    volumeEntity.volumeTitle,
                    volumeEntity.chapterIds.map {
                        getChapterInformation(it) ?: ChapterInformation("", "")
                    })
        })
    }

    @Query("delete from volume")
    fun clearVolumes()

    @Query("delete from chapter_information")
    fun clearChapterInformation()

    @Transaction
    fun clear() {
        clearVolumes()
        clearChapterInformation()
    }

    @Transaction
    fun insertVolumeEntities(vararg entities: VolumeEntity) {
        for (entity in entities) {
            insertVolume(entity)
        }
    }

    @Transaction
    fun insertChapterInformationEntities(vararg entities: ChapterInformationEntity) {
        for (entity in entities) {
            insertChapterInformation(entity)
        }
    }

    @Query("select * from chapter_information")
    fun getAllChapterInformationEntities(): List<ChapterInformationEntity>

    @Query("select * from volume")
    fun getAllVolumeEntities(): List<VolumeEntity>
}