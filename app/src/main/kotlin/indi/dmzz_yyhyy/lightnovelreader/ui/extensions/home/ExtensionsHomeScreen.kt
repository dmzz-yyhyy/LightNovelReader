package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.home.model.ExtensionsHomeUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionsHomeScreen(
    onNavigateToRepositories: () -> Unit,
    onNavigateToBrowse: () -> Unit,
    onNavigateToInstalled: () -> Unit,
    onNavigateToExploration: () -> Unit,
    viewModel: ExtensionsHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.extensions)) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ExtensionsHomeContent(
                    uiState = uiState,
                    onNavigateToRepositories = onNavigateToRepositories,
                    onNavigateToBrowse = onNavigateToBrowse,
                    onNavigateToInstalled = onNavigateToInstalled,
                    onNavigateToExploration = onNavigateToExploration
                )
            }
        }
    }
}

@Composable
fun ExtensionsHomeContent(
    uiState: ExtensionsHomeUI,
    onNavigateToRepositories: () -> Unit,
    onNavigateToBrowse: () -> Unit,
    onNavigateToInstalled: () -> Unit,
    onNavigateToExploration: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Repositories Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToRepositories
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.repositories),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.repositories_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.repository_count, uiState.repositoryCount),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Browse Extensions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToBrowse
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.browse_extensions),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.browse_extensions_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.available_extensions_count, uiState.availableExtensionsCount),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Installed Extensions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToInstalled
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.installed_extensions),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.installed_extensions_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.installed_extensions_count, uiState.installedExtensionsCount),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Extension Exploration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToExploration
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.extension_exploration),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.extension_exploration_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.explore_novels_from_extensions),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
