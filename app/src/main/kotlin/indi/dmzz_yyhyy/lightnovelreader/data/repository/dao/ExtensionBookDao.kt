package indi.dmzz_yyhyy.lightnovelreader.data.repository.dao

import androidx.room.*
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtensionBookDao {
    
    @Query("SELECT * FROM extension_books")
    fun getAllExtensionBooks(): Flow<List<ExtensionBookEntity>>
    
    @Query("SELECT * FROM extension_books WHERE extensionId = :extensionId")
    fun getBooksByExtension(extensionId: Int): Flow<List<ExtensionBookEntity>>
    
    @Query("SELECT * FROM extension_books WHERE internalBookId = :bookId")
    suspend fun getExtensionBookById(bookId: Int): ExtensionBookEntity?
    
    @Query("SELECT * FROM extension_books WHERE extensionId = :extensionId AND originalBookId = :originalBookId")
    suspend fun getExtensionBook(extensionId: Int, originalBookId: String): ExtensionBookEntity?
    
    @Query("SELECT * FROM extension_books WHERE isInLibrary = 1")
    fun getLibraryExtensionBooks(): Flow<List<ExtensionBookEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtensionBook(book: ExtensionBookEntity): Long
    
    @Update
    suspend fun updateExtensionBook(book: ExtensionBookEntity)
    
    @Delete
    suspend fun deleteExtensionBook(book: ExtensionBookEntity)
    
    @Query("DELETE FROM extension_books WHERE extensionId = :extensionId")
    suspend fun deleteBooksByExtension(extensionId: Int)
    
    @Query("UPDATE extension_books SET isInLibrary = :isInLibrary, addedToLibraryDate = :date WHERE internalBookId = :bookId")
    suspend fun updateLibraryStatus(bookId: Int, isInLibrary: Boolean, date: Long?)
}
