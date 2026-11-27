package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfBookMetadataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfEntity
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfBookMetadata
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BookshelfDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookshelf(bookshelfEntity: BookshelfEntity)

    @Insert
    suspend fun createBookshelf(bookshelfEntity: BookshelfEntity)

    @Query("delete from book_shelf where id=:id")
    suspend fun deleteBookshelf(id: Identifier.Bookshelf)

    @Query("select * from book_shelf where id=:id")
    suspend fun getBookshelf(id: Identifier.Bookshelf): BookshelfEntity?

    @Query("select * from book_shelf where id=:id")
    fun getBookShelfFlow(id: Identifier.Bookshelf): Flow<BookshelfEntity?>

    @Query("select * from book_shelf WHERE id GLOB 'bookshelf:' || :namespace || ':*'")
    fun getAllBookshelvesFlow(namespace: String): Flow<List<BookshelfEntity>>

    @Query("select * from book_shelf WHERE id GLOB 'bookshelf:' || :namespace || ':*'")
    suspend fun getAllBookshelves(namespace: String): List<BookshelfEntity>

    @Query("select * from book_shelf_book_metadata WHERE id GLOB 'book:' || :namespace || ':*'")
    suspend fun getAllBookshelfBookMetadataEntities(namespace: String): List<BookshelfBookMetadataEntity>

    @Transaction
    suspend fun getAllBookshelfBookMetadata(namespace: String): List<BookshelfBookMetadata> =
        getAllBookshelfBookMetadataEntities(namespace)
            .map {
                BookshelfBookMetadata(
                    it.id,
                    it.lastUpdate,
                    it.bookShelfIds
                )
            }

    @Query("select * from book_shelf_book_metadata where id=:id")
    suspend fun getBookshelfBookMetadataEntity(id: Identifier.Book): BookshelfBookMetadataEntity?

    @Query("select * from book_shelf_book_metadata where id=:id")
    fun getBookshelfBookMetadataEntityFlow(id: Identifier.Book): Flow<BookshelfBookMetadataEntity?>

    @TypeConverters(LocalDateTimeConverter::class, ListConverter::class)
    @Query(
        "replace into book_shelf_book_metadata (id, last_update, book_shelf_ids)" +
                " values (:id, :lastUpdate, :bookshelfIds)"
    )
    suspend fun insertBookshelfBookMetadata(
        id: Identifier.Book,
        lastUpdate: LocalDateTime,
        bookshelfIds: String,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookshelfBookMetadata(entity: BookshelfBookMetadataEntity)

    @Query("select id from book_shelf WHERE id GLOB 'bookshelf:' || :namespace || ':*'")
    suspend fun getAllBookshelfIds(namespace: String): List<Identifier.Bookshelf>

    @Transaction
    suspend fun getBookshelfBookMetadata(id: Identifier.Book): BookshelfBookMetadata? =
        getBookshelfBookMetadataEntity(id)?.let {
            BookshelfBookMetadata(
                it.id,
                it.lastUpdate,
                it.bookShelfIds
            )
        }

    @Transaction
    suspend fun addBookshelfMetadata(
        id: Identifier.Book,
        lastUpdate: LocalDateTime,
        bookshelfIds: List<Identifier.Bookshelf>
    ) {
        getBookshelfBookMetadataEntity(id).let {
            if (it == null)
                insertBookshelfBookMetadata(
                    id,
                    lastUpdate,
                    ListConverter.bookshelfListToString(bookshelfIds)
                )
            else
                insertBookshelfBookMetadata(
                    id,
                    lastUpdate,
                    ListConverter.bookshelfListToString((bookshelfIds + it.bookShelfIds).distinct())
                )
        }
    }

    @Query("delete from book_shelf_book_metadata where id=:id")
    suspend fun deleteBookshelfBookMetadata(id: Identifier.Book)

    @Query("select * from book_shelf_book_metadata WHERE id GLOB 'book:' || :namespace || ':*'")
    fun getAllBookshelfBookEntitiesFlow(namespace: String): Flow<List<BookshelfBookMetadataEntity>>

    @Query("select * from book_shelf_book_metadata WHERE id GLOB 'book:' || :namespace || ':*'")
    suspend fun getAllBookshelfBookEntities(namespace: String): List<BookshelfBookMetadataEntity>

    @Query("select id from book_shelf_book_metadata WHERE id GLOB 'book:' || :namespace || ':*'")
    fun getAllBookshelfBookIdsFlow(namespace: String): Flow<List<Identifier.Book>>

    @Query("delete from book_shelf WHERE id GLOB 'bookshelf:' || :namespace || ':*'")
    suspend fun clearBookshelf(namespace: String)

    @Query("delete from book_shelf_book_metadata WHERE id GLOB 'book:' || :namespace || ':*'")
    suspend fun clearBookshelfBookMetadata(namespace: String)

    @Transaction
    suspend fun clear(namespace: String) {
        clearBookshelf(namespace)
        clearBookshelfBookMetadata(namespace)
    }
}
