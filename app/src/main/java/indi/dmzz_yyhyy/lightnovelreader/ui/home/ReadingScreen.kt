package indi.dmzz_yyhyy.lightnovelreader.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.ui.home.iteam.ReadingBookCard

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun ReadingScreen(
    navController: NavController,
    readingViewModel: ReadingViewModel
) {
    val readingUiState by readingViewModel.uiState.collectAsState()

        val padding = 16.dp
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            for (book in readingUiState.readingBookDataList) {
                ReadingBookCard(readingViewModel, book)
            }
        }

}


