package indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.IntListConverter

@TypeConverters(IntListConverter::class)
@Entity(tableName = "book_volumes")
data class BookVolumesEntity(
    @PrimaryKey
    val id: Int,
    val volumes: List<Int>
)
