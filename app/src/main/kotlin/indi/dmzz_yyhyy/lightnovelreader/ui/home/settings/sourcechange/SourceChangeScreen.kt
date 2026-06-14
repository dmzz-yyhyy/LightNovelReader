package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import io.nightfish.lightnovelreader.api.web.WebDataSourceItem

@Composable
fun SourceChangeScreen(
    uiState: SourceChangeUiState,
    onClickBack: () -> Unit,
    onApplyClick: (Int) -> Unit
) {
    var selectedSourceId by rememberSaveable(uiState.currentSourceId) {
        mutableIntStateOf(uiState.currentSourceId)
    }

    val hasPendingChange = selectedSourceId != uiState.currentSourceId

    Scaffold(
        topBar = { TopBar(onClickBack) },
        bottomBar = {
            SourceChangeBottomBar(
                visible = hasPendingChange,
                isProcessing = uiState.isProcessing,
                currentSourceName = uiState.webDataSourceItems
                    .firstOrNull { it.id == uiState.currentSourceId }?.name ?: "",
                targetSourceName = uiState.webDataSourceItems
                    .firstOrNull { it.id == selectedSourceId }?.name ?: "",
                onApplyClick = { onApplyClick(selectedSourceId) }
            )

        }
    ) { innerPadding ->
        SourceChangeContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            selectedSourceId = selectedSourceId,
            onSelectedChange = { selectedSourceId = it }
        )
    }
}

@Composable
private fun SourceChangeBottomBar(
    visible: Boolean,
    isProcessing: Boolean,
    currentSourceName: String,
    targetSourceName: String,
    onApplyClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = 350,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        Surface(
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (visible) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.settings_data_source_pending_title,
                                targetSourceName
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(
                                R.string.settings_data_source_pending_desc,
                                currentSourceName
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = onApplyClick,
                    enabled = !isProcessing && visible
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(R.string.processing))
                    } else {
                        Text(text = stringResource(
                            R.string.apply
                        ))
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceChangeContent(
    modifier: Modifier = Modifier,
    uiState: SourceChangeUiState,
    selectedSourceId: Int,
    onSelectedChange: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
    ) {
        item("source_title") {
            SectionHeader(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(vertical = 10.dp),
                text = stringResource(R.string.source_available_title)
            )
        }

        itemsIndexed(
            uiState.webDataSourceItems,
            key = { _, item -> item.id }
        ) { index, item ->
            SourceRadioItem(
                item = item,
                selected = item.id == selectedSourceId,
                onClick = { onSelectedChange(item.id) },
                showDivider = index != uiState.webDataSourceItems.lastIndex,
            )
        }
        navigationBarSpacer()
    }

}

@Composable
private fun SourceRadioItem(
    item: WebDataSourceItem,
    selected: Boolean,
    onClick: () -> Unit,
    settingsEnabled: Boolean = false,
    onSettingsClick: () -> Unit = {},
    showDivider: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(
                        R.string.data_source_provider,
                        item.provider
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (settingsEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { onSettingsClick() }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_settings_24px),
                        contentDescription = null
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(24.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            RadioButton(
                modifier = Modifier.padding(horizontal = 10.dp),
                selected = selected,
                onClick = onClick
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
            )
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopBar(
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings_select_data_source),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClickBack) {
                Icon(
                    painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        }
    )
}