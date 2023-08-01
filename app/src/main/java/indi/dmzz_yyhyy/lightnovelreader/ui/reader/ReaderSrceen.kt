package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(navController: NavController, readerViewModel: ReaderViewModel) {
    val readerUiState by readerViewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(readerUiState.title) }) },
        bottomBar = {}
    ) {
        LazyColumn {
            item { Box(Modifier.padding(it)) }
            item {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = readerUiState.content,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
