package indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_information")
data class ChapterInformationEntity(
    @PrimaryKey
    val id: Int,
    val title: String
)