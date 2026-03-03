package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginMetadata
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.PluginCard
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalClaimSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginManagerScreen(
    enabledPluginList: List<String>,
    errorMessageMap: Map<String, String>,
    onClickInstall: () -> Unit,
    onClickBack: () -> Unit,
    onClickPluginApps: () -> Unit,
    onClickDetail: (String) -> Unit,
    onClickDelete: (String) -> Unit,
    onClickSwitch: (PluginMetadata) -> Unit,
    onClickKeyAlert: () -> Unit,
    onClickErrorAlert: () -> Unit,
    onClickIncompatibleAlert: () -> Unit,
    onClickPluginRepo: () -> Unit,
    onClickCheckUpdate: (String) -> Unit,
    pluginInfoList: List<PluginMetadata>,
    onClickShowSignatures: (String) -> Unit
) {
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val claim = LocalClaimSnackbarHost.current

    DisposableEffect(Unit) {
        claim(true)
        onDispose { claim(false) }
    }

    Scaffold(
        topBar = {
            TopBar(
                onClickBack = onClickBack,
                scrollBehavior = enterAlwaysScrollBehavior,
                onClickPluginApps = onClickPluginApps,
                onClickPluginRepo = onClickPluginRepo
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(end = 12.dp, bottom = 24.dp),
                onClick = onClickInstall,
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.archive_24px),
                        contentDescription = "install"
                    )
                },
                text = {
                    Text(text = stringResource(R.string.plugin_install_plugin))
                }
            )
        },
        snackbarHost = {
            SnackbarHost(LocalSnackbarHost.current)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ThirdPartyPluginTips()

            Spacer(Modifier.height(8.dp))

            if (pluginInfoList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyPage(
                        modifier = Modifier.navigationBarsPadding(),
                        icon = painterResource(R.drawable.deployed_code_update_24px),
                        title = stringResource(R.string.plugin_empty_title),
                        description = stringResource(R.string.plugin_empty_desc),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(pluginInfoList) { plugin ->
                        PluginCard(
                            modifier = Modifier.animateItem(),
                            pluginInfo = plugin,
                            onClickDetail = onClickDetail,
                            enabledPluginList = enabledPluginList,
                            isErrorDisabled = errorMessageMap.containsKey(plugin.packageName),
                            onClickSwitch = onClickSwitch,
                            onClickDelete = onClickDelete,
                            onClickCheckUpdate = onClickCheckUpdate,
                            onClickKeyAlert = onClickKeyAlert,
                            onClickErrorAlert = onClickErrorAlert,
                            onClickIncompatibleAlert = onClickIncompatibleAlert,
                            onClickShowSignatures = onClickShowSignatures
                        )
                    }
                    item {
                        Spacer(Modifier.height(98.dp))
                    }
                }
            }
        }
    }
}

val horizontalPadding = 16.dp

@Composable
private fun ThirdPartyPluginTips() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = horizontalPadding),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.info_24px),
                contentDescription = "warning"
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.plugin_third_party_tips),
                    style = typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit,
    onClickPluginApps: () -> Unit,
    onClickPluginRepo: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.settings_plugins),
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
        actions = {
            IconButton(
                onClick = onClickPluginApps
            ) {
                Icon(
                    painter = painterResource(R.drawable.deployed_code_24px), null
                )
            }
            if (BuildConfig.DEBUG)
                TextButton(
                    onClick = onClickPluginRepo
                ) {
                    Text(text = stringResource(R.string.plugin_repo))
                }
        },
        scrollBehavior = scrollBehavior
    )
}
