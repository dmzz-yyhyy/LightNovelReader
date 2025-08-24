package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.exploration

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
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionSearchScreen(
    onBack: () -> Unit,
    onBookClick: (BookInformationEntity) -> Unit,
    viewModel: ExtensionExplorationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_novels)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    viewModel.setSearchQuery(it)
                },
                onSearch = { viewModel.setSearchQuery(it) },
                active = false,
                onActiveChange = {},
                placeholder = { Text(stringResource(R.string.search_novels_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            ) {}

            // Results
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (searchQuery.isBlank()) {
                // Show browsing UI when no search query
                BrowseExtensionsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    onBrowseExtension = { extensionId ->
                        viewModel.browseExtension(extensionId)
                    }
                )
            } else if (uiState.books.isEmpty() && searchQuery.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_books_found),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else if (uiState.books.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.books) { book ->
                        BookCard(
                            book = book,
                            onClick = { onBookClick(book) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(
    book: BookInformationEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium
            )
            if (book.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.subtitle,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (book.author.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.author, book.author),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (book.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun BrowseExtensionsContent(
    modifier: Modifier = Modifier,
    onBrowseExtension: (String) -> Unit,
    viewModel: ExtensionExplorationViewModel = hiltViewModel()
) {
    val homeUIState by viewModel.homeUIState.collectAsState()
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Browse Extensions",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        
        item {
            Text(
                text = "Select an extension to browse its latest novels:",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        items(homeUIState.availableExtensions) { extension ->
            ExtensionBrowseCard(
                extension = extension,
                onBrowse = { onBrowseExtension(extension.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionBrowseCard(
    extension: indi.dmzz_yyhyy.lightnovelreader.ui.extensions.exploration.model.ExtensionInfo,
    onBrowse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = extension.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (extension.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = extension.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
            }
            
            Button(
                onClick = onBrowse,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text("Browse")
            }
        }
    }
}
