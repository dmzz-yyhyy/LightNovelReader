package indi.dmzz_yyhyy.lightnovelreader.data.extensions.settings

import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.ExtensionSettingDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionSettingEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.SettingType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages extension-specific settings
 */
@Singleton
class ExtensionSettingsManager @Inject constructor(
    private val extensionSettingDao: ExtensionSettingDao
) {

    /**
     * Get all settings for an extension
     */
    fun getExtensionSettings(extensionId: Int): Flow<List<ExtensionSettingEntity>> {
        return extensionSettingDao.getExtensionSettings(extensionId)
    }

    /**
     * Get a specific setting value
     */
    suspend fun getSettingValue(extensionId: Int, key: String, defaultValue: String = ""): String {
        return extensionSettingDao.getSettingValue(extensionId, key) ?: defaultValue
    }

    /**
     * Get a boolean setting value
     */
    suspend fun getBooleanSetting(extensionId: Int, key: String, defaultValue: Boolean = false): Boolean {
        val value = getSettingValue(extensionId, key, defaultValue.toString())
        return value.toBoolean()
    }

    /**
     * Get an integer setting value
     */
    suspend fun getIntSetting(extensionId: Int, key: String, defaultValue: Int = 0): Int {
        val value = getSettingValue(extensionId, key, defaultValue.toString())
        return value.toIntOrNull() ?: defaultValue
    }

    /**
     * Get a float setting value
     */
    suspend fun getFloatSetting(extensionId: Int, key: String, defaultValue: Float = 0f): Float {
        val value = getSettingValue(extensionId, key, defaultValue.toString())
        return value.toFloatOrNull() ?: defaultValue
    }

    /**
     * Update a setting value
     */
    suspend fun updateSetting(extensionId: Int, key: String, value: String) {
        val existing = extensionSettingDao.getExtensionSetting(extensionId, key)
        if (existing != null) {
            extensionSettingDao.updateSettingValue(extensionId, key, value)
        } else {
            // Create new setting
            val setting = ExtensionSettingEntity(
                extensionId = extensionId,
                key = key,
                value = value,
                type = SettingType.STRING // Default type
            )
            extensionSettingDao.insertSetting(setting)
        }
    }

    /**
     * Initialize default settings for an extension
     */
    suspend fun initializeExtensionSettings(extensionId: Int, defaultSettings: List<ExtensionSettingDefinition>) {
        defaultSettings.forEach { definition ->
            val existing = extensionSettingDao.getExtensionSetting(extensionId, definition.key)
            if (existing == null) {
                val setting = ExtensionSettingEntity(
                    extensionId = extensionId,
                    key = definition.key,
                    value = definition.defaultValue,
                    type = definition.type,
                    defaultValue = definition.defaultValue,
                    title = definition.title,
                    summary = definition.summary,
                    isVisible = definition.isVisible
                )
                extensionSettingDao.insertSetting(setting)
            }
        }
    }

    /**
     * Delete all settings for an extension
     */
    suspend fun deleteExtensionSettings(extensionId: Int) {
        extensionSettingDao.deleteExtensionSettings(extensionId)
    }

    data class ExtensionSettingDefinition(
        val key: String,
        val defaultValue: String,
        val type: SettingType,
        val title: String,
        val summary: String,
        val isVisible: Boolean = true
    )
}
