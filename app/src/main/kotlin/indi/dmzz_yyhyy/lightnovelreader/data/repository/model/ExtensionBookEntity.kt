package indi.dmzz_yyhyy.lightnovelreader.data.repository.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for tracking books that come from extensions
 */
@Entity(
    tableName = "extension_books",
    foreignKeys = [
        ForeignKey(
            entity = InstalledExtensionEntity::class,
            parentColumns = ["id"],
            childColumns = ["extensionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("extensionId"),
        Index("originalBookId"),
        Index(value = ["extensionId", "originalBookId"], unique = true)
    ]
)
data class ExtensionBookEntity(
    @PrimaryKey
    val internalBookId: Int,
    val extensionId: Int,
    val originalBookId: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val url: String = "",
    val isInLibrary: Boolean = false,
    val addedToLibraryDate: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
