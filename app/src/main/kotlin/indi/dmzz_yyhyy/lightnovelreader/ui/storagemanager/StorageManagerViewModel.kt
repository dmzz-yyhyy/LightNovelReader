package indi.dmzz_yyhyy.lightnovelreader.ui.storagemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.storage.StorageUsageRepository
import indi.dmzz_yyhyy.lightnovelreader.data.storage.StorageUsageSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StorageManagerViewModel @Inject constructor(
    private val storageUsageRepository: StorageUsageRepository
) : ViewModel() {
    val uiState = MutableStorageManagerUiState().apply {
        load = ::load
        selectSection = ::selectSection
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            storageUsageRepository.getCachedSnapshot()?.let { updateStorageOverview(it, false) }
            refresh()
        }
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            refresh()
        }
    }

    fun selectSection(title: Int) {
        uiState.expandedTitle = title
    }

    private suspend fun refresh() {
        withContext(Dispatchers.Main) {
            uiState.isLoading = true
        }
        updateStorageOverview(storageUsageRepository.refreshSnapshot(), false)
    }

    private suspend fun updateStorageOverview(snapshot: StorageUsageSnapshot, loading: Boolean) {
        val sections = listOf(
            StorageManagerSection(
                title = R.string.storage_manager_section_app_title,
                description = R.string.storage_manager_section_app_description,
                size = snapshot.appBytes
            ),
            StorageManagerSection(
                title = R.string.storage_manager_section_database_title,
                description = R.string.storage_manager_section_database_description,
                size = snapshot.databaseDiskBytes
            ),
            StorageManagerSection(
                title = R.string.storage_manager_section_plugins_title,
                description = R.string.storage_manager_section_plugins_description,
                size = snapshot.pluginBytes
            ),
            StorageManagerSection(
                title = R.string.storage_manager_section_cache_title,
                description = R.string.storage_manager_section_cache_description,
                size = snapshot.cacheBytes
            ),
            StorageManagerSection(
                title = R.string.storage_manager_section_other_title,
                description = R.string.storage_manager_section_other_description,
                size = snapshot.otherFileBytes
            )
        ).filter { it.size > 0L }

        withContext(Dispatchers.Main) {
            uiState.isLoading = loading
            uiState.totalSize = snapshot.totalBytes
            uiState.calculatedAt = snapshot.calculatedAt
            uiState.bookCount = snapshot.books.count { it.totalBytes > 0L }
            uiState.sections = sections
            if (uiState.expandedTitle !in sections.map(StorageManagerSection::title)) {
                uiState.expandedTitle = sections.firstOrNull()?.title
            }
        }
    }
}
