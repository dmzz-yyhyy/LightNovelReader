package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import io.nightfish.lightnovelreader.api.error.WebRequestError

@Composable
fun BookInformationErrorCover(
    width: Dp,
    height: Dp,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    rounded: Dp = 8.dp,
    onClick: (() -> Unit)? = null
) {
    val snackbarHostState = LocalSnackbarHost.current
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(rounded))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable {
                if (onClick != null) {
                    onClick()
                } else {
                    showSnackbar(
                        coroutineScope = coroutineScope,
                        hostState = snackbarHostState,
                        message = errorMessage
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(R.drawable.help_center_24px),
            contentDescription = "error",
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookInformationUnavailableCard(
    modifier: Modifier = Modifier,
    webRequestError: WebRequestError,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    titleHeight: Dp?
) {
    val interactionModifier = if (onClick != null) {
        Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongPress ?: {}
        )
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(146.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(interactionModifier)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookInformationErrorCover(
            width = 94.dp,
            height = 144.dp,
            errorMessage = webRequestError.message.ifEmpty { stringResource(R.string.error_data_source_generic) },
            onClick = onClick
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = if (titleHeight != null) Modifier
                    .height(titleHeight)
                    .wrapContentHeight(Alignment.CenterVertically)
                else Modifier,
                text = stringResource(R.string.error_book_title),
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.error_book_info),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = webRequestError.title.ifEmpty { stringResource(R.string.error_book_title) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
