package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.FormattingRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FormattingRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleEntity(rule: FormattingRuleEntity)

    @Query("select * from formatting_rule")
    fun getAllBookRuleEntityFlow(): Flow<List<FormattingRuleEntity>>

    @Query("select * from formatting_rule")
    suspend fun getAllBookRuleEntity(): List<FormattingRuleEntity>

    @Query("select * from formatting_rule where book_id = :bookId")
    fun getBookRuleEntityFlow(bookId: Int): Flow<List<FormattingRuleEntity>>

    @Query("replace into formatting_rule (id, book_id, name, is_regex, `match`, replacement, is_enabled) " +
            "values (:id, :bookId, :name, :isRegex, :match, :replacement, :isEnabled)")
    suspend fun update(id: Int, bookId: Int, name: String, isRegex: Boolean, match: String, replacement: String, isEnabled: Boolean)

    @Query("select * from formatting_rule where id = :id")
    suspend fun getBookRuleEntity(id: Int): FormattingRuleEntity

    @Query("delete from formatting_rule where id = :id")
    suspend fun deleteRule(id: Int)
}