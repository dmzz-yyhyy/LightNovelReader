package indi.dmzz_yyhyy.lightnovelreader.data.repository

import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.ExtensionDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.InstalledExtensionDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionRepositoryImpl @Inject constructor(
    private val extensionDao: ExtensionDao,
    private val installedExtensionDao: InstalledExtensionDao
) : ExtensionRepository {

    override fun getAllExtensions(): Flow<List<ExtensionEntity>> =
        extensionDao.getAllExtensions()

    override fun getExtensionsByRepoId(repoId: Int): Flow<List<ExtensionEntity>> =
        extensionDao.getExtensionsByRepoId(repoId)

    override suspend fun getExtensionById(repoId: Int, id: Int): ExtensionEntity? =
        extensionDao.getExtensionById(repoId, id)

    override suspend fun insertExtension(extension: ExtensionEntity): Long =
        extensionDao.insertExtension(extension)

    override suspend fun updateExtension(extension: ExtensionEntity) {
        extensionDao.updateExtension(extension)
    }

    override suspend fun deleteExtension(extension: ExtensionEntity) {
        extensionDao.deleteExtension(extension)
    }

    override suspend fun deleteExtensionsByRepoId(repoId: Int) {
        extensionDao.deleteExtensionsByRepoId(repoId)
    }

    // Installed extensions
    override fun getAllInstalledExtensions(): Flow<List<InstalledExtensionEntity>> =
        installedExtensionDao.getAllInstalledExtensions()

    override fun getInstalledExtensionById(id: Int): Flow<InstalledExtensionEntity?> =
        installedExtensionDao.getInstalledExtensionById(id)

    override fun getEnabledExtensions(): Flow<List<InstalledExtensionEntity>> =
        installedExtensionDao.getEnabledExtensions()

    override suspend fun getInstalledExtensionByIdSync(id: Int): InstalledExtensionEntity? =
        installedExtensionDao.getInstalledExtensionByIdSync(id)

    override suspend fun insertInstalledExtension(extension: InstalledExtensionEntity): Long =
        installedExtensionDao.insertInstalledExtension(extension)

    override suspend fun updateInstalledExtension(extension: InstalledExtensionEntity) {
        installedExtensionDao.updateInstalledExtension(extension)
    }

    override suspend fun deleteInstalledExtension(extension: InstalledExtensionEntity) {
        installedExtensionDao.deleteInstalledExtension(extension)
    }

    override suspend fun deleteInstalledExtensionById(id: Int) {
        installedExtensionDao.deleteInstalledExtensionById(id)
    }

    override suspend fun isExtensionInstalled(id: Int): Boolean =
        installedExtensionDao.isExtensionInstalled(id)
}
