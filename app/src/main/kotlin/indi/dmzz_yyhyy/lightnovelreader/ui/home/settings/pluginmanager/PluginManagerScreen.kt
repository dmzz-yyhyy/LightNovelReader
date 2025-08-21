package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography

data class Plugin(
    val isEnabled: Boolean,
    val isUpdatable: Boolean,
    val id: String,
    val name: String,
    val version: Int,
    val versionName: String,
    val author: String,
    val description: String,
    val url: String?,
    val updateUrl: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginManagerScreen(
    onClickBack: () -> Unit,
    onClickDetail: (String) -> Unit,
    pluginList: List<Plugin>
) {
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                onClickBack = onClickBack,
                scrollBehavior = enterAlwaysScrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(end = 12.dp, bottom = 24.dp),
                onClick = { },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.archive_24px),
                        contentDescription = "install"
                    )
                },
                text = {
                    Text("Install plugin")
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            item {
                ThirdPartyPluginTips()
            }
            items(pluginList) { plugin ->
                PluginCard(
                    plugin = plugin,
                    onClickDetail = onClickDetail
                )
            }
            item {
                Spacer(Modifier.height(98.dp))
            }
        }
    }
}

@Composable
private fun ThirdPartyPluginTips() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
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
                Text("来自第三方的插件可以提供额外的数据源或功能。安装插件前，请确保其来源可信。\nLightNovelReader 不对插件可用性负责。", style = AppTypography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PluginCard(
    plugin: Plugin,
    onClickDetail: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(plugin.name, style = AppTypography.titleMedium)
                    Text(plugin.versionName, style = AppTypography.labelMedium, color = colorScheme.secondary)
                    Text("by ${plugin.author}", style = AppTypography.labelMedium, color = colorScheme.secondary)
                }
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = plugin.isEnabled,
                    onCheckedChange = null
                )
            }
            /*HorizontalDivider(Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth())
            Text(plugin.description, style = AppTypography.bodyMedium)*/
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (plugin.isUpdatable) {
                    Button(
                        onClick = {},
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(painterResource(R.drawable.deployed_code_update_24px), null)
                            Spacer(Modifier.width(12.dp))
                            Text("更新")
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                FilledTonalIconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete_forever_24px),
                        contentDescription = "remove"
                    )
                }
                FilledTonalButton(
                    onClick = {
                        onClickDetail(plugin.id)
                    }
                ) {
                    Text("详情")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(
                text = "扩展插件",
                style = AppTypography.titleTopBar,
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