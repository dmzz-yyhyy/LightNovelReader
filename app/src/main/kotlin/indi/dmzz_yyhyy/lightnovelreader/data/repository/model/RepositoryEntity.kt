package indi.dmzz_yyhyy.lightnovelreader.data.repository.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a repository that contains extensions
 */
@Entity(tableName = "repositories")
data class RepositoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val url: String,
    val isEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
