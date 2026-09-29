package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R

@Composable
fun ErrorPage(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    onRetry: () -> Unit,
    isRetryEnabled: Boolean = false
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                modifier = Modifier.size(44.dp),
                painter = painterResource(R.drawable.error_24px),
                contentDescription = null,
                tint = colorScheme.secondary
            )
            Text(
                text = title,
                style = typography.titleMedium,
                fontWeight = FontWeight.W600
            )
            Text(
                text = message,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = typography.bodyMedium,
                color = colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (isRetryEnabled) {
                Spacer(Modifier.height(8.dp))
                Button(
                    modifier = Modifier
                        .width(140.dp)
                        .height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = onRetry
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}