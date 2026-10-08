package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingGroup
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import io.nightfish.lightnovelreader.api.book.BookInformation

@Composable
fun TextFormattingScreen(
    onClickGroup: (String) -> Unit,
    onClickBack: () -> Unit,
    groups: List<FormattingGroup>,
    bookInformationMap: Map<String, BookInformation>
) {
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = enterAlwaysScrollBehavior,
                onClickBack = onClickBack
            )
        }
    ) { paddingValues ->
        val rules = groups.filter { it.id.isNotBlank() }
        if (rules.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                GlobalRulesGroup(
                    ruleCount = groups.firstOrNull { it.id.isEmpty() }?.size ?: 0,
                    onClick = { onClickGroup("") }
                )
                EmptyPage(
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.text_snippet_24px),
                    title = stringResource(R.string.text_formatting_no_book_rules),
                    description = stringResource(R.string.text_formatting_no_book_rules_desc)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues)
            ) {
                item {
                    GlobalRulesGroup(
                        ruleCount = groups.firstOrNull { it.id.isEmpty() }?.size ?: 0,
                        onClick = { onClickGroup("") }
                    )
                }
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp),
                        text = stringResource(R.string.book_rules),
                        style = typography.titleSmall,
                        letterSpacing = 0.5.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                items(rules) { group ->
                    Group(
                        onClickGroup = { onClickGroup(group.id) },
                        formattingGroup = group,
                        bookInformation = bookInformationMap[group.id]
                    )
                }
                navigationBarSpacer()
            }
        }
    }
}

@Composable
private fun GlobalRulesGroup(
    ruleCount: Int,
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = source
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.language_24px),
                tint = colorScheme.secondary,
                contentDescription = ""
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.global_formatting_rules),
                style = typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.n_rules, ruleCount),
                style = typography.bodyMedium,
                color = colorScheme.secondary
            )
        }
        IconButton(
            onClick = onClick,
            interactionSource = source
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(R.drawable.arrow_forward_ios_24px),
                tint = colorScheme.secondary,
                contentDescription = ""
            )
        }
    }
}

@Composable
private fun Group(
    onClickGroup: (String) -> Unit,
    formattingGroup: FormattingGroup,
    bookInformation: BookInformation?
) {
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClickGroup(formattingGroup.id) },
                interactionSource = source
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bookInformation != null) {
            Cover(
                width = 60.dp,
                height = 87.dp,
                uri = bookInformation.coverUri,
                rounded = 8.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = bookInformation.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.W600
                )
                Text(
                    text = bookInformation.author,
                    style = typography.bodyMedium,
                    color = colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.n_rules, formattingGroup.size),
                    style = typography.bodyMedium,
                    color = colorScheme.secondary
                )
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
        IconButton(
            onClick = { onClickGroup(formattingGroup.id) },
            interactionSource = source
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(R.drawable.arrow_forward_ios_24px),
                tint = colorScheme.secondary,
                contentDescription = "enter"
            )
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.settings_text_formatting),
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

