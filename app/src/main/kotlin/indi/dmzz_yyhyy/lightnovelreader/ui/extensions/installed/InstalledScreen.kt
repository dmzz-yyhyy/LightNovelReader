package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.installed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.installed.model.InstalledExtensionItem
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.components.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledScreen(
    onBack: () -> Unit,
    onNavigateToSettings: (Int, String) -> Unit, // extensionId, extensionName
    viewModel: InstalledViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.installed_extensions)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.extensions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_extensions_installed),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.extensions) { item ->
                    InstalledExtensionCard(
                        item = item,
                        onUninstall = { viewModel.uninstallExtension(it) },
                        onToggleEnabled = { extension, enabled -> 
                            viewModel.toggleExtension(extension, enabled)
                        },
                        onSettings = { extension ->
                            onNavigateToSettings(extension.id, extension.name)
                        }
                    )
                }
            }
        }

        // Show error dialog if there's an error
        uiState.error?.let { error ->
            ErrorDialog(
                error = error,
                onDismiss = { /* TODO: Clear error in ViewModel */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledExtensionCard(
    item: InstalledExtensionItem,
    onUninstall: () -> Unit,
    onToggleEnabled: () -> Unit,
    onSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.extension.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = item.extension.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.extension_version, item.extension.version),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(R.string.extension_language, item.extension.lang),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (item.isUninstalling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row {
                            Switch(
                                checked = item.extension.isEnabled,
                                onCheckedChange = { onToggleEnabled() }
                            )
                            IconButton(onClick = onSettings) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.settings)
                                )
                            }
                            IconButton(onClick = onUninstall) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.uninstall)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
