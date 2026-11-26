package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import io.nightfish.lightnovelreader.api.identifier.Identifier

@Entity(tableName = "datasource")
@BackupTable(BackupGroup.METADATA, "id", "datasource")
data class DataSourceEntity(
    @PrimaryKey
    val id: Identifier.DataSource
) : Mergeable<DataSourceEntity> {
    override fun merge(new: DataSourceEntity) = new
}
