package indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.utils.formTime
import io.nightfish.lightnovelreader.api.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerScreen(
    onClickBack: () -> Unit,
    downloadItemIdList: List<DownloadItem>,
    bookInformationMap: Map<String, BookInformation>,
    loadBookInfo: (String) -> Unit,
    onClickCancel: (DownloadItem) -> Unit,
    onClickClearCompleted: () -> Unit
) {
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.book_management_title),
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
                }
            )
        }
    ) {
        Box(Modifier.padding(it)) {
            Content(
                downloadItemIdList = downloadItemIdList,
                bookInformationMap = bookInformationMap,
                loadBookInfo = loadBookInfo,
                onClickCancel = onClickCancel,
                onClickClearCompleted = onClickClearCompleted
            )
        }
    }
}

@Composable
private fun Content(
    downloadItemIdList: List<DownloadItem>,
    bookInformationMap: Map<String, BookInformation>,
    loadBookInfo: (String) -> Unit,
    onClickCancel: (DownloadItem) -> Unit,
    onClickClearCompleted: () -> Unit
) {
    if (downloadItemIdList.isEmpty())
        EmptyPage(
            modifier = Modifier.navigationBarsPadding(),
            icon = painterResource(id = R.drawable.download_24px),
            title = stringResource(R.string.nothing_here),
            description = stringResource(R.string.nothing_here_desc_book_manager),
        )
    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (downloadItemIdList.any { it.progress < 1f })
            item {
                Text(
                    modifier = Modifier.height(34.dp).animateItem(),
                    text = stringResource(R.string.download_in_progress),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.W600
                )
            }
        items(
            items = downloadItemIdList.distinct().filter { it.progress < 1f }.reversed(),
            key = { "dl_${it.type.name}_${it.bookId}_${it.startTime}" }
        ) { downloadItem ->
            val bookInformation = bookInformationMap[downloadItem.bookId] ?: BookInformation.empty(downloadItem.bookId)
            LaunchedEffect(downloadItem.bookId) {
                loadBookInfo(downloadItem.bookId)
            }
            Card(
                modifier = Modifier.animateItem(),
                bookInformation = bookInformation,
                downloadItem = downloadItem,
                onClickCancel = { onClickCancel(downloadItem) }
            )
        }
        if (downloadItemIdList.any { it.progress >= 1f })
            item {
                Row(
                    modifier = Modifier.animateItem(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.completed),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.W600
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClickClearCompleted) {
                        Text(
                            text = stringResource(R.string.clear_all),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.W600,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        items(
            items =  downloadItemIdList.distinct().filter { it.progress >= 1f }.reversed(),
            key = { "finished_${it.type.name}_${it.bookId}_${it.startTime}" }
        ) { downloadItem ->
            val bookInformation = bookInformationMap[downloadItem.bookId] ?: BookInformation.empty(downloadItem.bookId)
            LaunchedEffect(downloadItem.bookId) {
                loadBookInfo(downloadItem.bookId)
            }
            Card(
                modifier = Modifier.animateItem(),
                bookInformation = bookInformation,
                downloadItem = downloadItem,
                onClickCancel = { onClickCancel(downloadItem) }
            )
        }
        navigationBarSpacer()
    }
}

@Composable
private fun Card(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    downloadItem: DownloadItem,
    onClickCancel: () -> Unit
) {
    val progressAnim by animateFloatAsState(
        targetValue = downloadItem.progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "",
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(
            width = 64.dp,
            height = 93.dp,
            uri = bookInformation.coverUri
        )
        Box(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = bookInformation.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.W600
            )
            Text(
                text = bookInformation.author,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W500,
                letterSpacing = 0.15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp),
                    painter =
                        if (downloadItem.progress >= 1) painterResource(R.drawable.done_outline_24px)
                        else if (downloadItem.progress >= 0) painterResource(downloadItem.type.icon)
                        else painterResource(R.drawable.error_24px),
                    contentDescription = null
                )
                Box(Modifier.width(10.dp))
                Text(
                    text =
                        if (downloadItem.progress < 1)
                            stringResource(R.string.download_item_progress,
                                formTime(downloadItem.startTime),
                                (downloadItem.progress*100).toInt()
                            )
                        else if (downloadItem.progress > 0)
                            stringResource(R.string.download_item_finished, downloadItem.type.typeName)
                        else stringResource(R.string.download_item_failed, downloadItem.type.typeName),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.W500,
                    letterSpacing = 0.15.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (downloadItem.progress < 1)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { progressAnim },
                )
        }
        if (downloadItem.progress < 1)
            IconButton(onClickCancel) {
                Icon(
                    painter = painterResource(R.drawable.cancel_24px),
                    contentDescription = "cancel"
                )
            }
        Box(Modifier.width(7.dp))
    }
}