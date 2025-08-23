package indi.dmzz_yyhyy.lightnovelreader.data.repository.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing an extension in a repository
 */
@Entity(
    tableName = "repository_extensions",
    foreignKeys = [
        ForeignKey(
            entity = RepositoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["repoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("repoId")],
    primaryKeys = ["repoId", "id"]
)
data class ExtensionEntity(
    val id: Int,
    val repoId: Int,
    val name: String,
    val fileName: String,
    val imageURL: String,
    val lang: String,
    val version: String,
    val md5: String,
    val type: String,
    val description: String = ""
)
