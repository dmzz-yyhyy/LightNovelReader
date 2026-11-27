package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.identifier.Identifier

@Dao
interface BookInformationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(information: BookInformationEntity)

    @Transaction
    suspend fun insert(information: BookInformation) {
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
    suspend fun getEntity(id: Identifier.Book): BookInformationEntity?

    @Query("select * from book_information WHERE id GLOB 'book:' || :namespace || ':*'")
    suspend fun getAllEntities(namespace: String): List<BookInformationEntity>

    @Query("delete from book_information where id in (:ids)")
    suspend fun deleteByIds(ids: List<Identifier.Book>)

    @Transaction
    suspend fun get(id: Identifier.Book): BookInformation? {
        val entity = getEntity(id) ?: return null
        return BookInformation(
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
    suspend fun has(id: Identifier.Book): Boolean {
        return get(id) != null
    }

    @Query("delete from book_information WHERE id GLOB 'book:' || :namespace || ':*'")
    suspend fun clear(namespace: String)
}
