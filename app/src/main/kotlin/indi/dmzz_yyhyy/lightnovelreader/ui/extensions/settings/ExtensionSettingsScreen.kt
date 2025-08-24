package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionSettingEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.SettingType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionSettingsScreen(
    extensionId: Int,
    extensionName: String,
    onBack: () -> Unit,
    viewModel: ExtensionSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(extensionId) {
        viewModel.loadSettings(extensionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$extensionName Settings") },
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.settings) { setting ->
                    ExtensionSettingItem(
                        setting = setting,
                        onValueChange = { newValue ->
                            viewModel.updateSetting(setting.key, newValue)
                        }
                    )
                }
                
                if (uiState.settings.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No settings available for this extension",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error snackbar or dialog
            }
        }
    }
}

@Composable
fun ExtensionSettingItem(
    setting: ExtensionSettingEntity,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = setting.title.takeIf { it.isNotBlank() } ?: setting.key,
                style = MaterialTheme.typography.titleMedium
            )
            
            if (setting.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = setting.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (setting.type) {
                SettingType.STRING -> {
                    var textValue by remember(setting.value) { mutableStateOf(setting.value) }
                    
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { 
                            textValue = it
                            onValueChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                SettingType.BOOLEAN -> {
                    var checked by remember(setting.value) { 
                        mutableStateOf(setting.value.toBoolean()) 
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable")
                        Switch(
                            checked = checked,
                            onCheckedChange = { 
                                checked = it
                                onValueChange(it.toString())
                            }
                        )
                    }
                }
                
                SettingType.INT -> {
                    var textValue by remember(setting.value) { mutableStateOf(setting.value) }
                    
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '-' }) {
                                textValue = newValue
                                onValueChange(newValue)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                SettingType.FLOAT -> {
                    var textValue by remember(setting.value) { mutableStateOf(setting.value) }
                    
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '.' || it == '-' }) {
                                textValue = newValue
                                onValueChange(newValue)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                SettingType.LIST -> {
                    // For now, just show as text. In the future, implement dropdown
                    var textValue by remember(setting.value) { mutableStateOf(setting.value) }
                    
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { 
                            textValue = it
                            onValueChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}
