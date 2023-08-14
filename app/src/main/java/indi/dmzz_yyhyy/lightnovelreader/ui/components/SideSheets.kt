package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ModalSideSheet(
    modifier: Modifier = Modifier,
    width: Dp = 256.dp,
    backButton: @Composable () -> Unit = { Box(Modifier.width(12.dp)) },
    title: @Composable () -> Unit,
    closeButton: @Composable () -> Unit,
    onClickScrim: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {Box(Modifier.fillMaxWidth().fillMaxHeight()) {
    Box(Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
        .clickable {
            onClickScrim()
        })
        Card(modifier
            .fillMaxHeight()
            .align(Alignment.TopStart)
            .width(width),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 0.dp,
                bottomStart = 12.dp,
                bottomEnd = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
            Column {
                Box(
                    Modifier
                        .padding(
                        top = 12.dp,
                        end = 24.dp,
                        start = 4.dp)
                        .fillMaxWidth()
                ) {
                    Box(Modifier.align(alignment = Alignment.TopStart)) {
                        Row {
                            backButton()
                            Box(Modifier.height(48.dp)) {
                                Box(Modifier.align(alignment = Alignment.Center)){
                                    title()
                                }
                            }
                        }
                    }
                    Box(Modifier.align(alignment = Alignment.TopEnd).padding(start = 12.dp)) {
                        closeButton()
                    }
                }
                Box(Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp)) {
                    content()
                }
            }
        }
    }
}