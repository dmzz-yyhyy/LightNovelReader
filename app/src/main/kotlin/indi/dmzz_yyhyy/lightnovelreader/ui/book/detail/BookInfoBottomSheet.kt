package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.get
import indi.dmzz_yyhyy.lightnovelreader.utils.dateFormatter
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookInfoBottomSheet(
    bookInformation: BookInformation,
    bookVolumes: BookVolumes,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    @Composable
    fun InfoItem(
        title: String? = "",
        content: String,
        titleStyle: TextStyle,
        contentStyle: TextStyle,
        icon: Painter? = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.weight(3f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                icon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title!!,
                    style = titleStyle
                )
            }

            Row(
                modifier = Modifier.weight(7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = content,
                    style = contentStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                coroutineScope.launch {
                                    val clipData = ClipData.newPlainText("content", content)
                                    val clipEntry = ClipEntry(clipData = clipData)
                                    clipboard.setClipEntry(clipEntry = clipEntry)
                                }
                            },
                        )
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        val titleStyle = typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W600
        )
        val contentStyle = typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            InfoItem(
                title = stringResource(R.string.detail_info_title),
                content = bookInformation.title,
                titleStyle = titleStyle,
                contentStyle = contentStyle,
                icon = painterResource(R.drawable.title_24px)
            )

            if (bookInformation.subtitle.isNotEmpty()) {
                InfoItem(
                    content = bookInformation.subtitle,
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                )
            }

            InfoItem(
                title = stringResource(R.string.detail_info_id),
                content = bookInformation.id,
                titleStyle = titleStyle,
                contentStyle = contentStyle,
                icon = painterResource(R.drawable.info_24px)
            )

            InfoItem(
                title = stringResource(R.string.detail_info_author),
                content = bookInformation.author,
                titleStyle = titleStyle,
                contentStyle = contentStyle,
                icon = painterResource(R.drawable.person_edit_24px)
            )

            InfoItem(
                title = stringResource(R.string.detail_info_publishing_house),
                content = bookInformation.publishingHouse,
                titleStyle = titleStyle,
                contentStyle = contentStyle,
                icon = painterResource(R.drawable.text_snippet_24px)
            )

            InfoItem(
                title = stringResource(R.string.detail_info_updated_on),
                content = bookInformation.lastUpdated.format(dateFormatter()) + "\n" +
                        if (bookInformation.isComplete) stringResource(R.string.book_completed)
                        else stringResource(R.string.book_ongoing),
                titleStyle = titleStyle,
                contentStyle = contentStyle,
                icon = painterResource(R.drawable.autorenew_24px)
            )

            InfoItem(
                title = stringResource(R.string.detail_info_tags),
                content = bookInformation.tags.joinToString(separator = "，"),
                titleStyle = titleStyle,
                contentStyle = contentStyle,
                icon = painterResource(R.drawable.tag_24px)
            )

            InfoItem(
                title = stringResource(R.string.detail_info_stats),
                content = stringResource(
                    R.string.detail_info_word_count_content,
                    bookInformation.wordCount.get()
                ) + "\n" + stringResource(
                    R.string.detail_info_stats_count_content,
                    bookVolumes.volumes.count(),
                    bookVolumes.volumes.sumOf { it.chapters.size }
                ),
                titleStyle = titleStyle,
                contentStyle = contentStyle,
                icon = painterResource(R.drawable.text_fields_24px)
            )
        }
    }
}
