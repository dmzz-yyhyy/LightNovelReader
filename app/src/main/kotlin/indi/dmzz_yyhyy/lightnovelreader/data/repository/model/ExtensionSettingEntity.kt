package indi.dmzz_yyhyy.lightnovelreader.data.repository.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for storing extension-specific settings
 */
@Entity(
    tableName = "extension_settings",
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
        Index(value = ["extensionId", "key"], unique = true)
    ]
)
data class ExtensionSettingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val extensionId: Int,
    val key: String,
    val value: String,
    val type: SettingType,
    val defaultValue: String = "",
    val title: String = "",
    val summary: String = "",
    val isVisible: Boolean = true
)

enum class SettingType {
    STRING,
    BOOLEAN,
    INT,
    FLOAT,
    LIST // For dropdown selections
}
