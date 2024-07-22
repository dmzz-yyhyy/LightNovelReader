package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.loacltion.room.converter.LocalDataTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity.BookInformationEntity
import java.time.LocalDateTime

@Dao
interface BookInformationDao {
    @TypeConverters(LocalDataTimeConverter::class)
    @Query("replace into book_information (id, title, cover_url, author, description, publishing_house, word_count, last_update, is_complete) " +
            "values (:id, :title, :coverUrl, :author, :description, :publishingHouse, :wordCount, :lastUpdated, :isComplete) ")
    suspend fun update(id: Int,
               title: String,
               coverUrl: String,
               author: String,
               description: String,
               publishingHouse: String,
               wordCount: Int,
               lastUpdated: LocalDateTime,
               isComplete: Boolean)

    @Transaction
    suspend fun update(information: BookInformation) {
        return update(
            information.id,
            information.title,
            information.coverUrl,
            information.author,
            information.description,
            information.publishingHouse,
            information.wordCount,
            information.lastUpdated,
            information.isComplete,
        )
    }

    @Query("select * from book_information where id=:id")
    suspend fun getEntity(id: Int): BookInformationEntity?

    @Transaction
    suspend fun get(id: Int): BookInformation? {
        val entity = getEntity(id) ?: return null
        return BookInformation(
            entity.id,
            entity.title,
            entity.coverUrl,
            entity.author,
            entity.description,
            entity.publishingHouse,
            entity.wordCount,
            entity.lastUpdated,
            entity.isComplete,
        )
    }

    @Transaction
    suspend fun has(id: Int): Boolean {
        return get(id) != null
    }
}