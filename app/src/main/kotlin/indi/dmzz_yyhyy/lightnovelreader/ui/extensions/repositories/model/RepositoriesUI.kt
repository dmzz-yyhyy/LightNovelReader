package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.repositories.model

import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepositoryEntity

data class RepositoriesUI(
    val repositories: List<RepositoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
