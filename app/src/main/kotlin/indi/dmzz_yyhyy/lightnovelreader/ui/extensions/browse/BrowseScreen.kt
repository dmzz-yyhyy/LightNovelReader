package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.browse.model.BrowseExtensionItem
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.components.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    onBack: () -> Unit,
    viewModel: BrowseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.browse_extensions)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        // Toggle search functionality
                        viewModel.toggleSearch()
                    }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
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
                    text = stringResource(R.string.no_extensions_available),
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
                    ExtensionCard(
                        item = item,
                        onInstall = { viewModel.installExtension(item.extension) },
                        onUpdate = { viewModel.updateExtension(item.extension) }
                    )
                }
            }
        }

        // Show error dialog if there's an error
        uiState.error?.let { error ->
            ErrorDialog(
                error = error,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionCard(
    item: BrowseExtensionItem,
    onInstall: () -> Unit,
    onUpdate: () -> Unit
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
                    if (item.isInstalling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (item.isInstalled) {
                        if (item.isUpdateAvailable) {
                            Button(
                                onClick = onUpdate,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text(stringResource(R.string.update))
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.installed),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Button(onClick = onInstall) {
                            Text(stringResource(R.string.install))
                        }
                    }
                }
            }
        }
    }
}
