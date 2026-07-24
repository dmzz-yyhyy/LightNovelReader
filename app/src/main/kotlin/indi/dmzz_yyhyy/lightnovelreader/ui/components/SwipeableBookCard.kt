package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookCardContent
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookCardContentSkeleton
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCardItem(
    modifier: Modifier = Modifier,
    bookInformationFlow: Flow<Result<BookInformation, WebRequestError>>,
    selected: Boolean = false,
    collected: Boolean = false,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    latestChapterTitle: String? = null,
    shimmer: Shimmer? = null,
    swipeToRightActions: List<SwipeAction> = listOf(),
    swipeToLeftActions: List<SwipeAction> = listOf(),
    titleHeight: Dp?
) {
    SwipeableActionsBox(
        startActions = swipeToRightActions,
        endActions = swipeToLeftActions
    ) {
        val result by bookInformationFlow.collectAsStateWithLifecycle(null)
        Crossfade(
            targetState = result,
            label = "BookCardCrossfade"
        ) { result ->
            result?.onOk {
                BookCardContent(
                    modifier = modifier,
                    selected = selected,
                    collected = collected,
                    latestChapterTitle = latestChapterTitle,
                    bookInformation = it,
                    onClick = onClick,
                    onLongPress = onLongPress,
                    titleHeight = titleHeight
                )
            }?.onErr {
                //TODO 错误显示
            } ?: BookCardContentSkeleton(
                modifier = if (shimmer != null) modifier.shimmer(shimmer)
                else modifier
            )
        }
    }
}