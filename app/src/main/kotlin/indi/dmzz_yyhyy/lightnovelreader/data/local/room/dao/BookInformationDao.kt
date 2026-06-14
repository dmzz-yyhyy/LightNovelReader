package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation

@Dao
interface BookInformationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(information: BookInformationEntity)

    @Transaction
    fun insert(information: BookInformation) {
        insert(
            BookInformationEntity(
                id = information.id,
                title = information.title,
                subtitle = information.subtitle,
                coverUri = information.coverUri,
                author = information.author,
                description = information.description,
                tags = information.tags,
                publishingHouse = information.publishingHouse,
                wordCount = information.wordCount,
                lastUpdated = information.lastUpdated,
                isComplete = information.isComplete
            )
        )
    }

    @Query("select * from book_information where id=:id")
    suspend fun getEntity(id: String): BookInformationEntity?

    @Query("select * from book_information")
    suspend fun getAllEntities(): List<BookInformationEntity>

    @Query("delete from book_information where id in (:ids)")
    fun deleteByIds(ids: List<String>)

    @Transaction
    suspend fun get(id: String): BookInformation? {
        val entity = getEntity(id) ?: return null
        return MutableBookInformation(
            entity.id,
            entity.title,
            entity.subtitle,
            entity.coverUri,
            entity.author,
            entity.description,
            entity.tags,
            entity.publishingHouse,
            entity.wordCount,
            entity.lastUpdated,
            entity.isComplete,
        )
    }

    @Transaction
    suspend fun has(id: String): Boolean {
        return get(id) != null
    }

    @Query("delete from book_information")
    fun clear()
}
