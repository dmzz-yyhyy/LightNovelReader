package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SwitchChip
import io.nightfish.lightnovelreader.api.book.BookVolumes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBottomSheet(
    sheetState: SheetState,
    bookVolumes: BookVolumes,
    settings: ExportSettings,
    onSettingsChange: (ExportSettings) -> Unit,
    onDismissRequest: () -> Unit,
    onClickExport: (ExportSettings) -> Unit
) {
    val isSplitEnabled = settings.selectedVolumeIds.isNotEmpty()
    val allVolumeIds = bookVolumes.volumes.map { it.volumeId }.toSet()

    val selectedVolumeIds = if (isSplitEnabled) {
        settings.selectedVolumeIds
    } else {
        allVolumeIds
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        tonalElevation = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.export_epub),
                    style = typography.displayMedium
                )
                Spacer(Modifier.width(16.dp))
                Button(onClick = {
                    if (isSplitEnabled) {
                        onClickExport(settings.copy(selectedVolumeIds = selectedVolumeIds, exportType = ExportType.VOLUMES))
                    } else {
                        onClickExport(settings.copy(selectedVolumeIds = emptySet(), exportType = ExportType.BOOK))
                    }
                    onDismissRequest()
                }) {
                    Text(stringResource(R.string.export), fontWeight = FontWeight.SemiBold)
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                item {
                    SwitchChip(
                        label = stringResource(R.string.export_include_images),
                        selected = settings.includeImages,
                        onClick = {
                            onSettingsChange(settings.copy(includeImages = !settings.includeImages))
                        }
                    )
                }

                item {
                    SwitchChip(
                        label = stringResource(R.string.export_split_volumes),
                        selected = isSplitEnabled,
                        onClick = {
                            onSettingsChange(
                                settings.copy(
                                    selectedVolumeIds = if (isSplitEnabled) emptySet() else allVolumeIds
                                )
                            )
                        }
                    )
                }

                if (isSplitEnabled) {
                    val isAllSelected = selectedVolumeIds.size == allVolumeIds.size
                    if (!isAllSelected) {
                        item {
                            SwitchChip(
                                label = stringResource(R.string.select_all),
                                selected = false,
                                onClick = {
                                    onSettingsChange(settings.copy(selectedVolumeIds = allVolumeIds))
                                }
                            )
                        }
                        item {
                            SwitchChip(
                                label = stringResource(R.string.invert_selection),
                                selected = false,
                                onClick = {
                                    val toggled = allVolumeIds - selectedVolumeIds
                                    onSettingsChange(settings.copy(selectedVolumeIds = toggled))
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 18.dp))

            LazyColumn {
                items(bookVolumes.volumes) { volume ->
                    val isChecked = volume.volumeId in selectedVolumeIds

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isSplitEnabled) {
                                if (isSplitEnabled) {
                                    val updated = if (isChecked) {
                                        selectedVolumeIds - volume.volumeId
                                    } else {
                                        selectedVolumeIds + volume.volumeId
                                    }
                                    onSettingsChange(settings.copy(selectedVolumeIds = updated))
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f, fill = true),
                            text = volume.volumeTitle,
                            style = typography.titleMedium,
                            color = if (isSplitEnabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (isSplitEnabled) {
                                    val updated = if (isChecked) {
                                        selectedVolumeIds - volume.volumeId
                                    } else {
                                        selectedVolumeIds + volume.volumeId
                                    }
                                    onSettingsChange(settings.copy(selectedVolumeIds = updated))
                                }
                            },
                            enabled = isSplitEnabled
                        )
                    }
                }
            }
        }
    }
}