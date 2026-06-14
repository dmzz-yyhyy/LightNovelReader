package indi.dmzz_yyhyy.lightnovelreader.ui.storagemanager

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import indi.dmzz_yyhyy.lightnovelreader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagerScreen(
    onClickBack: () -> Unit,
    uiState: StorageManagerUiState,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.storage_manager_title),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.W600
                    )
                },
                navigationIcon = {
                    IconButton(onClickBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_24px),
                            contentDescription = "back"
                        )
                    }
                },
                actions = {
                    IconButton(uiState.load) {
                        Icon(
                            painter = painterResource(id = R.drawable.refresh_24px),
                            contentDescription = "refresh"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        StorageOverviewContent(
            modifier = Modifier.padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            uiState = uiState,
        )
    }
}
