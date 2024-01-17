package indi.dmzz_yyhyy.lightnovelreader.data.room.entity;

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookVolume(
        @PrimaryKey val id: Int,
        val bookId: Int,
        val volumeIndex: Int,
        val volumeName: String

)
