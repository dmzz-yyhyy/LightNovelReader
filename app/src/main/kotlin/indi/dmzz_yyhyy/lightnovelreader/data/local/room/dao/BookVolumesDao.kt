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
import io.nightfish.lightnovelreader.api.identifier.Identifier

@Dao
interface BookVolumesDao {
    @Query(
        "replace into volume (book_id, volume_id, volume_title, chapter_id_list, volume_index)" +
                " values (:bookId, :volumeId, :volumeTitle, :chapterIds, :index)"
    )
    suspend fun insertVolume(
        bookId: Identifier.Book,
        volumeId: Identifier.Volume,
        volumeTitle: String,
        chapterIds: String,
        index: Int
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVolume(entity: VolumeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapterInformation(chapterInformationEntity: ChapterInformationEntity)

    @Query("select * from chapter_information where id = :id")
    suspend fun getChapterInformation(id: Identifier.Chapter): ChapterInformation?

    @Query("select * from chapter_information where id = :id")
    suspend fun getChapterInformationEntity(id: Identifier.Chapter): ChapterInformationEntity?

    @Transaction
    suspend fun insertVolume(bookId: Identifier.Book, volumes: BookVolumes) {
        volumes.volumes.forEachIndexed { index, volume ->
            insertVolume(
                bookId,
                volume.volumeId,
                volume.volumeTitle,
                ListConverter.chapterListToString(volume.chapters.map { it.id }),
                index
            )
            volume.chapters.forEach {
                insertChapterInformation(ChapterInformationEntity(it.id, it.title))
            }
        }
    }

    @Query("select * from volume where volume_id = :volumeId")
    suspend fun getVolumeEntity(volumeId: Identifier.Volume): VolumeEntity?

    @Query("select * from volume where book_id = :bookId")
    suspend fun getVolumeEntitiesByBookId(bookId: Identifier.Book): List<VolumeEntity>

    @Query("select * from volume where book_id in (:bookIds)")
    suspend fun getVolumeEntitiesByBookIds(bookIds: List<Identifier.Book>): List<VolumeEntity>

    @Transaction
    suspend fun getBookVolumes(bookId: Identifier.Book): BookVolumes? {
        val volumes = getVolumeEntitiesByBookId(bookId)
        if (volumes.isEmpty()) return null
        return BookVolumes(
            bookId,
            volumes
                .sortedBy { it.index }
                .map { volumeEntity ->
                    Volume(
                        volumeEntity.volumeId,
                        volumeEntity.volumeTitle,
                        volumeEntity.chapterIds.map {
                            getChapterInformation(it) ?: ChapterInformation(it, "")
                        })
                })
    }

    @Query("delete from volume WHERE book_id GLOB 'book:' || :namespace || ':*'")
    suspend fun clearVolumes(namespace: String)

    @Query("delete from volume where book_id in (:bookIds)")
    suspend fun deleteByBookIds(bookIds: List<Identifier.Book>)

    @Query("delete from chapter_information WHERE id GLOB 'chapter:' || :namespace || ':*'")
    suspend fun clearChapterInformation(namespace: String)

    @Query("delete from chapter_information where id in (:chapterIds)")
    suspend fun deleteChapterInformationByIds(chapterIds: List<Identifier.Chapter>)

    @Transaction
    suspend fun clear(namespace: String) {
        clearVolumes(namespace)
        clearChapterInformation(namespace)
    }

    @Transaction
    suspend fun insertVolumeEntities(vararg entities: VolumeEntity) {
        for (entity in entities) {
            insertVolume(entity)
        }
    }

    @Transaction
    suspend fun insertChapterInformationEntities(vararg entities: ChapterInformationEntity) {
        for (entity in entities) {
            insertChapterInformation(entity)
        }
    }

    @Query("select * from chapter_information WHERE id GLOB 'chapter:' || :namespace || ':*'")
    suspend fun getAllChapterInformationEntities(namespace: String): List<ChapterInformationEntity>

    @Query("select * from volume WHERE book_id GLOB 'book:' || :namespace || ':*'")
    suspend fun getAllVolumeEntities(namespace: String): List<VolumeEntity>
}
