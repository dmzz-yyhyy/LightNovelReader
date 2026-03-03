package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.repository

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginMetadata
import indi.dmzz_yyhyy.lightnovelreader.ui.components.LnrSnackbar
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.RemotePluginMetadata
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginRepositoryScreen(
    uiState: PluginRepositoryUiState,
    installedPluginList: List<PluginMetadata>,
    onRefresh: () -> Unit,
    onClickBack: () -> Unit,
    onClickSetRepoUrl: () -> Unit,
    onClickCancel: (String) -> Unit,
    onClickInstallFromRepo: (RemotePluginMetadata) -> Unit,
    loadPluginMetadata: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val enterAlwaysScrollBehavior = enterAlwaysScrollBehavior()
    val pullToRefreshState = rememberPullToRefreshState()
    val snackbarHostState = LocalSnackbarHost.current
    var isRefreshing by remember { mutableStateOf(false) }
    isRefreshing = uiState.isLoading

    Scaffold(
        topBar = {
            TopBar(
                showLoadingIndicator = uiState.isLoading,
                onClickBack = onClickBack,
                onClickSetRepoUrl = onClickSetRepoUrl,
                scrollBehavior = enterAlwaysScrollBehavior
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                LnrSnackbar(it, modifier = Modifier.padding(bottom = 56.dp))
            }
        }
    ) { paddingValues ->
        val pluginMetadataList = uiState.remotePluginMetadataList

        val pluginListToShow = remember(uiState.indexList, uiState.remotePluginMetadataList) {
            uiState.indexList.ifEmpty {
                uiState.remotePluginMetadataList.map {
                    RepoIndexEntry(id = it.id, name = it.name)
                }
            }
        }

        PullToRefreshBox(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                onRefresh()
                coroutineScope.launch {
                    pullToRefreshState.animateToHidden()
                }
            },
            state = pullToRefreshState
        ) {
            if (pluginMetadataList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!uiState.isLoading) {
                        Text(stringResource(R.string.plugin_repo_load_failed))
                        uiState.errorMessage?.let {
                            showSnackbar(
                                coroutineScope = coroutineScope,
                                message = it,
                                hostState = snackbarHostState,
                                duration = SnackbarDuration.Long,
                                actionLabel = stringResource(R.string.confirm)
                            ) { }
                        }
                    }
                }
                return@PullToRefreshBox
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(pluginListToShow, key = { it.id }) { indexEntry ->
                    val metadata = uiState.remotePluginMetadataList.firstOrNull { it.id == indexEntry.id }
                    val isLoadingMetadata = uiState.metadataLoadingStates[indexEntry.id] == true

                    PluginCard(
                        installed = installedPluginList.firstOrNull { it.packageName == indexEntry.id } != null,
                        inQueue = uiState.queue.firstOrNull{ it == indexEntry.id} != null,
                        metadata = metadata,
                        indexEntry = indexEntry,
                        isLoadingMetadata = isLoadingMetadata,
                        onClickDownload = {
                            if (metadata == null) {
                                loadPluginMetadata(indexEntry.id)
                            } else {
                                onClickInstallFromRepo(metadata)
                            }
                        },
                        onClickCancel = { onClickCancel(indexEntry.id) },
                        progress = uiState.progressMap[indexEntry.id],
                        loadPluginMetadata = loadPluginMetadata
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PluginCard(
    installed: Boolean,
    inQueue: Boolean,
    metadata: RemotePluginMetadata?,
    indexEntry: RepoIndexEntry,
    isLoadingMetadata: Boolean,
    onClickDownload: () -> Unit,
    onClickCancel: () -> Unit,
    progress: Float?,
    loadPluginMetadata: (String) -> Unit
) {
    LaunchedEffect(indexEntry.id) {
        if (metadata == null && !isLoadingMetadata) {
            loadPluginMetadata(indexEntry.id)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = indexEntry.name, style = typography.bodyLarge)
            Spacer(Modifier.height(4.dp))

            if (isLoadingMetadata || metadata == null) {
                SkeletonMetadataDisplay()
            } else {
                LoadedMetadataDisplay(metadata)
            }
        }

        Spacer(Modifier.width(12.dp))

        Box {
            when {
                isLoadingMetadata -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
                metadata == null -> {
                    Icon(
                        painter = painterResource(id = R.drawable.error_24px),
                        contentDescription = "加载失败",
                        tint = Color.Red
                    )
                }
                else -> {
                    DownloadActionButton(
                        installed = installed,
                        progress = progress,
                        inQueue = inQueue,
                        onClickDownload = onClickDownload,
                        onClickCancel = onClickCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonMetadataDisplay() {
    Column {
        SkeletonText(width = 80.dp, height = 16.dp)
        Spacer(Modifier.height(4.dp))
        SkeletonText(width = 60.dp, height = 14.dp)
        Spacer(Modifier.height(6.dp))
        SkeletonText(width = 200.dp, height = 16.dp)
        SkeletonText(width = 180.dp, height = 16.dp)
    }
}

@Composable
private fun LoadedMetadataDisplay(metadata: RemotePluginMetadata) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(
                R.string.plugin_repo_item_version,
                metadata.versionName,
                metadata.version
            ),
            style = typography.bodySmall
        )
        Spacer(Modifier.width(8.dp))
        Text(text = metadata.author, style = typography.labelMedium)
    }
    Spacer(Modifier.height(6.dp))
    Text(text = metadata.description, style = typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun SkeletonText(width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(
                color = Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
    )
}

enum class Status { IdleDownload, IdleReinstall, Indeterminate, Determinate, Processing }

@Composable
fun DownloadActionButton(
    installed: Boolean,
    progress: Float?,
    inQueue: Boolean,
    onClickDownload: () -> Unit,
    onClickCancel: () -> Unit,
) {
    val status by remember(installed, inQueue, progress) {
        derivedStateOf {
            when {
                inQueue -> Status.Indeterminate
                progress == null && !installed -> Status.IdleDownload
                progress == null && installed -> Status.IdleReinstall
                progress != null && progress >= 1f -> Status.Processing
                progress == 0f -> Status.Indeterminate
                progress != null -> Status.Determinate
                else -> Status.IdleDownload
            }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = status,
            transitionSpec = {
                val enter = fadeIn(animationSpec = tween(220)) + slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth / 2 },
                    animationSpec = tween(220)
                )
                val exit = fadeOut(animationSpec = tween(180)) + slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 2 },
                    animationSpec = tween(180)
                )
                enter.togetherWith(exit)
            },
            contentAlignment = Alignment.Center,
            label = "download-action"
        ) { status ->
            when (status) {
                Status.IdleDownload -> {
                    IconButton(onClick = onClickDownload) {
                        Icon(
                            painter = painterResource(id = R.drawable.download_24px),
                            contentDescription = "download"
                        )
                    }
                }

                Status.IdleReinstall -> {
                    TextButton(onClick = onClickDownload) {
                        Icon(
                            painter = painterResource(id = R.drawable.handyman_24px),
                            contentDescription = "reinstall",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.plugin_repo_reinstall))
                    }
                }

                Status.Indeterminate -> {
                    TextButton(onClick = onClickCancel) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.cancel))
                    }
                }

                Status.Processing -> {
                    TextButton(onClick = onClickCancel) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.plugin_repo_installing))
                    }
                }

                Status.Determinate -> {
                    val target = progress?.coerceIn(0f, 1f) ?: 0f
                    val animProgress by animateFloatAsState(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 300)
                    )

                    TextButton(onClick = onClickCancel) {
                        CircularProgressIndicator(progress = { animProgress }, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    showLoadingIndicator: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit,
    onClickSetRepoUrl: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.plugin_repo_title),
                    style = typography.displayLarge,
                    color = colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onClickBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back_24px),
                        contentDescription = "back"
                    )
                }
            },
            actions = {
                IconButton(onClick = onClickSetRepoUrl) {
                    Icon(
                        painter = painterResource(id = R.drawable.deployed_code_update_24px),
                        contentDescription = "set repository url"
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        if (showLoadingIndicator)
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
    }
}