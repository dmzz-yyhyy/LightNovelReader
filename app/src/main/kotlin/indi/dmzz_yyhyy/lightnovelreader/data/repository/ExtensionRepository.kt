package indi.dmzz_yyhyy.lightnovelreader.data.repository

import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import kotlinx.coroutines.flow.Flow

interface ExtensionRepository {
    fun getAllExtensions(): Flow<List<ExtensionEntity>>
    fun getExtensionsByRepoId(repoId: Int): Flow<List<ExtensionEntity>>
    suspend fun getExtensionById(repoId: Int, id: Int): ExtensionEntity?
    suspend fun insertExtension(extension: ExtensionEntity): Long
    suspend fun updateExtension(extension: ExtensionEntity)
    suspend fun deleteExtension(extension: ExtensionEntity)
    suspend fun deleteExtensionsByRepoId(repoId: Int)
    
    // Installed extensions
    fun getAllInstalledExtensions(): Flow<List<InstalledExtensionEntity>>
    fun getInstalledExtensionById(id: Int): Flow<InstalledExtensionEntity?>
    fun getEnabledExtensions(): Flow<List<InstalledExtensionEntity>>
    suspend fun getInstalledExtensionByIdSync(id: Int): InstalledExtensionEntity?
    suspend fun insertInstalledExtension(extension: InstalledExtensionEntity): Long
    suspend fun updateInstalledExtension(extension: InstalledExtensionEntity)
    suspend fun deleteInstalledExtension(extension: InstalledExtensionEntity)
    suspend fun deleteInstalledExtensionById(id: Int)
    suspend fun isExtensionInstalled(id: Int): Boolean
}
