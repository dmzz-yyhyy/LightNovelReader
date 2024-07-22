package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R

@Composable
fun ContentScreen(
    onClickBackButton: () -> Unit,
    topBar: (@Composable () -> Unit) -> Unit,
    bottomBar: (@Composable () -> Unit) -> Unit,
    padding: PaddingValues,
    bookId: Int,
    chapterId: Int,
    viewModel: ContentViewModel = hiltViewModel()
    ) {

    var lastChapterId by remember { mutableStateOf(0) }
    if (lastChapterId != chapterId) {
        lastChapterId = chapterId
        viewModel.init(bookId, chapterId)
    }
    var isImmersive by remember { mutableStateOf(false) }
    topBar {
        AnimatedVisibility(
            visible = isImmersive,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            TopBar(onClickBackButton, viewModel.uiState.chapterContent.title)
        }
    }
    bottomBar {

    }
    LazyColumn(
        Modifier
            .animateContentSize()
            .fillMaxSize()
            .clickable(
                interactionSource =
                remember { MutableInteractionSource() },
                indication = null
            ) {
                isImmersive = !isImmersive
            }
    ) {
        item {
            Text(viewModel.uiState.chapterContent.content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBackButton: () -> Unit,
    title: String
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onClickBackButton) {
                Icon(painterResource(id = R.drawable.arrow_back_24px), "back")
            }
        },
        title = {
            LazyRow {
                item {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = {}) {
                Icon(painterResource(id = R.drawable.menu_24px), "menu")
            }
        }
    )
}