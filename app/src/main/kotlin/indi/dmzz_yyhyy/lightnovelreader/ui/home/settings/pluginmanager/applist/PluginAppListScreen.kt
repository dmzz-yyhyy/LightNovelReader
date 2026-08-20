package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.applist

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginAppInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginAppListScreen(
    appPluginList: List<PluginAppInfo>,
    onRefresh: suspend () -> Unit,
    onClickBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    val triggerRefresh: () -> Unit = refresh@{
        if (isRefreshing) return@refresh
        isRefreshing = true
        coroutineScope.launch {
            onRefresh()
            isRefreshing = false
            pullToRefreshState.animateToHidden()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                onClickBack = onClickBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = triggerRefresh,
            state = pullToRefreshState
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (appPluginList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyPage(
                            modifier = Modifier.navigationBarsPadding(),
                            icon = painterResource(R.drawable.extension_24px),
                            title = stringResource(R.string.plugin_app_empty_title),
                            description = stringResource(R.string.plugin_app_empty_desc)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(appPluginList) { plugin ->
                            AppCard(plugin)
                        }
                        item {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCard(
    plugin: PluginAppInfo
) {
    val context = LocalContext.current
    val intent = remember(plugin.packageName) {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", plugin.packageName, null)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                context.startActivity(intent)
            }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(packageName = plugin.packageName)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plugin.name,
                    style = typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${plugin.packageName} · ${plugin.versionName}",
                    style = typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.plugin_app_loadable_tag),
                    style = typography.labelMedium,
                    color = colorScheme.surfaceTint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(24.dp),
                painter = painterResource(R.drawable.open_in_new_24px),
                tint = colorScheme.onSurfaceVariant,
                contentDescription = "detail",
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.plugin_app_list),
                style = typography.displayLarge,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun AppIcon(packageName: String) {
    val context = LocalContext.current
    val iconState = remember(packageName) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(packageName) {
        val bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val drawable = context.packageManager.getApplicationIcon(packageName)
                drawable.toBitmap().asImageBitmap()
            }.getOrNull()
        }
        iconState.value = bitmap
    }

    val iconBitmap = iconState.value
    if (iconBitmap == null) {
        Icon(
            painter = painterResource(R.drawable.extension_24px),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
    } else {
        Image(
            bitmap = iconBitmap,
            modifier = Modifier.size(40.dp),
            contentDescription = null
        )
    }
}
