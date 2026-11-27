package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug

import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry

@Composable
fun DebugScreen(
    onClickBack: () -> Unit,
    onClickQuery: (String) -> Unit,
    onClickOpenBook: (Identifier.Book) -> Unit,
    result: String
) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { TopBar(onClickBack) }
        item { BookBlock(onClickOpenBook) }
        item { SqlBlock(onClickQuery, result) }
        item { CrashBlock() }
    }
}

@Composable
fun BookBlock(onClickOpenBook: (Identifier.Book) -> Unit) {
    var bookId by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth()) {
        SectionHeader(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            text = "打开书本"
        )
        OutlinedTextField(
            value = bookId,
            onValueChange = { bookId = it },
            label = { Text("书本ID") },
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 14.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                runCatching { Identifier.parseAs<Identifier.Book>(bookId) }.getOrNull()
                    ?.let(onClickOpenBook)
            }) {
                Text("打开")
            }
        }
    }
}

@Composable
fun SqlBlock(onClickQuery: (String) -> Unit, result: String) {
    var sqlCommand by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth()) {
        SectionHeader(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            text = "SQL调试"
        )
        OutlinedTextField(
            value = sqlCommand,
            onValueChange = { sqlCommand = it },
            label = { Text("SQL指令") },
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 14.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { onClickQuery(sqlCommand) }) {
                Text("执行")
            }
        }
        Text(
            text = result,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun CrashBlock() {
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            text = "崩溃测试"
        )
        SettingsClickableEntry(
            title = "Crash by Lopper",
            description = "Looper.getMainLooper().quit()",
            onClick = { Looper.getMainLooper().quit() }
        )
        SettingsClickableEntry(
            title = "Crash by NPE",
            description = "NullPointerException",
            onClick = { throw NullPointerException() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.debug_settings))
            }
        },
        navigationIcon = {
            IconButton(onClickBack) {
                Icon(
                    painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
    )
}
