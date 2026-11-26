package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import io.nightfish.lightnovelreader.api.identifier.Identifier

@Entity(tableName = "formatting_rule")
@BackupTable(BackupGroup.SETTINGS, "book_id", "book")
data class FormattingRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name = "book_id")
    val bookId: Identifier.Book,
    val name: String,
    @ColumnInfo(name = "is_regex")
    val isRegex: Boolean,
    val match: String,
    val replacement: String,
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean
) : Mergeable<FormattingRuleEntity> {
    override fun merge(new: FormattingRuleEntity): FormattingRuleEntity = new
}
