package indi.dmzz_yyhyy.lightnovelreader.ui.storagemanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface StorageManagerUiState {
    val isLoading: Boolean
    val totalSize: Long
    val calculatedAt: Long
    val bookCount: Int
    val sections: List<StorageManagerSection>
    val expandedTitle: Int?
    val load: () -> Unit
    val selectSection: (Int) -> Unit
}

class MutableStorageManagerUiState : StorageManagerUiState {
    override var load: () -> Unit = {}
    override var selectSection: (Int) -> Unit = {}
    override var isLoading by mutableStateOf(true)
    override var totalSize by mutableLongStateOf(0L)
    override var calculatedAt by mutableLongStateOf(0L)
    override var bookCount by mutableIntStateOf(0)
    override var expandedTitle by mutableStateOf<Int?>(null)
    override var sections by mutableStateOf(emptyList<StorageManagerSection>())
}
