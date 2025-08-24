package indi.dmzz_yyhyy.lightnovelreader.data.repository.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an installed extension - matches Shosetsu's schema
 */
@Entity(tableName = "installed_extensions")
data class InstalledExtensionEntity(
    @PrimaryKey
    val id: Int,
    val repoId: Int,
    val name: String,
    val fileName: String,
    val imageURL: String,
    val lang: String,
    val version: String,
    val md5: String,
    val type: String, // ExtensionType as string ("LuaScript", etc.)
    val description: String = "",
    val isEnabled: Boolean = true,
    val installDate: Long = System.currentTimeMillis()
)
