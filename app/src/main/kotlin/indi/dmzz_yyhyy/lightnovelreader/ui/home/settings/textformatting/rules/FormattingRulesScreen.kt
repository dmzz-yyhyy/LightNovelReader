package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingRule
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.RegexText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormattingRulesScreen(
    rules: List<FormattingRule>,
    isGlobal: Boolean,
    onToggle: (rule: Int) -> Unit,
    onClickBack: () -> Unit,
    onClickAddRule: () -> Unit,
    onClickEditRule: (ruleId: Int) -> Unit
) {
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = enterAlwaysScrollBehavior,
                isGlobal = isGlobal,
                onClickBack = onClickBack,
                onClickAddRule = onClickAddRule
            )
        }
    ) { paddingValues ->
        if (rules.isEmpty()) {
            EmptyPage(
                modifier = Modifier.padding(paddingValues),
                icon = painterResource(R.drawable.empty_90dp),
                title = stringResource(R.string.text_formatting_no_rules)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                items(rules) { rule ->
                    RuleListItem(
                        rule = rule,
                        ruleId = rule.id!!,
                        onEdit = onClickEditRule,
                        onToggle = onToggle
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleListItem(
    rule: FormattingRule,
    ruleId: Int,
    onEdit: (ruleId: Int) -> Unit,
    onToggle: (ruleId: Int) -> Unit
) {
    val ruleTitle = if (rule.name.isBlank()) {
        stringResource(R.string.text_formatting_unnamed_title)
    } else {
        rule.name
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onEdit(ruleId) }
                .padding(vertical = 12.dp)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (rule.isRegex) {
                    Icon(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(24.dp),
                        painter = painterResource(R.drawable.regular_expression_24px),
                        tint = colorScheme.primary,
                        contentDescription = ""
                    )
                }
                Text(
                    text = ruleTitle,
                    style = typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (rule.isRegex)
                    RegexText(
                        regex = rule.match,
                        style = typography.labelMedium,
                        color = colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                    )
                else
                    Text(
                        text = rule.match,
                        style = typography.labelMedium,
                        color = colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                    )
                Icon(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(20.dp),
                    painter = painterResource(R.drawable.keyboard_double_arrow_right_24px),
                    tint = colorScheme.secondary,
                    contentDescription = null
                )
                Text(
                    text = rule.replacement,
                    style = typography.labelMedium,
                    color = colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                )
            }


        }

        VerticalDivider(
            modifier = Modifier
                .padding(start = 4.dp, end = 12.dp)
                .height(24.dp),
            color = colorScheme.secondary
        )

        Switch(
            checked = rule.isEnabled,
            onCheckedChange = {
                onToggle(ruleId)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    isGlobal: Boolean,
    onClickBack: () -> Unit,
    onClickAddRule: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(
                    id = if (isGlobal) R.string.global_formatting_rules else R.string.book_rules
                ),
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
            IconButton(onClick = onClickAddRule) {
                Icon(
                    painter = painterResource(id = R.drawable.add_circle_24px),
                    contentDescription = "add"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
