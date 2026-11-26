package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import io.nightfish.lightnovelreader.api.identifier.Identifier

data class ExportSettings(
    val selectedVolumeIds: Set<Identifier.Volume> = emptySet(),
    val includeImages: Boolean = true,
    val exportType: ExportType = ExportType.BOOK
)

enum class ExportType {
    BOOK,
    VOLUMES
}
