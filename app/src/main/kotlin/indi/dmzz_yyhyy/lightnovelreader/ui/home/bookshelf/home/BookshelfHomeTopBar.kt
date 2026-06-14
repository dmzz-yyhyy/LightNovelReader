package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions.BookshelfSortTypeOptions
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfHomeTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    backgroundColor: Color,
    uiState: BookshelfHomeUiState,
    onShareBookshelf: () -> Unit,
    onSaveThisBookshelf: () -> Unit,
    onSaveAllBookshelf: () -> Unit,
    onImportBookshelf: () -> Unit,
) {
    val localDensity = LocalDensity.current
    var mainMenuExpanded by remember { mutableStateOf(false) }
    var exportImportMenuExpanded by remember { mutableStateOf(false) }
    var mainMenuItemHeight by remember { mutableStateOf(0.dp) }
    var exportImportMenuWidth by remember { mutableStateOf(0.dp) }
    val sortLocked = uiState.selectedBookshelf.sortType != BookshelfSortType.Default

    MediumTopAppBar(
        title = {
            AnimatedText(
                text = when {
                    uiState.selectMode -> stringResource(R.string.nav_bookshelf_select_mode, uiState.selectedBookIds.size)
                    else -> stringResource(R.string.nav_bookshelf)
                },
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            AnimatedVisibility(visible = uiState.selectMode) {
                IconButton(
                    onClick = uiState.onDisableSelectMode
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cancel_24px),
                        contentDescription = "cancel"
                    )
                }
            }
        },
        actions = {
            when {
                !uiState.selectMode -> {
                    IconButton(onClick = uiState.onCreate) {
                        Icon(
                            painter = painterResource(R.drawable.library_add_24px),
                            contentDescription = "create"
                        )
                    }
                    Box {
                        var sortMenuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(
                                painter = painterResource(R.drawable.sort_24px),
                                contentDescription = "sort"
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            Text(
                                text = stringResource(R.string.bookshelf_sort_type),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 12.dp, bottom = 6.dp)
                            )
                            BookshelfSortTypeOptions.optionList.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(item.nameId),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    leadingIcon = {
                                        RadioButton(
                                            selected = item.key == uiState.selectedBookshelf.sortType.key,
                                            onClick = null
                                        )
                                    },
                                    onClick = {
                                        uiState.changeSortType(BookshelfSortTypeOptions.getOptionWithValue(item.key).value)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                enabled = uiState.selectedBookshelf.sortType != BookshelfSortType.Default,
                                text = {
                                    Text(
                                        text = stringResource(R.string.bookshelf_sort_reverse),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                leadingIcon = {
                                    Checkbox(
                                        checked = uiState.selectedBookshelf.sortType != BookshelfSortType.Default &&
                                            uiState.selectedBookshelf.sortReversed,
                                        enabled = uiState.selectedBookshelf.sortType != BookshelfSortType.Default,
                                        onCheckedChange = null
                                    )
                                },
                                onClick = {
                                    if (uiState.selectedBookshelf.sortType == BookshelfSortType.Default) return@DropdownMenuItem
                                    uiState.changeSortReversed(!uiState.selectedBookshelf.sortReversed)
                                }
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { mainMenuExpanded = true }) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert_24px),
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                                with(localDensity) {
                                    mainMenuItemHeight = layoutCoordinates.size.height.toDp().div(7)
                                }
                            },
                            expanded = mainMenuExpanded,
                            onDismissRequest = { mainMenuExpanded = false }
                        ) {
                            Text(
                                text = uiState.selectedBookshelf.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 12.dp, bottom = 6.dp),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.bookshelf_create_title),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    mainMenuExpanded = false
                                    uiState.onCreate()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.bookshelf_settings),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    mainMenuExpanded = false
                                    uiState.onEdit(uiState.selectedBookshelfId)
                                }
                            )
                            DropdownMenuItem(
                                enabled = !sortLocked,
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(R.string.bookshelf_adjust_order),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        if (sortLocked) {
                                            Text(
                                                text = stringResource(R.string.bookshelf_adjust_order_locked),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    mainMenuExpanded = false
                                    uiState.enableReorderMode()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.share_bookshelf),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = onShareBookshelf
                            )
                            Text(
                                text = stringResource(R.string.options),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 12.dp, bottom = 6.dp)
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.bookshelf_adjust_bookshelf_order),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    mainMenuExpanded = false
                                    uiState.enableBookshelfReorderMode()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.import_and_export),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_right_24px),
                                        contentDescription = null
                                    )
                                },
                                onClick = { exportImportMenuExpanded = true }
                            )
                        }
                        DropdownMenu(
                            modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                                with(localDensity) {
                                    exportImportMenuWidth = layoutCoordinates.size.width.toDp()
                                }
                            },
                            offset = DpOffset(
                                x = -exportImportMenuWidth,
                                y = mainMenuItemHeight.times(6.5f)
                            ),
                            expanded = exportImportMenuExpanded,
                            onDismissRequest = { exportImportMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.export_to_lnr_file),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    onSaveThisBookshelf()
                                    exportImportMenuExpanded = false
                                    mainMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.export_all_to_lnr_file),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    onSaveAllBookshelf()
                                    exportImportMenuExpanded = false
                                    mainMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.import_from_file),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    onImportBookshelf()
                                    exportImportMenuExpanded = false
                                    mainMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                else -> {
                    IconButton(onClick = uiState.onSelectAll) {
                        Icon(
                            painter = painterResource(R.drawable.select_all_24px),
                            contentDescription = "select all"
                        )
                    }
                    IconButton(onClick = uiState.onPin) {
                        Icon(
                            painter = painterResource(R.drawable.keep_24px),
                            contentDescription = "pin"
                        )
                    }
                    IconButton(onClick = uiState.onRemove) {
                        Icon(
                            painter = painterResource(R.drawable.bookmark_remove_24px),
                            contentDescription = "remove"
                        )
                    }
                    IconButton(onClick = uiState.onMarkSelectedBooks) {
                        Icon(
                            painter = painterResource(R.drawable.outline_bookmark_24px),
                            contentDescription = "bookmark"
                        )
                    }
                }
            }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            scrolledContainerColor = backgroundColor
        )
    )
}
