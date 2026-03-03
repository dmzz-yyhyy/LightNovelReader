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
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BookshelfDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookshelf(bookshelfEntity: BookshelfEntity)

    @Insert
    fun createBookshelf(bookshelfEntity: BookshelfEntity)

    @Query("delete from book_shelf where id=:id")
    fun deleteBookshelf(id: Int)

    @Query("select * from book_shelf where id=:id")
    fun getBookshelf(id: Int): BookshelfEntity?

    @Query("select * from book_shelf where id=:id")
    fun getBookShelfFlow(id: Int): Flow<BookshelfEntity?>

    @Query("select * from book_shelf")
    fun getAllBookshelvesFlow(): Flow<List<BookshelfEntity>>

    @Query("select * from book_shelf")
    fun getAllBookshelves(): List<BookshelfEntity>

    @Query("select * from book_shelf_book_metadata")
    fun getAllBookshelfBookMetadataEntities(): List<BookshelfBookMetadataEntity>

    @Transaction
    fun getAllBookshelfBookMetadata(): List<BookshelfBookMetadata> = getAllBookshelfBookMetadataEntities()
        .map {
            BookshelfBookMetadata(
                it.id,
                it.lastUpdate,
                it.bookShelfIds
            )
        }

    @Query("select * from book_shelf_book_metadata where id=:id")
    fun getBookshelfBookMetadataEntity(id: String): BookshelfBookMetadataEntity?

    @Query("select * from book_shelf_book_metadata where id=:id")
    fun getBookshelfBookMetadataEntityFlow(id: String): Flow<BookshelfBookMetadataEntity?>

    @TypeConverters(LocalDateTimeConverter::class, ListConverter::class)
    @Query("replace into book_shelf_book_metadata (id, last_update, book_shelf_ids)" +
            " values (:id, :lastUpdate, :bookshelfIds)")
    fun insertBookshelfBookMetadata(
        id: String,
        lastUpdate: LocalDateTime,
        bookshelfIds: String,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookshelfBookMetadata(entity: BookshelfBookMetadataEntity)

    @Query("select id from book_shelf")
    fun getAllBookshelfIds(): List<Int>

    @Transaction
    fun getBookshelfBookMetadata(id: String): BookshelfBookMetadata? = getBookshelfBookMetadataEntity(id)?.let {
        BookshelfBookMetadata(
            it.id,
            it.lastUpdate,
            it.bookShelfIds
        )
    }

    @Transaction
    fun addBookshelfMetadata(
        id: String,
        lastUpdate: LocalDateTime,
        bookshelfIds: List<Int>
    ) {
        getBookshelfBookMetadataEntity(id).let {
            if ( it == null)
                insertBookshelfBookMetadata(id, lastUpdate, ListConverter.intListToString(bookshelfIds))
            else
                insertBookshelfBookMetadata(id, lastUpdate, ListConverter.intListToString((bookshelfIds + it.bookShelfIds).distinct()))
        }
    }

    @Query("delete from book_shelf_book_metadata where id=:id")
    fun deleteBookshelfBookMetadata(id: String)

    @Query("select * from book_shelf_book_metadata")
    fun getAllBookshelfBookEntitiesFlow(): Flow<List<BookshelfBookMetadataEntity>>

    @Query("select * from book_shelf_book_metadata")
    fun getAllBookshelfBookEntities(): List<BookshelfBookMetadataEntity>

    @Query("select id from book_shelf_book_metadata")
    fun getAllBookshelfBookIdsFlow(): Flow<List<String>>

    @Query("delete from book_shelf")
    fun clearBookshelf()

    @Query("delete from book_shelf_book_metadata")
    fun clearBookshelfBookMetadata()

    @Transaction
    fun clear() {
        clearBookshelf()
        clearBookshelfBookMetadata()
    }
}