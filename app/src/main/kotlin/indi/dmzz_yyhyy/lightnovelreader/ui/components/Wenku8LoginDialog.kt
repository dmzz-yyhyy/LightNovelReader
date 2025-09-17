package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login.Wenku8LoginResult
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.login.Wenku8LoginViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Wenku8LoginDialog(
    onDismissRequest: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: Wenku8LoginViewModel = hiltViewModel(),
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(viewModel) {
        onDispose { viewModel.reset() }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(id = R.string.login_wenku8)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text(stringResource(id = R.string.login_account)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(id = R.string.login_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                when {
                    uiState.isLoading -> Text(text = stringResource(id = R.string.login_logging_in), modifier = Modifier.padding(top = 12.dp))
                    uiState.isSuccess -> Text(text = stringResource(id = R.string.login_success), modifier = Modifier.padding(top = 12.dp))
                    uiState.error != null -> {
                        val msg = when (val err = uiState.error) {
                            Wenku8LoginResult.Failure.Username -> stringResource(id = R.string.login_error_username)
                            Wenku8LoginResult.Failure.Password -> stringResource(id = R.string.login_error_password)
                            Wenku8LoginResult.Failure.Network -> stringResource(id = R.string.login_error_network)
                            Wenku8LoginResult.Failure.NotLoggedIn -> stringResource(id = R.string.wenku8_err_not_logged_in)
                            is Wenku8LoginResult.Failure.Code -> when (err.code) {
                                4 -> stringResource(id = R.string.wenku8_err_bookshelf_full)
                                5 -> stringResource(id = R.string.wenku8_err_already_in_bookshelf)
                                6 -> stringResource(id = R.string.wenku8_err_novel_not_in_bookshelf)
                                7 -> stringResource(id = R.string.wenku8_err_topic_not_exist)
                                8 -> stringResource(id = R.string.wenku8_err_sign_failed)
                                9 -> stringResource(id = R.string.wenku8_err_recommend_failed)
                                10 -> stringResource(id = R.string.wenku8_err_post_failed)
                                11 -> stringResource(id = R.string.wenku8_err_refer_page_0)
                                else -> stringResource(id = R.string.login_error_unknown)
                            }
                            Wenku8LoginResult.Failure.Unknown -> stringResource(id = R.string.login_error_unknown)
                            else -> stringResource(id = R.string.login_error_unknown)
                        }
                        Text(text = msg, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.login(account, password) },
                enabled = account.isNotBlank() && password.isNotBlank() && !uiState.isLoading
            ) { Text(text = stringResource(id = R.string.apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(text = stringResource(id = R.string.cancel)) }
        }
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }
}
