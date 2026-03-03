package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "formatting_rule")
data class FormattingRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name = "book_id")
    val bookId: String,
    val name: String,
    @ColumnInfo(name = "is_regex")
    val isRegex: Boolean,
    val match: String,
    val replacement: String,
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean
): Mergeable<FormattingRuleEntity> {
    override fun merge(new: FormattingRuleEntity): FormattingRuleEntity = new
}
